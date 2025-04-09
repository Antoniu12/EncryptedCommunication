package com.chat_interceptor.demo.MitMSimulations.RSA;

import java.security.PublicKey;

public class SharedKeys {
    public static PublicKey sharedKeyA;
    public static PublicKey sharedKeyB;

    public static void SharedKeys(){

    }

    public static PublicKey getSharedKeyA() {
        return sharedKeyA;
    }

    public static void setSharedKeyA(PublicKey key) {
        sharedKeyA = key;
    }
    public static PublicKey getSharedKeyB() {
        return sharedKeyB;
    }

    public static void setSharedKeyB(PublicKey key) {
        sharedKeyB = key;
    }
    public static void reset() {
        sharedKeyA = null;
        sharedKeyB = null;

    }
}

