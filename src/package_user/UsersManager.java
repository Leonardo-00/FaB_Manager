package package_user;

import java.util.TreeSet;

public class UsersManager{

    private TreeSet<String> usernames;
    private UserInfo cUI;

    public UsersManager(TreeSet<String> usersList){
        usernames = usersList;
    }

    public void setLoggedUserInfos(UserInfo info) {
        cUI = info;
    }

    public boolean checkCurrentPassword(String pw){
        return cUI.getPassword().equals(pw);
    }

    public void setNewPassword(String pw){
        cUI.setPassword(pw);
    }

    public UserInfo getUserInfo() {
        return new UserInfo(cUI);
    }

    public boolean exists(String username){
        return usernames.contains(username);
    }
}
