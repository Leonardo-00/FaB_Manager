package package_view;

import package_server.Server;

import java.util.Stack;

public class ViewManager {
    static final Server server = Server.getInstance();
    private static Stack<View> views = null;

    public ViewManager() {
        views = new Stack<>();
        addView(new SplashScreenView());
    }

    public static void main(String[] args) {
        new ViewManager();
        server.shutDown();
    }

    static void addView(final View view) {
        views.push(view);
        showView();
    }

    static void backView() throws ViewException {
        if (views.empty())
            throw new ViewException();
        else {
            views.pop();
            if (!views.empty())
                showView();
        }
    }

    static void goToLogin() {
        views.clear();
        addView(new LoginView());
    }

    static void goToHomepage() {
        views.clear();
        addView(new HomepageView());
    }

    private static void showView() {
        views.peek().show();
    }




}
