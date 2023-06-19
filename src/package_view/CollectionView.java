package package_view;

import package_card.AbstractCard;
import package_card.CardManager;
import package_card.ConcreteCard;
import package_card.Condition;
import package_collection_card.Collection;
import package_collection_card.CollectionCardsList;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import static package_view.ViewManager.*;

public class CollectionView extends View {
    private final Collection c = server.getCollection();
    private final TreeMap<ConcreteCard, CollectionCardsList> userCollection = server.getCollection().getCollection();
    private final CardManager cm = server.getCards();

    @Override
    public void menu() {
        System.out.println("--- Collection ---");
        System.out.println("""
                Select:\s
                \t1. See all cards \t2. See single card \s
                \t3. Add card \s
                \t4. Filter\s
                \t0. BACK
                \s
                """);
    }

    @Override
    public void actions(int action) throws ViewException {
        switch (action) {
            case 1 -> seeAllCards();
            case 2 -> seeSingleCard();
            case 3 -> addCard();
            case 4 -> filter();
            case 0 -> backView();
            default -> errorAction();
        }
    }

    private void seeAllCards() {
        System.out.println("# Your Cards #");
        for (Map.Entry<ConcreteCard, CollectionCardsList> c : userCollection.entrySet()) {
            AbstractCard cc = c.getKey().getCard().getCard();
            System.out.println(cc.getID() + " " + cc.getName());
        }
    }

    private void seeSingleCard() {
        System.out.print("Insert CardId: ");
        int cardId = getInteger();
        boolean find = false;
        for (Map.Entry<ConcreteCard, CollectionCardsList> c : userCollection.entrySet()) {
            if (c.getKey().getId() == cardId) {
                find = true;
                addView(new CardView(c.getKey()));
                break;
            }
        }
        if (find)
            System.out.println("Card added");
        else
            System.out.println("Card not found");
    }

    private void addCard() {
        System.out.print("Insert card name: ");
        String nameCard = getInput();
        ArrayList<String> expansions = cm.getExpCards(nameCard);
        if (expansions.isEmpty())
            System.out.println("Name of this card is not presented in expansions");
        else {
            for (String s : expansions)
                System.out.println(s);
            String expansion = getInput();
            if (expansions.contains(expansion)) {
                TreeMap<Integer, ConcreteCard> cc = cm.getConcreteCards(nameCard, expansion);
                System.out.println("Choose a card: ");
                for (Map.Entry<Integer, ConcreteCard> c : cc.entrySet()) {
                    System.out.println("ID: " + c.getKey() + " Name: " + c.getValue().getCard().getCard().getName());
                }
                System.out.print("Select: ");
                int id = getInteger();
                if (cc.containsKey(id)) {
                    try {
                        System.out.print("Choose the condition: ");
                        Condition condition = getCondition();
                        System.out.print("Insert quantity: ");
                        int q = getInteger();
                        c.addCard(cm.getPhysCard(cc.get(id), condition), q);
                        System.out.println("Cards added");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("ERROR - card not found");
                }
            } else {
                System.out.println("Insert wrong expansion");
            }
        }
    }

    /*
    NOT IMPLEMENTED
     */
    private void filter() {
    }

}
