package com.alibaba.rocketmq.remoting.netty;

import com.alibaba.rocketmq.remoting.exception.SSLContextCreationException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
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

    public static SSLContext getSSLContext() throws SSLContextCreationException {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            KeyStore keyStore = KeyStore.getInstance("JKS");
            KeyStore trustKeyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(SSL.getProperty("ssl.keyStore")),
                    System.getProperty("RocketMQKeyStorePassword").toCharArray());
            trustKeyStore.load(new FileInputStream(SSL.getProperty("ssl.trustKeyStore")),
                    System.getProperty("RocketMQTrustKeyStorePassword").toCharArray());
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
        }
    }

}
