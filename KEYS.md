# Signing keys
The keys used for signing apps are found in the `assets/` folder in the source tree and in the final APK. 

From a regular Java keystore `key.jks` that contained a signing key with alias `key`, the files were extracted as such:

```bash
keytool -exportcert -alias key -keystore key.jks -file key.crt
keytool -importkeystore -srckeystore key.jks -destkeystore keystore.p12 -deststoretype PKCS12 -srcalias key
openssl pkcs12 -in keystore.p12 -nocerts -nodes -out key.pem
openssl pkcs8 -topk8 -inform PEM -outform DER -in key.pem -out key.pk8 -nocrypt
```

The produced `key.crt` and `key.pk8` being what the app will use for signing.
