package com.chat_client_A.A.WebSockets;

import com.chat_client_A.A.DTO.ChatMessage;
import com.chat_client_A.A.EncryptionMethods.DiffieHellman.DiffieHellman;
import com.chat_client_A.A.Utils.EncryptionContext;
import com.chat_client_A.A.Utils.EncryptionMode;
import com.chat_client_A.A.EncryptionMethods.RSA.RSA;
import com.chat_client_A.A.Utils.SharedKeys;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import javax.crypto.SecretKey;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatSessionHandler extends StompSessionHandlerAdapter {
    private final String name;
    private StompSession session;

    public ChatSessionHandler(String name) {
        this.name = name;
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        this.session = session;
        session.subscribe("/topic/messages", this);
        System.out.println("You secured a connection! Start typing");
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        ChatMessage msg = (ChatMessage) payload;
        if (msg.getSender().equals(name)) return;

        String content = msg.getContent().trim();

        if (msg.getSender().equals("B") && content.equalsIgnoreCase("rsa_plz")) {
            EncryptionContext.setMode(EncryptionMode.RSA);
            SharedKeys.reset();
            System.out.println("Switched to RSA mode!");
            EncryptionContext.setFlag(Boolean.TRUE);
            return;
        }
        if (msg.getSender().equals("B") && content.equalsIgnoreCase("raw_plz")) {
            EncryptionContext.setMode(EncryptionMode.RAW);
            SharedKeys.reset();
            System.out.println("Switched to raw mode!");
            return;
        }
        if (msg.getSender().equals("B") && content.equalsIgnoreCase("hellman_plz")) {
            EncryptionContext.setMode(EncryptionMode.HELLMAN);
            SharedKeys.reset();
            System.out.println("Switched to HELLMAN mode!");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        if (content.startsWith("RSA_KEY:") && EncryptionContext.getMode() == EncryptionMode.RSA) {
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
        if (EncryptionContext.getMode() == EncryptionMode.HELLMAN) {
            if (content.startsWith("HELLMAN_COMMON:")) {
                Pattern pattern = Pattern.compile("HELLMAN_COMMON:<p>(\\d+)<g>(\\d+)");
                Matcher matcher = pattern.matcher(content);
                if (matcher.matches()) {
                    BigInteger p = new BigInteger(matcher.group(1));
                    BigInteger g = new BigInteger(matcher.group(2));
                    DiffieHellman.init(p, g);
//                    System.out.println("[HELLMAN] Received and initialized p: " + p + " and g: " + g);
                }
                return;
            }
            else if (content.startsWith("HELLMAN_PUBLIC:")) {
                BigInteger peerPublic = new BigInteger(content.substring("HELLMAN_PUBLIC:".length()));
                try {
                    SecretKey key = DiffieHellman.computeSharedKey(peerPublic);
                    SharedKeys.setSecretKey(key);
//                    System.out.println("[HELLMAN] Shared key computed.");
                    EncryptionContext.setStoredSecret(Boolean.TRUE);
                } catch (Exception e) {
                    System.err.println("[HELLMAN] Error computing shared key: " + e.getMessage());
                }
                return;
            }

        }

        EncryptionMode mode = EncryptionContext.getMode();

        if (mode == EncryptionMode.RAW) {
            System.out.println(msg.getSender() + ": " + content);
        } else if (mode == EncryptionMode.RSA && EncryptionContext.getFlag() == Boolean.FALSE) {
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
