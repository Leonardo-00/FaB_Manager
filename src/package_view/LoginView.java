package package_view;

import static package_view.ViewManager.*;

public class LoginView extends View {

    @Override
    public void menu() {
        System.out.println("""
                Login or Sing In :\s
                \t1. LOGIN \t2. SING IN\s
                \t0. EXIT \s
                """);
    }

    @Override
    public void actions(int action) throws ViewException {
        switch (action) {
            case 1 -> login();
            case 2 -> singIn();
            case 0 -> backView();
            default -> errorAction();
        }
    }

    private void login() {
        System.out.print("Insert UserId: ");
        String user = getInput();
        System.out.print("Insert Password: ");
        String pw = getInput();
        if ((!user.equals("")) && (!pw.equals(""))) {
            if (server.login(user, pw)) {
                System.out.println("Welcome: " + user + " \n");
                goToHomepage();
            } else
                System.out.println("LOGIN FAILED - Invalid credentials\n");
        } else
            System.out.println("Some credentials are missing...\n");
    }

    /*
    NOT IMPLEMENTED
     */
    private void singIn() {
    }

}
