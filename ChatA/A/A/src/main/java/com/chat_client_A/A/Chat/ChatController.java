package com.chat_client_A.A.Chat;

import com.chat_client_A.A.DTO.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class ChatController {

    @MessageMapping("/send")
    @SendTo("/topic/messages")
    public ChatMessage handle(ChatMessage msg) {
        if (msg.getSender().equalsIgnoreCase("A") && isAppA()) {
            msg.setTimestamp(LocalDateTime.now().toString());
            return msg;
        } else if (msg.getSender().equalsIgnoreCase("B") && isAppB()) {
            msg.setTimestamp(LocalDateTime.now().toString());
            return msg;
        }

        return null;
    }

    private boolean isAppA() {
        return System.getProperty("client.name", "A").equalsIgnoreCase("A");
    }
    private boolean isAppB() {
        return System.getProperty("client.name", "B").equalsIgnoreCase("B");
    }

}