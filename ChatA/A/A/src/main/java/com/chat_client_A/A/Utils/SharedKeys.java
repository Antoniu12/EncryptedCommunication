package com.chat_client_A.A.Utils;

import javax.crypto.SecretKey;
import java.security.PublicKey;

public class SharedKeys {
    public static PublicKey sharedKey;
    public static SecretKey secretKey;
    public static SecretKey hellmanKey;
    public static void SharedKeys(){

    }

    public static PublicKey getSharedKey() {
        return sharedKey;
    }

    public static void setSharedKey(PublicKey key) {
        sharedKey = key;
    }

    public static SecretKey getSecretKey() {
        return secretKey;
    }

    public static void setSecretKey(SecretKey secretKey) {
        SharedKeys.secretKey = secretKey;
    }

    public static SecretKey getHellmanKey() {
        return hellmanKey;
    }

    public static void setHellmanKey(SecretKey hellmanKey) {
        SharedKeys.hellmanKey = hellmanKey;
    }

    public static void reset() {
        sharedKey = null;
        secretKey = null;
        hellmanKey = null;
    }
}
