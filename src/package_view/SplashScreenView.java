package package_view;


import package_server.Server;

import static Utils.constants.StringText.APPNAME;
import static package_view.ViewManager.goToHomepage;
import static package_view.ViewManager.goToLogin;

public class SplashScreenView extends View {

    @Override
    public void show() {
        menu();
        actions(Server.logged ? 1 : 0);
    }

    @Override
    public void menu() {
        System.out.println("### WELCOME TO " + APPNAME + " ###");
    }

    @Override
    public void actions(int action) {
        if (action == 1)
            goToHomepage();
        else
            goToLogin();
    }
}
