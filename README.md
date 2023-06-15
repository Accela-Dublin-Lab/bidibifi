# bidibifi
Bidirectional Binary SSL Filtering Proxy

[Client] -> [PROXY] -> [TARGET]

## Set-up keystore for SSL
    $ keytool -genkeypair -alias test -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore keystore.p12
            -validity 365 -storepass password -keypass password
            -dname "CN=localhost, OU=Test, O=Company, L=City, ST=State, C=US"

    $ keytool -genkey -keypass password \
            -storepass password \
            -storetype PKCS12 \
            -keyalg RSA -keysize 2048 \
            -keystore serverkeystore.jks \
            -dname "CN=localhost, OU=Test, O=Company, L=City, ST=State, C=US"

     $ keytool -export -storepass password \
                     -file server.cer \
                     -keystore serverkeystore.jks

     $ keytool -import -v -trustcacerts \
                     -file server.cer \
                     -keypass password \
                     -storepass password \
                     -keystore clienttruststore.jks

## Build

    $ sbt buildExecutableJar

## Command line
    $ java -jar target/bidibifi.jar \
        -Djavax.net.ssl.keyStore=.../serverkeystore.jks \
        -Djavax.net.ssl.keyStorePassword=password \
        --target-host=velosb2.servicebus.windows.net --target-port=5671

