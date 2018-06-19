package pl.oskarpolak.randomchat.models.services;


import org.springframework.stereotype.Service;
import pl.oskarpolak.randomchat.models.UserModel;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserListService {
    private List<UserModel> userModels = new ArrayList<>();

    public void addUser(UserModel userModel){
        userModels.add(userModel);
    }

    public void removeUser(UserModel userModel){
        userModels.remove(userModel);
    }

    public List<UserModel> getUserModels() {
        return userModels;
    }
}
