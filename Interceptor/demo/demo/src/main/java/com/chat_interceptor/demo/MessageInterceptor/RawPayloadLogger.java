package com.chat_interceptor.demo.MessageInterceptor;

import com.chat_interceptor.demo.DTO.ChatMessage;
import com.chat_interceptor.demo.MitMSimulations.RSA.RSA;
import com.chat_interceptor.demo.Utils.EncryptionContext;
import com.chat_interceptor.demo.Utils.EncryptionMode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import java.lang.reflect.Type;
import java.security.PublicKey;
import java.time.LocalDateTime;

public class RawPayloadLogger extends StompSessionHandlerAdapter {
    private final String name;
    private final ObjectMapper mapper = new ObjectMapper();
    private StompSession stompSession;

    public RawPayloadLogger(String name) {
        this.name = name;
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        this.stompSession = session;
        System.out.println("[" + name + "] Connected to WebSocket broker.");
        session.subscribe("/topic/messages", this);
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        try {
            String raw = payload instanceof byte[] ? new String((byte[]) payload) : payload.toString();
            ChatMessage intercepted = mapper.readValue(raw, ChatMessage.class);

            String sender = intercepted.getSender();
            String content = intercepted.getContent();
            if (content.startsWith("RSA_KEY:")) {
                return;
            }
            if (EncryptionContext.getMode() == EncryptionMode.RAW) {
                System.out.println("[" + name + "] RAW message intercepted from [" + sender + "]: " + content);
            } else if (EncryptionContext.getMode() == EncryptionMode.RSA) {
                try {
                    String decrypted = RSA.decryptWithOwnPrivate(content);
                    System.out.println("[" + name + "] RSA message intercepted from [" + sender + "] (decrypted): " + decrypted);
                } catch (Exception e) {
                    System.out.println("[" + name + "] RSA message from [" + sender + "] could not be decrypted.");
                }
            }

            if ("rsa_plz".equalsIgnoreCase(content)) {
                EncryptionContext.setMode(EncryptionMode.RSA);

                Thread.sleep(100);

                ChatMessage forgedB = new ChatMessage();
                forgedB.setSender("A");
                forgedB.setContent("RSA_KEY:" + RSA.getEncodedPublicKey());
                forgedB.setTimestamp(LocalDateTime.now().toString());
                sendForgedMessage(forgedB);
                System.out.println("[" + name + "] Sent forged RSA key to B as A.");

                ChatMessage forgedA = new ChatMessage();
                forgedA.setSender("B");
                forgedA.setContent("RSA_KEY:" + RSA.getEncodedPublicKey());
                forgedA.setTimestamp(LocalDateTime.now().toString());
                sendForgedMessage(forgedA);
                System.out.println("[" + name + "] Sent forged RSA key to A as B.");
            }

        } catch (Exception e) {
            System.err.println("[" + name + "] Failed to process intercepted message: " + e.getMessage());
        }
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return Object.class;
    }

    public void sendForgedMessage(ChatMessage message) {
        try {
            if (stompSession != null && stompSession.isConnected()) {
                stompSession.send("/app/send", message);
            } else {
                System.err.println("[" + name + "] STOMP session is not connected.");
            }
        } catch (Exception e) {
            System.err.println("[" + name + "] Failed to send forged message: " + e.getMessage());
        }
    }
}
