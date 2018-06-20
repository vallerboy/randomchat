package pl.oskarpolak.randomchat.models.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.TextMessage;
import pl.oskarpolak.randomchat.models.UserModel;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class KickCommand extends MainCommand {


    public KickCommand(List<UserModel> allUsers) {
        super(allUsers);
    }

    @Override
    public boolean executeCommand(UserModel sender, String... args) throws IOException {
        if(args.length < 1) {
            sender.sendServerMessage(new TextMessage("Podałeś za mało argumentów " + info()));
            return false;
        }

        Optional<UserModel> userToKick = findUserByNickname(args[0]);
        if(userToKick.isPresent()){
                userToKick.get().getUserSession().close();
               // sender.sendMessage(new TextMessage("Wyrzucono gracza!"));
        }else{
            sender.sendServerMessage(new TextMessage("Taki nick nie istnieje!"));
        }
        return true;
    }

    @Override
    public String info() {
        return "Use /kick <nickname>";
    }

}
