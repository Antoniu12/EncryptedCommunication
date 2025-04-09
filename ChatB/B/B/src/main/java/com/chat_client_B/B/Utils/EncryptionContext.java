package com.chat_client_B.B.Utils;


public class EncryptionContext {
    private static EncryptionMode currentMode = EncryptionMode.RAW;

    private static volatile Boolean flag = Boolean.FALSE;

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
}