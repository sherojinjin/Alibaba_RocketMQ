package com.ndpmedia.rocketmq.lsr.paxos.network;

import static com.ndpmedia.rocketmq.lsr.common.ProcessDescriptor.processDescriptor;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

import com.ndpmedia.rocketmq.lsr.common.KillOnExceptionHandler;
import com.ndpmedia.rocketmq.lsr.common.PID;
import com.ndpmedia.rocketmq.lsr.paxos.messages.Message;
import com.ndpmedia.rocketmq.lsr.paxos.messages.MessageFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for handling stable TCP connection to other
 * replica, provides two methods for establishing new connection: active and
 * passive. In active mode we try to connect to other side creating new socket
 * and connects. If passive mode is enabled, then we wait for socket from the
 * <code>SocketServer</code> provided by <code>TcpNetwork</code>.
 * <p>
 * Every time new message is received from this connection, it is deserialized,
 * and then all registered network listeners in related <code>TcpNetwork</code>
 * are notified about it.
 * 
 * @see TcpNetwork
 */
public class TcpConnection {
    public static final int TCP_BUFFER_SIZE = 16 * 1024 * 1024;
    private static final long MAX_QUEUE_OFFER_DELAY_MS = 25L;
    private Socket socket;
    private DataInputStream input;
    private OutputStream output;

    private final PID replica;

    private volatile boolean connected = false;
    private volatile long lastSndTs = 0L;
    private volatile boolean writing = false;

    private final Object connectedLock = new Object();

    /** true if connection should be started by this replica; */
    private final boolean active;

    private final TcpNetwork network;

    private final Thread senderThread;
    private final Thread receiverThread;

    private final ArrayBlockingQueue<byte[]> sendQueue = new ArrayBlockingQueue<byte[]>(512);
    private final int peerId;

    private boolean closing = false;

    /**
     * Creates a new TCP connection to specified replica.
     * 
     * @param network - related <code>TcpNetwork</code>.
     * @param replica - replica to connect to.
     * @param peerId - ID of the replica on the other end of connection
     * @param active - initiates connection if true; waits for remote connection
     *            otherwise.
     */
    public TcpConnection(TcpNetwork network, final PID replica, int peerId, boolean active) {
        this.network = network;
        this.replica = replica;
        this.peerId = peerId;
        this.active = active;

        logger.info("Creating connection: {} - {}", replica, active);

        receiverThread = new Thread(new ReceiverThread(), "ReplicaIORcv-" + replica.getId());
        senderThread = new Thread(new Sender(), "ReplicaIOSnd-" + replica.getId());

        receiverThread.setUncaughtExceptionHandler(new KillOnExceptionHandler());
        senderThread.setUncaughtExceptionHandler(new KillOnExceptionHandler());

        receiverThread.setDaemon(true);
        senderThread.setDaemon(true);
        senderThread.setPriority(Thread.MAX_PRIORITY);
    }

    /**
     * Starts the receiver and sender thread.
     */
    public synchronized void start() {
        receiverThread.start();
        senderThread.start();
    }

    final class Sender implements Runnable {

        public void run() {
            logger.debug("Sender thread started.");
            try {
                while (true) {
                    // wait for connection
                    synchronized (connectedLock) {
                        while (!connected)
                            connectedLock.wait();
                    }

                    while (true) {
                        if (Thread.interrupted()) {
                            if (!closing)
                                throw new RuntimeException("Sender " +
                                                           Thread.currentThread().getName() +
                                                           " thread has been interupted and stopped.");
                            return;
                        }
                        byte[] msg = sendQueue.take();
                        // ignore message if not connected
                        // Works without memory barrier because connected is
                        // volatile
                        if (!connected) {
                            sendQueue.offer(msg);
                            break;
                        }

                        try {
                            writing = true;
                            output.write(msg);
                            output.flush();
                            writing = false;
                        } catch (IOException e) {
                            logger.warn("Error sending message", e);
                            writing = false;
                            close();
                        }
                        lastSndTs = System.currentTimeMillis();
                    }
                }
            } catch (InterruptedException e) {
                if (closing)
                    logger.info("Clean closing the {}", Thread.currentThread().getName());
                else
                    throw new RuntimeException("Sender " + Thread.currentThread().getName() +
                                               " thread has been interupted", e);
            }
        }
    }

