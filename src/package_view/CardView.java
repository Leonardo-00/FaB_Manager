package package_view;

import package_card.*;
import package_collection_card.Collection;

import static package_view.ViewManager.*;

public class CardView extends View {
    private final ConcreteCard card;
    private final AbstractCard abstractCard;
    private final CardManager cm = server.getCards();
    private final Collection cc = server.getCollection();

    public CardView(ConcreteCard card) {
        this.card = card;
        this.abstractCard = card.getCard().getCard();
    }

    @Override
    public void menu() {
        System.out.println("||Card " + abstractCard.getName());
        System.out.println("""
                Select:\s
                \t1. See details \s
                \t2. Modify quantity \t3. insert ad \s
                \t0. BACK
                \s
                """);
    }

    @Override
    public void actions(final int action) throws ViewException {
        switch (action) {
            case 1 -> seeDetails();
            case 2 -> modifyQuantity();
            case 3 -> insertAd();
            case 0 -> backView();
            default -> errorAction();
        }
    }

    private void seeDetails() {
        int id = abstractCard.getID();
        String name = abstractCard.getName();
        String type = abstractCard.getType();
        String effect = abstractCard.getEffect();
        if ((abstractCard instanceof DeckCard)) {
            System.out.print("Id: " + id + " Name: " + name + " Type: " + type + " Effect: " + effect + " DECK-CARD: " + ((DeckCard) abstractCard).getColor() + " attack: " + ((DeckCard) abstractCard).getAttack() + " defense: " + ((DeckCard) abstractCard).getDefence());
        } else if ((abstractCard instanceof WeaponCard)) {
            System.out.print("Id: " + id + " Name: " + name + " Type: " + type + " Effect: " + effect + " WEAPON-CARD: " + ((WeaponCard) abstractCard).getAttack());
        } else if ((abstractCard instanceof EquipmentCard)) {
            System.out.print("Id: " + id + " Name: " + name + " Type: " + type + " Effect: " + effect + " EQUIPMENT-CARD: " + ((EquipmentCard) abstractCard).getDefence());
        }
    }

    private void modifyQuantity() {
        System.out.print("Insert quantity: ");
        int quantity = getInteger();
        try {
            System.out.print("Insert condition: ");
            Condition c = getCondition();
            if (quantity < 0) {
                quantity = Math.abs(quantity);
                PhysicalCard pc = cm.getPhysCard(card, c);
                if (server.getCollection().checkCardAvailability(server.getLoggedUser(), pc, quantity)) {
                    cc.removeCard(pc, quantity);
                }
            } else {
                PhysicalCard pc = cm.getPhysCard(card, c);
                cc.addCard(pc, quantity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    NOT IMPLEMENTED
     */
    private void insertAd() {
    }

}
