package com.alibaba.rocketmq.remoting.netty;

import io.netty.handler.ssl.SslContext;

import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

    public static File certificate() throws SSLException, CertificateException {
        if (null == SSL.getProperty("ssl.cert")) {
            System.err.println("ssl.properties file does not contain configuration for certificate path");
            System.exit(1);
        }
        return new File(SSL.getProperty("ssl.cert"));
    }

    public static File privateKey() throws SSLException, CertificateException {
        if (null == SSL.getProperty("ssl.key")) {
            System.err.println("ssl.properties file does not contain configuration for certificate path");
            System.exit(1);
        }
        return new File(SSL.getProperty("ssl.key"));
    }

    public static String privateKeyPassword() {
        String password = null;
        password = System.getenv("ROCKETMQ_KEY_PASSWORD");
        if (null != password) {
            return password;
        } else {
            password = System.getProperty("ssl_key.password");
            if (null != password) {
                return password;
            }
            return "";
        }

    }

    public static SslContext getClientSSLContext() throws SSLException {
        return SslContext.newClientContext((TrustManagerFactory)null);
    }
}
