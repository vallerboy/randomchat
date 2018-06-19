package pl.oskarpolak.randomchat.models.commands;

import pl.oskarpolak.randomchat.models.UserModel;

import java.io.IOException;

public interface Command {
    boolean executeCommand(UserModel sender, String ... args) throws IOException;
    String info();
}
