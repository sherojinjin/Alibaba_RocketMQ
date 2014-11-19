package com.ndpmedia.security;

import sun.misc.BASE64Encoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.security.*;
import java.security.cert.Certificate;

public class KeyExporter {

    private File keyStoreFile;
    private String keyStoreType;
    private char[] password;
    private String alias;
    private File exportedFile;

    public static KeyPair getPrivateKey(KeyStore keystore, String alias, char[] password) {
        try {
            Key key = keystore.getKey(alias, password);
            if (key instanceof PrivateKey) {
                Certificate cert = keystore.getCertificate(alias);
                PublicKey publicKey = cert.getPublicKey();
                return new KeyPair(publicKey, (PrivateKey) key);
            }
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void export() throws Exception {
        KeyStore keystore = KeyStore.getInstance(keyStoreType);
        BASE64Encoder encoder = new BASE64Encoder();
        keystore.load(new FileInputStream(keyStoreFile), password);
        KeyPair keyPair = getPrivateKey(keystore, alias, password);
        PrivateKey privateKey = keyPair.getPrivate();
        String encoded = encoder.encode(privateKey.getEncoded());
        FileWriter fw = new FileWriter(exportedFile);
        fw.write("—–BEGIN PRIVATE KEY—–\n");
        fw.write(encoded);
        fw.write("\n");
        fw.write("—–END PRIVATE KEY—–\n");
        fw.close();
    }


    public static void main(String args[]) throws Exception {

        if (args.length != 5) {
            System.out.println("Usage:");
            System.out.println("java -jar rocketmq-security-3.2.2.jar key-store-path key-store-type password alias exported-file-name");
            return;
        }

        KeyExporter export = new KeyExporter();
        export.keyStoreFile = new File(args[0]);
        export.keyStoreType = args[1];
        export.password = args[2].toCharArray();
        export.alias = args[3];
        export.exportedFile = new File(args[4]);
        export.export();
    }
}