    /**
     * Main loop used to connect and read from the socket.
     */
    final class ReceiverThread implements Runnable {
        public void run() {
            do {
                if (Thread.interrupted()) {
                    if (!closing)
                        throw new RuntimeException("Receiver thread has been interrupted.");
                    return;
                }

                logger.info("Waiting for tcp connection to {}", replica.getId());

                try {
                    connect();
                } catch (InterruptedException e) {
                    if (!closing)
                        throw new RuntimeException("Receiver thread has been interrupted.");
                    break;
                }

                while (true) {
                    if (Thread.interrupted()) {
                        if (!closing)
                            throw new RuntimeException("Receiver thread has been interrupted.");
                        return;
                    }

                    try {
                        Message message = MessageFactory.create(input);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Received [{}] {} size: {}", replica.getId(), message,
                                    message.byteSize());
                        }
                        network.fireReceiveMessage(message, replica.getId());
                    } catch (EOFException e) {
                        // end of stream with socket occurred so close
                        // connection and try to establish it again
                        if (!closing) {
                            logger.info("Error reading message - EOF", e);
                            close();
                        }
                        break;
                    } catch (IOException e) {
                        // problem with socket occurred so close connection and
                        // try to establish it again
                        if (!closing) {
                            logger.warn("Error reading message (?)", e);
                            close();
                        }
                        break;
                    }
                }
            } while (active);
        }
    }

    /**
     * Sends specified binary packet using underlying TCP connection.
     * 
     * @param message - binary packet to send
     * @return true if sending message was successful
     */
    public void send(byte[] message) {
        if (connected) {
            // FIXME: (JK) discuss what should be done here

            while (!sendQueue.offer(message)) {
                // if some messages are being sent, wait a while
                if (!writing || System.currentTimeMillis() - lastSndTs <= MAX_QUEUE_OFFER_DELAY_MS) {
                    Thread.yield();
                    continue;
                }

                byte[] discarded = sendQueue.poll();
                if (logger.isDebugEnabled()) {
                    logger.warn(
                            "TCP msg queue overfolw: Discarding message {} to send {}. Last send: {}, writing: {}",
                            discarded.toString(), message.toString(), System.currentTimeMillis() -
                                                                      lastSndTs, writing);
                } else {
                    logger.warn("TCP msg queue overfolw: Discarding a message to send anoter");
                }
            }
        } else {
            // keep last n messages
            while (!sendQueue.offer(message)) {
                sendQueue.poll();
            }
        }

    }

    /**
     * Registers new socket to this TCP connection. Specified socket should be
     * initialized connection with other replica. First method tries to close
     * old connection and then set-up new one.
     * 
     * @param socket - active socket connection
     * @param input - input stream from this socket
     * @param output - output stream from this socket
     */
    public synchronized void setConnection(Socket socket, DataInputStream input,
                                           DataOutputStream output) {
        assert socket != null : "Invalid socket state";

        // initialize new connection
        this.socket = socket;
        this.input = input;
        this.output = output;

        logger.info("TCP connection accepted from {}", replica);

        synchronized (connectedLock) {
            connected = true;
            // wake up receiver and sender
            connectedLock.notifyAll();
        }
    }

    public void stopAsync() {
        close();
        receiverThread.interrupt();
        senderThread.interrupt();
    }

    /**
     * Stops current connection and stops all underlying threads.
     * 
     * Note: This method waits until all threads are finished.
     * 
     * @throws InterruptedException
     */
    public void stop() throws InterruptedException {
        close();
        receiverThread.interrupt();
        senderThread.interrupt();

        receiverThread.join();
        senderThread.join();
    }

    /**
     * Establishes connection to host specified by this object. If this is
     * active connection then it will try to connect to other side. Otherwise we
     * will wait until connection will be set-up using
     * <code>setConnection</code> method. This method will return only if the
     * connection is established and initialized properly.
     * 
     * @throws InterruptedException
     */
    private void connect() throws InterruptedException {
        if (active) {
            // this is active connection so we try to connect to host
            while (true) {
                try {
                    socket = new Socket();
                    socket.setReceiveBufferSize(TCP_BUFFER_SIZE);
                    socket.setSendBufferSize(TCP_BUFFER_SIZE);
                    logger.debug("RcvdBuffer: {}, SendBuffer: {}", socket.getReceiveBufferSize(),
                            socket.getSendBufferSize());
                    socket.setTcpNoDelay(true);

                    logger.info("Connecting to: {}", replica);
                    try {
                        socket.connect(new InetSocketAddress(replica.getHostname(),
                                replica.getReplicaPort()),
                                (int) processDescriptor.tcpReconnectTimeout);
                    } catch (ConnectException e) {
                        logger.info("TCP connection with replica {} failed", replica.getId());
                        Thread.sleep(processDescriptor.tcpReconnectTimeout);
                        continue;
                    } catch (SocketTimeoutException e) {
                        logger.info("TCP connection with replica {} timed out", replica.getId());
                        continue;
                    } catch (SocketException e) {
                        if (socket.isClosed()) {
                            logger.warn("Invoking connect() on closed socket. Quitting?");
                            return;
                        }
                        throw new RuntimeException("when else it can be thrown here?", e);
                    } catch (IOException e) {
                        throw new RuntimeException("what else can be thrown here?", e);
                    }

                    input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

                    output = socket.getOutputStream();

                    byte buf[] = new byte[4];
                    ByteBuffer.wrap(buf).putInt(processDescriptor.localId);

                    output.write(buf);
                    output.flush();
                    // connection established
                    break;
                } catch (IOException e) {
                    throw new RuntimeException("Unexpected error connecting to " + replica, e);
                }
            }

            logger.info("TCP connect successfull to {}", replica);

            // Wake up the sender thread
            synchronized (connectedLock) {
                connected = true;
                // notify sender
                connectedLock.notifyAll();
            }
            network.addConnection(peerId, this);

        } else {
            // this is passive connection so we are waiting until other replica
            // connect to us; we will be notified by setConnection method
            synchronized (connectedLock) {
                while (!connected) {
                    connectedLock.wait();
                }
            }
        }
    }

    /**
     * Closes the connection.
     */
    private synchronized void close() {
        if (active)
            network.removeConnection(peerId, this);
        closing = true;
        connected = false;
        if (socket != null && socket.isConnected()) {
            logger.info("Closing TCP connection to {}", replica);
            try {
                socket.shutdownOutput();
                socket.close();
                socket = null;
                logger.info("TCP connection closed to {}", replica);
            } catch (IOException e) {
                logger.warn("Error closing socket", e);
            }
        }
    }

    private final static Logger logger = LoggerFactory.getLogger(TcpConnection.class);

    public boolean isActive() {
        return active;
    }
}
