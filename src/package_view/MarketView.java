package package_view;


import package_board.Board;
import package_board.Offer;
import package_board.Request;
import package_card.CardManager;
import package_card.ConcreteCard;
import package_card.Condition;
import package_card.PhysicalCard;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import static package_view.ViewManager.*;

public class MarketView extends View {
    private final Board mn = server.getBoard();
    private final TreeMap<Integer, Offer> offers = server.getBoard().getOffersList();
    private final TreeMap<Integer, Request> requests = server.getBoard().getRequestsList();
    private final CardManager cm = server.getCards();
    private final String username = server.getLoggedUser();

    @Override
    public void menu() {
        System.out.println("--- Market ---");
        System.out.println("""
                Select:\s
                \t1. See offers \t2. See requests \s
                \t3. Add Ad\s
                \t4. Filter\s
                \t0. BACK
                \s
                """);
    }

    @Override
    public void actions(int action) throws ViewException {
        switch (action) {
            case 1 -> seeOffers();
            case 2 -> seeRequests();
            case 3 -> addAd();
            case 4 -> filter();
            case 0 -> backView();
            default -> errorAction();
        }
    }

    private void addAd() {
        System.out.println("Is an Offer = 1. or a Request = 2. ?");
        System.out.print("Select: ");
        switch (getInteger()) {
            case 1 -> addOffer();
            case 2 -> addRequest();
            default -> errorAction();
        }
    }

    private void addOffer() {
        System.out.print("Insert card name: ");
        String nameCard = getInput();
        System.out.print("Insert expansion card: ");
        ArrayList<String> expansions = cm.getExpCards(nameCard);
        if (expansions.isEmpty())
            System.out.println("Name of this card is not presented in expansions");
        else {
            System.out.println("Choose an expansion: ");
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
                        PhysicalCard pc = cm.getPhysCard(cc.get(id), condition);
                        System.out.print("Insert quantity: ");
                        int quantity = getInteger();
                        if (mn.addOffer(username, pc, quantity)) {
                            System.out.println("Offer added");
                        } else {
                            System.out.println("ERROR - quantity not available");
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    System.out.println("ERROR - id card not found");
                }
            } else {
                System.out.println("ERROR - Incorrect expansion");
            }
        }
    }

    private void addRequest() {
        System.out.print("Insert card name: ");
        String nameCard = getInput();
        System.out.print("Insert expansion card: ");
        ArrayList<String> expansions = cm.getExpCards(nameCard);
        if (expansions.isEmpty())
            System.out.println("Name of this card is not presented in expansions");
        else {
            System.out.println("Choose an expansion: ");
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
                        System.out.print("Choose the minimal condition: ");
                        Condition condition = getCondition();
                        System.out.print("Insert quantity: ");
                        int quantity = getInteger();
                        mn.addRequest(username, cc.get(id), condition, quantity);
                        System.out.println("Request added");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    System.out.println("ERROR - id card not found");
                }
            } else {
                System.out.println("ERROR - Incorrect expansion");
            }
        }
    }

    private void seeOffers() {
        for (Map.Entry<Integer, Offer> r : offers.entrySet()) {
            Offer rr = r.getValue();
            String nameCard = rr.getPc().getConcrCard().getCard().getCard().getName();
            System.out.println("Offers-> Card:" + nameCard + " MinCond: " + rr.getPc().getCondition().name() + " Quantity: " + rr.getQuantity() + " Owner: " + rr.getOwner() + " Date: " + rr.getDateOfCreation());
        }
    }

    private void seeRequests() {
        for (Map.Entry<Integer, Request> r : requests.entrySet()) {
            Request rr = r.getValue();
            String nameCard = rr.getCc().getCard().getCard().getName();
            System.out.println("Request-> Card:" + nameCard + " MinCond: " + rr.getMinimalCondition() + " Quantity: " + rr.getQuantity() + " Owner: " + rr.getOwner() + " Date: " + rr.getDateOfCreation());
        }
    }


    /*
    NOT IMPLEMENTED
     */
    private void filter() {
    }

}
