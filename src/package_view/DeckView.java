package package_view;

import package_deck.Deck;

import static package_view.ViewManager.backView;

public class DeckView extends View {
    private final Deck d;

    public DeckView(Deck d) {
        this.d = d;
    }

    @Override
    public void menu() {
        System.out.println("|| Deck: " + d.getTitle());
        System.out.println("""
                Select:\s
                \t1. See all cards\s
                \t0. BACK\s
                """);
    }

    @Override
    public void actions(int action) throws ViewException {
        switch (action) {
            case 1 -> seeAllCards();
            case 0 -> backView();
            default -> errorAction();
        }
    }

    public void seeAllCards() {
        d.exportDeck();
    }

}
