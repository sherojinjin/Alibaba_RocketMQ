package com.alibaba.rocketmq.remoting.netty;

import com.alibaba.rocketmq.remoting.exception.SSLContextCreationException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Properties;

public class SslHelper {

    private static final Properties SSL = new Properties();

    static {
        ClassLoader classLoader = SslHelper.class.getClassLoader();
        InputStream inputStream = null;
        try {
            inputStream = classLoader.getResourceAsStream("ssl.properties");
            SSL.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static SSLContext getSSLContext(SslRole role) throws SSLContextCreationException {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            KeyStore keyStore = KeyStore.getInstance("JKS");
            KeyStore trustKeyStore = KeyStore.getInstance("JKS");

            switch (role) {
                case SERVER:
                    keyStore.load(new FileInputStream(SSL.getProperty("ssl.serverKeyStore")),
                            System.getProperty("RocketMQServerKeyStorePassword").toCharArray());
                    trustKeyStore.load(new FileInputStream(SSL.getProperty("ssl.serverTrustKeyStore")),
                            System.getProperty("RocketMQServerTrustKeyStorePassword").toCharArray());
                    keyManagerFactory.init(keyStore, System.getProperty("RocketMQServerKeyPassword").toCharArray());
                    trustManagerFactory.init(trustKeyStore);
                    break;

                case CLIENT:
                    keyStore.load(new FileInputStream(SSL.getProperty("ssl.clientKeyStore")),
                            System.getProperty("RocketMQClientKeyStorePassword").toCharArray());
                    trustKeyStore.load(new FileInputStream(SSL.getProperty("ssl.clientTrustKeyStore")),
                            System.getProperty("RocketMQClientTrustKeyStorePassword").toCharArray());
                    keyManagerFactory.init(keyStore, System.getProperty("RocketMQClientKeyPassword").toCharArray());
                    trustManagerFactory.init(trustKeyStore);
                    break;
            }
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            return sslContext;
        } catch (NoSuchAlgorithmException e) {
            throw new SSLContextCreationException("Error while creating SSLContext", e);
        } catch (KeyStoreException e) {
            throw new SSLContextCreationException("Error while creating SSLContext", e);
        } catch (IOException e) {
            throw new SSLContextCreationException("Error while creating SSLContext", e);
        } catch (CertificateException e) {
            throw new SSLContextCreationException("Error while creating SSLContext", e);
        } catch (KeyManagementException e) {
            throw new SSLContextCreationException("Error while creating SSLContext", e);
        } catch (UnrecoverableKeyException e) {
            throw new SSLContextCreationException("Error while creating SSLContext", e);
        }
    }

}

enum SslRole {
    CLIENT, SERVER
}
