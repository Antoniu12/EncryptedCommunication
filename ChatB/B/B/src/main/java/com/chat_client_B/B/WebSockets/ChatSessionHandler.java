package com.chat_client_B.B.WebSockets;

import com.chat_client_B.B.DTO.ChatMessage;
import com.chat_client_B.B.EncryptionMethods.DiffieHellman.DiffieHellman;
import com.chat_client_B.B.EncryptionMethods.RSA.RSA;
import com.chat_client_B.B.Utils.EncryptionContext;
import com.chat_client_B.B.Utils.EncryptionMode;
import com.chat_client_B.B.Utils.SharedKeys;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import javax.crypto.SecretKey;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.security.PublicKey;

public class ChatSessionHandler extends StompSessionHandlerAdapter {
    private final String name;

    public ChatSessionHandler(String name) {
        this.name = name;
    }


    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        session.subscribe("/topic/messages", this);
        System.out.println("You secured a connection! Start typing");
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        ChatMessage msg = (ChatMessage) payload;
        if (msg.getSender().equals(name)) return;
        String content = msg.getContent();

        if (content.startsWith("RSA_KEY:")) {
            try {
                if (SharedKeys.getSharedKey() == null) {
                    String encodedKey = content.substring(8);
                    PublicKey peerKey = RSA.decodePublicKey(encodedKey);
                    SharedKeys.setSharedKey(peerKey);
//                    System.out.println("[RSA] Stored peer public key: " + encodedKey);
                }
            } catch (Exception e) {
                System.out.println("[RSA] Failed to decode key: " + e.getMessage());
            }
            return;
        }
        if (content.startsWith("HELLMAN_PUBLIC:")) {
            BigInteger peerPublic = new BigInteger(content.substring("HELLMAN_PUBLIC:".length()));
            try {
                SecretKey key = DiffieHellman.computeSharedKey(peerPublic);
                SharedKeys.setHellmanKey(key);
//                System.out.println("[HELLMAN] Shared key computed.");
            } catch (Exception e) {
                System.err.println("[HELLMAN] Error computing shared key: " + e.getMessage());
            }
            return;
        }

        if (EncryptionContext.getMode() == EncryptionMode.RAW) {
            System.out.println(msg.getSender() + ": " + content);
        } else if (EncryptionContext.getMode() == EncryptionMode.RSA) {
            System.out.println(msg.getSender() + " <RSA_ENCRYPTED>: " + content);
            try {
                String decripted = RSA.decryptWithOwnPrivate(content);
                System.out.println(msg.getSender() + " <RSA_DECRYPTED>: " + decripted);
            } catch (Exception e) {
                System.out.println("ERROR DECRYPTING RSA!");
            }
        } else if (EncryptionContext.getMode() == EncryptionMode.HELLMAN) {
            System.out.println(msg.getSender() + " <HELLMAN_ENCRYPTED>: " + content);
            try {
                String decrypted = DiffieHellman.decrypt(content);
                System.out.println(msg.getSender() + " <HELLMAN_DECRYPTED>: " + decrypted);
            } catch (Exception e) {
                System.err.println("[HELLMAN] Error decrypting message: " + e.getMessage());
            }
        }
    }
    @Override
    public Type getPayloadType(StompHeaders headers) {
        return ChatMessage.class;
    }
}

