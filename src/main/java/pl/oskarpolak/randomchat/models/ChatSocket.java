package pl.oskarpolak.randomchat.models;

import org.apache.catalina.User;
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

    private List<UserModel> userList = new ArrayList<>();
    private Queue<String> lastTenMessages = new ArrayDeque<>();

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(this, "/room")
                                .setAllowedOrigins("*");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        userList.add(new UserModel(session));

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
        //tak samo usuwanie z listy odbywa sie po UserModel a nie sesji!!!!!
        UserModel userWhoIsExiting = findBySession(session);

        sendMessageToAllWithoutSender(userWhoIsExiting.getNickname() + ", odchodzi z czatu!", userWhoIsExiting);
        userList.remove(userWhoIsExiting);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        //Znajdz usermodel po sesji (on tutaj zawsze musi byc)
        //Poniewaz zawsze przed metoda handleTextMessegae najpierw musi wykonac sie
        //metoda afterConnectionEstablished
        UserModel sender = findBySession(session);

        //Jesli prefix wiadomosci rozpoczyna sie od "nickname:"
        // to znaczy ze ktos chce ustawic nick
        //Przydaloby sie zrobic sprawdzanie czy nick jest wolny :)
        if(message.getPayload().startsWith("nickname:")){
            if(sender.getNickname() == null){
                 sender.setNickname(message.getPayload().replace("nickname:", ""));

                 sender.sendMessage(new TextMessage("Ustawiłeś swój nick"));
                 sendMessageToAllWithoutSender(sender.getNickname() + ", dołączył do chatu", sender);
            }else{
                 sender.sendMessage(new TextMessage("Nie możesz zmienić nicku więcej razy!"));
            }
            return;
        }

        //Nie rozsyłaj wiadoomości usera ktory nie ma nicku
        if(sender.getNickname() == null){
            sender.sendMessage(new TextMessage("Najpierw ustal nick!"));
            return;
        }

        addMessageToQue(sender.getNickname() + ": " + message.getPayload());
        sendMessageToAll(sender.getNickname() + ": " + message.getPayload());
    }

    private void sendMessageToAll(String message) {
        userList.forEach(s -> {
            try {
                s.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void sendMessageToAllWithoutSender(String message, UserModel sender){
        userList.stream()
                .filter(s -> !s.equals(sender))
                .forEach(s -> {
                    try {
                        s.sendMessage(new TextMessage(message));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    //Funkcja szukajaca usera po sesji (pamietamy ze lista jest teraz lista UserModeli a nie sesji)
    //A w metodach wbudowanych w WebSocket przychodzi tylko sesja
    private UserModel findBySession(WebSocketSession webSocketSession){
         return userList.stream()
                 .filter(s -> s.getUserSession().getId().equals(webSocketSession.getId()))
                 .findAny()
                 .orElseThrow(IllegalStateException::new);
    }

    private void addMessageToQue(String message){
        if(lastTenMessages.size() >= 10){
            lastTenMessages.poll();
        }

        lastTenMessages.offer(message);
    }
}
