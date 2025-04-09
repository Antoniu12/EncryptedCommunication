package com.chat_client_A.A.Chat;

import com.chat_client_A.A.EncryptionMethods.DiffieHellman.DiffieHellman;
import com.chat_client_A.A.EncryptionMethods.RSA.RSA;
import com.chat_client_A.A.Utils.EncryptionContext;
import com.chat_client_A.A.Utils.EncryptionMode;
import com.chat_client_A.A.Utils.SharedKeys;
import com.chat_client_A.A.WebSockets.ChatSessionHandler;
import com.chat_client_A.A.DTO.ChatMessage;
import org.springframework.boot.CommandLineRunner;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

@Component
public class ChatClient implements CommandLineRunner {

    private final String name;
    private final String connectUrl;

    private enum Mode {RAW, ENCRYPTED}

    private Mode mode = Mode.RAW;

    private SecretKey aesKey = null;

    public ChatClient() {
        this.name = System.getProperty("client.name", "A");
        this.connectUrl = System.getProperty("target.url", "http://localhost:8082/chat");
    }

    @Override
    public void run(String... args) throws Exception {
        WebSocketStompClient stompClient = new WebSocketStompClient(
                new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        ChatSessionHandler sessionHandler = new ChatSessionHandler(name);
        StompSession session = stompClient.connect(connectUrl, sessionHandler).get();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            if (EncryptionContext.getMode() == EncryptionMode.RSA && EncryptionContext.getFlag() == Boolean.TRUE) {
                EncryptionContext.setFlag(Boolean.FALSE);
                Thread.sleep(500);
                ChatMessage keyMsg = new ChatMessage();
                keyMsg.setSender(name);
                keyMsg.setContent("RSA_KEY:" + RSA.getEncodedPublicKey());
                keyMsg.setTimestamp(LocalDateTime.now().toString());
                session.send("/app/send", keyMsg);
            }
            if (EncryptionContext.getMode() == EncryptionMode.HELLMAN && EncryptionContext.getStoredSecret() == Boolean.TRUE) {
                EncryptionContext.setStoredSecret(Boolean.FALSE);
                Thread.sleep(500);
                ChatMessage keyMsg = new ChatMessage();
                keyMsg.setSender(name);
                keyMsg.setContent("HELLMAN_PUBLIC:" + DiffieHellman.getPublicKey());
                keyMsg.setTimestamp(LocalDateTime.now().toString());
                session.send("/app/send", keyMsg);
            }
                if (System.in.available() == 0) {
                Thread.sleep(100);
                continue;
            }

            String message = scanner.nextLine();
            ChatMessage msg = new ChatMessage();
            msg.setSender(name);
            msg.setTimestamp(LocalDateTime.now().toString());

            if (EncryptionContext.getMode() == EncryptionMode.RSA && SharedKeys.getSharedKey() != null) {
                String encrypted = RSA.encryptWithPeerPublic(message, SharedKeys.getSharedKey());
                msg.setContent(encrypted);
            } else if (EncryptionContext.getMode() == EncryptionMode.HELLMAN && SharedKeys.getSecretKey() != null) {
                String encrypted = DiffieHellman.encrypt(message);
                msg.setContent(encrypted);
            }else {
                msg.setContent(message);
            }
            session.send("/app/send", msg);
        }

    }
}

