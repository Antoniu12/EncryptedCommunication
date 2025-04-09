package com.chat_client_B.B.Chat;

import com.chat_client_B.B.DTO.ChatMessage;
import com.chat_client_B.B.EncryptionMethods.RSA.RSA;
import com.chat_client_B.B.Utils.EncryptionContext;
import com.chat_client_B.B.EncryptionMethods.DiffieHellman.DiffieHellman;
import com.chat_client_B.B.Utils.EncryptionMode;
import com.chat_client_B.B.Utils.SharedKeys;
import com.chat_client_B.B.WebSockets.ChatSessionHandler;
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
    private SecretKey aesKey = null;
    public ChatClient() {
        this.name = System.getProperty("client.name", "B");
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
            String message = scanner.nextLine();

            if ("rsa_plz".equalsIgnoreCase(message)) {
                EncryptionContext.setMode(EncryptionMode.RSA);
                EncryptionContext.setFlag(Boolean.TRUE);
                System.out.println("Switched to RSA mode.");
                sendMessage(session, "rsa_plz");
                Thread.sleep(500);
                sendMessage(session, "RSA_KEY:" + RSA.getEncodedPublicKey());
                continue;
            }

            if ("hellman_plz".equalsIgnoreCase(message)) {
                EncryptionContext.setMode(EncryptionMode.HELLMAN);
                System.out.println("Switched to HELLMAN mode.");
                DiffieHellman.generateParams();
                sendMessage(session, "hellman_plz");
                Thread.sleep(200);
                sendMessage(session, "HELLMAN_COMMON:<p>"+ DiffieHellman.getP() + "<g>" + DiffieHellman.getG());
                Thread.sleep(200);
                sendMessage(session, "HELLMAN_PUBLIC:"+ DiffieHellman.getPublicKey());
                continue;
            }

            if ("raw_plz".equalsIgnoreCase(message)) {
                EncryptionContext.setMode(EncryptionMode.RAW);
                SharedKeys.reset();
                System.out.println("Switched to RAW mode.");
                sendMessage(session, "raw_plz");
                continue;
            }

            if (EncryptionContext.getMode() == EncryptionMode.RSA && SharedKeys.getSharedKey() != null) {
                String encrypted = RSA.encryptWithPeerPublic(message, SharedKeys.getSharedKey());
                sendMessage(session, encrypted);
            }else if (EncryptionContext.getMode() == EncryptionMode.HELLMAN && SharedKeys.getHellmanKey() != null) {
                String encrypted = DiffieHellman.encrypt(message);
                sendMessage(session, encrypted);
            }else {
                sendMessage(session, message);
            }
        }
    }
    public void sendMessage(StompSession session, String content){
        ChatMessage msg = new ChatMessage();
        msg.setSender(name);
        msg.setContent(content);
        msg.setTimestamp(LocalDateTime.now().toString());
        session.send("/app/send", msg);
    }
}

