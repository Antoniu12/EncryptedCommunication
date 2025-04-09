package com.chat_interceptor.demo.Utils;

import com.chat_interceptor.demo.Utils.EncryptionMode;

public class EncryptionContext {
    private static EncryptionMode currentMode = EncryptionMode.RAW;

    public static EncryptionMode getMode() {
        return currentMode;
    }

    public static void setMode(EncryptionMode mode) {
        currentMode = mode;
    }
}
