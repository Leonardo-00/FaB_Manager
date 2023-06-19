package package_view;


import package_card.CardManager;
import package_card.HeroCard;
import package_deck.Deck;
import package_deck.DeckManager;

import java.util.Map;
import java.util.TreeMap;

import Utils.Exceptions.DeckNotFoundException;
import Utils.Exceptions.InvalidDeckAccessException;

import static package_view.ViewManager.*;

public class DecksView extends View {
    private final DeckManager dc = server.getDeckManager();
    private final TreeMap<Integer, Deck> userDecks = server.getDeckManager().getUserDecks();
    private final CardManager cm = server.getCards();
    private final String username = server.getLoggedUser();

    @Override
    public void menu() {
        System.out.println("--- Decks ---");
        System.out.println("""
                Select:\s
                \t1. See all decks \t2. See single deck\s
                \t3. Add deck \t4. Modify deck \t5. Remove deck\s
                \t6. BACK\s
                """);
    }

    @Override
    public void actions(int action) throws ViewException {
        switch (action) {
            case 1 -> seeAllDecks();
            case 2 -> seeSingleDeck();
            case 3 -> addDeck();
            case 4 -> modifyDeck();
            case 5 -> removeDeck();
            case 0 -> backView();
            default -> errorAction();
        }
    }

    private void seeAllDecks() {
        System.out.println("# Your Decks #");
        for (Map.Entry<Integer, Deck> dd : userDecks.entrySet()) {
            System.out.println("ID: " + dd.getKey() + " Deck: " + dd.getValue().getTitle() + " Description: " + dd.getValue().getDescription());
        }
    }

    private void seeSingleDeck() {
        System.out.print("Insert DeckId: ");
        int idDeck = getInteger();
        boolean found = false;
        for (Map.Entry<Integer, Deck> dd : userDecks.entrySet())
            if (dd.getKey() == idDeck && dd.getValue().getOwner().equals(username)) {
                found = true;
                addView(new DeckView(dd.getValue()));
                break;
            }
        if (!found)
            System.out.println("Deck not found");
    }

    private void addDeck() {
        System.out.print("Insert title: ");
        String title = getInput();
        System.out.print("Insert description: ");
        String description = getInput();
        TreeMap<Integer, HeroCard> hc = cm.getHeroCards();
        System.out.println("List of Heroes: ");
        for (Map.Entry<Integer, HeroCard> c : hc.entrySet()) {
            System.out.println("ID: " + c.getKey() + " Name: " + c.getValue().getName());
        }
        System.out.print("Choose a hero: ");
        int id = getInteger();
        if (hc.containsKey(id)) {
            dc.addDeck(title, description, hc.get(id));
            System.out.println("Deck added correctly");
        } else {
            System.out.println("ERROR - hero not found");
        }
    }

    private void removeDeck() {
        System.out.print("Insert DeckId: ");
        int idDeck = getInteger();
        boolean removed = false;
        for (Map.Entry<Integer, Deck> dd : userDecks.entrySet())
            if (dd.getKey() == idDeck && dd.getValue().getOwner().equals(username)) {
                try {
                    dc.removeDeck(idDeck);
                    removed = true;
                    break;
                } catch (DeckNotFoundException | InvalidDeckAccessException e) {
                    e.printStackTrace();
                }
            }
        if (removed)
            System.out.println("Deck removed");
        else
            System.out.println("Deck not found");
    }

    /*
    NOT IMPLEMENTED
     */
    private void modifyDeck() {
    }


}
