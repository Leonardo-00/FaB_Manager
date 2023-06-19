package package_view;


import package_user.Contact;

import java.util.ArrayList;

import static package_view.ViewManager.*;

public class AccountView extends View {
    private final String username = server.getUsers().getUserInfo().getUsername();
    private final ArrayList<Contact> contacts = server.getUsers().getUserInfo().getContacts();

    @Override
    public void menu() {
        System.out.println("|| User: " + username);
        System.out.println("|| ProfileImage: #### IMAGE ####");
        System.out.println("""
                Select:\s
                \t1. See user info \t2. Change password\s
                \t3. See transactions \t4. See contacts\s
                \t0. BACK\s
                """);
    }

    @Override
    public void actions(int action) throws ViewException {
        switch (action) {
            case 1 -> seeUserInfo();
            case 2 -> changePassword();
            case 3 -> seeTransactions();
            case 4 -> seeContacts();
            case 0 -> backView();
            default -> errorAction();
        }
    }

    private void changePassword() {
        System.out.print("Insert old password: ");
        String psw = getInput();
        System.out.print("Insert new password: ");
        String newPsw = getInput();
        System.out.print("Insert new password again: ");
        String confirmNewPsw = getInput();
        if (server.checkCurrentPassword(psw)) {
            if (newPsw.equals(confirmNewPsw)) {
                server.setNewPassword(newPsw);
                System.out.println("Password changed!");
            } else {
                System.out.println("ERROR - Passwords are not equal");
            }
        } else {
            System.out.println("ERROR - Invalid Password");
        }
    }

    private void seeUserInfo() {
        System.out.println("Username: " + username);
        System.out.println("Profile Picture: #### IMAGE ####");
    }

    private void seeContacts() {
        System.out.println("# Contacts #");
        for (Contact c : contacts)
            System.out.println(c.getContact() + " " + c.getType());
    }

    /*
    NOT IMPLEMENTED
     */
    private void seeTransactions() {
    }

}
