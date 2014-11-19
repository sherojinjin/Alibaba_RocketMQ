SSL
    Use the following script to generate key/certificate pair.
    `sh ssl_key_cer.sh`
    Add the cer.pem to key store, for example
     `sudo keytool -import -alias NDPMedia -file cer.pem -keystore ${JAVA_HOME}/jre/lib/security/cacerts`