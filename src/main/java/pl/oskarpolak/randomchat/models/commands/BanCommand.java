package pl.oskarpolak.randomchat.models.commands;

import org.springframework.web.socket.TextMessage;
import pl.oskarpolak.randomchat.models.BanEntity;
import pl.oskarpolak.randomchat.models.UserModel;
import pl.oskarpolak.randomchat.models.repositories.BanRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class BanCommand extends MainCommand {

    private BanRepository banRepository;

    public BanCommand(List<UserModel> allUsers, BanRepository banRepository) {
        super(allUsers);
        this.banRepository = banRepository;
    }

    @Override
    public boolean executeCommand(UserModel sender, String... args) throws IOException {
        Optional<UserModel> userToBan = findUserByNickname(args[0]);
        if(!userToBan.isPresent()){
            sender.sendServerMessage(new TextMessage("Taki nick nie istnieje!"));
            return  false;
        }

        String ip = userToBan.get().getUserSession().getRemoteAddress().getAddress().getHostAddress();

        if (banRepository.existsByIp(ip)) {
            return false;
        }

        BanEntity banEntity = new BanEntity();
        banEntity.setIp(ip);

        banRepository.save(banEntity);
        return true;
    }

    @Override
    public String info() {
        return null;
    }
}
