package pl.oskarpolak.randomchat.models;

import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import pl.oskarpolak.randomchat.models.commands.KickCommand;
import pl.oskarpolak.randomchat.models.commands.MainCommand;
import pl.oskarpolak.randomchat.models.commands.OnlineCommand;
import pl.oskarpolak.randomchat.models.services.UserListService;

import java.io.IOException;
import java.util.*;

@Component
public class ChatSocket extends TextWebSocketHandler implements WebSocketConfigurer {

    private Queue<String> lastTenMessages = new ArrayDeque<>();

    @Autowired
    UserListService userListService;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(this, "/room")
                                .setAllowedOrigins("*");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        userListService.addUser(new UserModel(session));

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
        userListService.removeUser(userWhoIsExiting);
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

        if(isCommand(sender, message)){
            return;
        }

        addMessageToQue(sender.getNickname() + ": " + message.getPayload());
        sendMessageToAll(sender.getNickname() + ": " + message.getPayload());
    }

    private boolean isCommand(UserModel sender, TextMessage message) {
        if(!message.getPayload().startsWith("/")){
            return false;
        }

        String[] commandArgs = message.getPayload().replace("/", "").split(" ");
        MainCommand mainCommand;
        switch (commandArgs[0]){
            case "kick": {
                mainCommand = new KickCommand(userListService.getUserModels());
                break;
            }
            case "online":{
                 mainCommand = new OnlineCommand(userListService.getUserModels());
                 break;
            }
            default: {
                return false;
            }
        }
        try {
            mainCommand.executeCommand(sender, Arrays.copyOfRange(commandArgs, 1,  commandArgs.length));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void sendMessageToAll(String message) {
        userListService.getUserModels().forEach(s -> {
            try {
                s.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void sendMessageToAllWithoutSender(String message, UserModel sender){
        userListService.getUserModels().stream()
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
         return  userListService.getUserModels().stream()
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
