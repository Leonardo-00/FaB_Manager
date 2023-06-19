package package_user;

import java.util.ArrayList;

public class UserInfo {
    private final String username;
    private String password;
    private final ArrayList<Contact> contacts;

    public UserInfo(String name, String pw) {
        username = name;
        password = pw;
        contacts = new ArrayList<>();
    }

    public UserInfo(UserInfo c){
        username = c.username;
        password = c.password;
        contacts = new ArrayList<>(c.contacts);
    }

    public String getUsername() {
        return username;
    }

    String getPassword(){
        return password;
    }

    public void setPassword(String pw){
        password = pw;
    }

    public ArrayList<Contact> getContacts() {
        return contacts;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Username: ").append(username);
        for (Contact c : contacts) {
            sb.append("\n\n").append("Contact type: ").append(c.getType()).append("  -  Contact: ").append(c.getContact());
        }
        return sb.toString();
    }

    public void addContact(Contact c) {
        contacts.add(c);
    }

}
