package com.alibaba.rocketmq.remoting.netty;

import com.alibaba.rocketmq.remoting.exception.SSLContextCreationException;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

public class SslHelper {

    private static final String DEFAULT_KEY_STORE_PASSWORD = "changeit";

    private static final String DEFAULT_KEY_PASSWORD = "changeit";

    private static final String INITIAL_VECTOR = "0102030405060708";

    private static final String CIPHER = "AES/CBC/PKCS5Padding";

    public static SSLContext getSSLContext(SslRole role) throws SSLContextCreationException {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            KeyStore keyStore = KeyStore.getInstance("JKS");
            KeyStore trustKeyStore = KeyStore.getInstance("JKS");

            ClassLoader classLoader = SslHelper.class.getClassLoader();

            switch (role) {
                case SERVER:
                    keyStore.load(classLoader.getResourceAsStream("server.ks"),
                            System.getProperty("RocketMQServerKeyStorePassword", DEFAULT_KEY_STORE_PASSWORD).toCharArray());
                    trustKeyStore.load(classLoader.getResourceAsStream("server.ks"),
                            System.getProperty("RocketMQServerTrustKeyStorePassword", DEFAULT_KEY_STORE_PASSWORD).toCharArray());
                    keyManagerFactory.init(keyStore,
                            System.getProperty("RocketMQServerKeyPassword", DEFAULT_KEY_PASSWORD).toCharArray());
                    trustManagerFactory.init(trustKeyStore);
                    break;

                case CLIENT:
                    keyStore.load(classLoader.getResourceAsStream("client.ks"),
                            System.getProperty("RocketMQClientKeyStorePassword", DEFAULT_KEY_STORE_PASSWORD).toCharArray());
                    trustKeyStore.load(classLoader.getResourceAsStream("client.ks"),
                            System.getProperty("RocketMQClientTrustKeyStorePassword", DEFAULT_KEY_STORE_PASSWORD).toCharArray());
                    keyManagerFactory.init(keyStore,
                            System.getProperty("RocketMQClientKeyPassword", DEFAULT_KEY_PASSWORD).toCharArray());
                    trustManagerFactory.init(trustKeyStore);
                    break;
            }
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            return sslContext;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new SSLContextCreationException("Error while creating SSLContext", e);
        } catch (KeyStoreException e) {
            e.printStackTrace();
            throw new SSLContextCreationException("Error while creating SSLContext", e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new SSLContextCreationException("Error while creating SSLContext", e);
        } catch (CertificateException e) {
            e.printStackTrace();
            throw new SSLContextCreationException("Error while creating SSLContext", e);
        } catch (KeyManagementException e) {
            e.printStackTrace();
            throw new SSLContextCreationException("Error while creating SSLContext", e);
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
            throw new SSLContextCreationException("Error while creating SSLContext", e);
        }
    }


    public static SSLEngine getSSLEngine(SSLContext context, SslRole role) {
        SSLEngine engine = context.createSSLEngine();
        switch (role) {
            case SERVER:
                engine.setUseClientMode(false);
                break;
            case CLIENT:
                engine.setUseClientMode(true);
                break;
        }
        return engine;
    }

    public static String encrypt(String strKey, String strIn) throws Exception {
        SecretKeySpec secretKeySpec = getKey(strKey);
        Cipher cipher = Cipher.getInstance(CIPHER);
        IvParameterSpec iv = new IvParameterSpec(INITIAL_VECTOR.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);
        byte[] encrypted = cipher.doFinal(strIn.getBytes());
        return new BASE64Encoder().encode(encrypted);
    }

    public static String decrypt(String strKey, String strIn) throws Exception {
        SecretKeySpec secretKeySpec = getKey(strKey);
        Cipher cipher = Cipher.getInstance(CIPHER);
        IvParameterSpec iv = new IvParameterSpec(INITIAL_VECTOR.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);
        byte[] encrypted1 = new BASE64Decoder().decodeBuffer(strIn);

        byte[] original = cipher.doFinal(encrypted1);
        String originalString = new String(original);
        return originalString;
    }

    private static SecretKeySpec getKey(String strKey) throws Exception {
        byte[] tmpArray = strKey.getBytes();
        byte[] array = new byte[16]; // 创建一个空的16位字节数组（默认值为0）

        System.arraycopy(tmpArray, 0, array, 0, tmpArray.length > 16 ? 16 : tmpArray.length);
        return new SecretKeySpec(array, "AES");
    }

    public static void main(String[] args) throws Exception {
        String Code = "中文ABc123";
        String key = "1q2w3e4r";
        String codE;

        codE = encrypt(key, Code);

        System.out.println("原文：" + Code);
        System.out.println("密钥：" + key);
        System.out.println("密文：" + codE);
        System.out.println("解密：" + decrypt(key, codE));
    }
}

enum SslRole {
    CLIENT, SERVER
}
