package com.chat_interceptor.demo.MitMSimulations.RSA;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSA {

    private static KeyPair keyPair;

    static {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            keyPair = generator.generateKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getEncodedPublicKey() {
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }

    public static PublicKey decodePublicKey(String base64Key) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(base64Key);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(new X509EncodedKeySpec(bytes));
    }

    public static String encryptWithPeerPublic(String message, PublicKey peerPublicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, peerPublicKey);
        byte[] encrypted = cipher.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decryptWithOwnPrivate(String base64CipherText) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        byte[] decoded = Base64.getDecoder().decode(base64CipherText);
        return new String(cipher.doFinal(decoded));
    }

    public static PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public static PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }
}
