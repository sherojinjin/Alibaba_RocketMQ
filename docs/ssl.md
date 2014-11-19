SSL Configuration
======================

##Generate SSL Key Pairs
`keytool -genkey -keyalg RSA -alias CAPS -keystore keystore_filename`

More command options are:

`keytool -genkey -keyalg algortihm -alias keypair_name -keypass keypair_password -keystore keystore_name -storepass keystore_password -validity number_days`

##Export public certificate
keytool -export -keystore NDPMedia.ks -alias NDPMedia -file pub.cer


