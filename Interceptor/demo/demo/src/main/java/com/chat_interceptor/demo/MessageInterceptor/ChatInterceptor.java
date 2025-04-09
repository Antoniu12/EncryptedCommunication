package com.chat_interceptor.demo.MessageInterceptor;

import com.chat_interceptor.demo.MessageInterceptor.RawPayloadLogger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.List;
@Component
public class ChatInterceptor implements CommandLineRunner {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatInterceptor(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        String name = System.getProperty("client.name", "Interceptor");
        String connectUrl = System.getProperty("target.url", "http://localhost:8082/chat");

        WebSocketStompClient stompClient = new WebSocketStompClient(
                new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient())))
        );
        stompClient.setMessageConverter(new org.springframework.messaging.converter.MappingJackson2MessageConverter());

        RawPayloadLogger sessionHandler = new RawPayloadLogger(name);

        stompClient.connect(connectUrl, sessionHandler).get();
    }
}
