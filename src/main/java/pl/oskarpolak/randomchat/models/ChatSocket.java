package pl.oskarpolak.randomchat.models;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

@Component
public class ChatSocket extends TextWebSocketHandler implements WebSocketConfigurer {

    private List<WebSocketSession> userList = new ArrayList<>();
    private Queue<String> lastTenMessages = new ArrayDeque<>();

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(this, "/room")
                                .setAllowedOrigins("*");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        userList.add(session);

        lastTenMessages.forEach(s -> {
            try {
                session.sendMessage(new TextMessage(s));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        userList.remove(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        addMessageToQue(message.getPayload());

        userList.forEach(s -> {
            try {
                s.sendMessage(new TextMessage(message.getPayload()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void addMessageToQue(String message){
        if(lastTenMessages.size() >= 10){
            lastTenMessages.poll();
        }

        lastTenMessages.offer(message);
    }
}
