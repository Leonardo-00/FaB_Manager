package package_view;

import static package_view.ViewManager.*;

public class HomepageView extends View {

    @Override
    public void menu() {
        System.out.println("|--- HOMEPAGE ---|");
        System.out.println("""
                Select:\s
                \t1. See your Collection \t2. See your Decks \t3. Visit the Market \s
                \t4. Account\s
                \t0. EXIT
                \s
                """);
    }

    @Override
    public void actions(int action) throws ViewException {
        switch (action) {
            case 1 -> addView(new CollectionView());
            case 2 -> addView(new DecksView());
            case 3 -> addView(new MarketView());
            case 4 -> addView(new AccountView());
            case 0 -> backView();
            default -> errorAction();
        }
    }

}
