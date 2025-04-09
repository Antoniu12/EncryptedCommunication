package com.chat_client_A.A.Utils;


public class EncryptionContext {
    private static EncryptionMode currentMode = EncryptionMode.RAW;

    private static volatile Boolean flag = false;
    private static volatile Boolean storedSecret = false;
    public static EncryptionMode getMode() {
        return currentMode;
    }

    public static void setMode(EncryptionMode mode) {
        currentMode = mode;
    }
    public static Boolean getFlag(){
        return flag;
    }
    public static void setFlag(Boolean f){
        flag = f;
    }

    public static Boolean getStoredSecret() {
        return storedSecret;
    }

    public static void setStoredSecret(Boolean storedSecret) {
        EncryptionContext.storedSecret = storedSecret;
    }
}