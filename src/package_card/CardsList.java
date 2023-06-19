package package_card;

import java.util.TreeMap;

import Utils.Exceptions.InvalidQuantityRemovalException;


public class CardsList{

    protected final TreeMap<PhysicalCard, Integer> cardList;

    public CardsList(){
        cardList = new TreeMap<>();
    }

    public CardsList(PhysicalCard pc, int q){
        this();
        cardList.put(pc,q);
    }

    public void addCard(PhysicalCard pc, int q) {
        if(cardList.containsKey(pc)) {
            q += cardList.get(pc);
            cardList.remove(pc);
        }
        cardList.put(pc, q);
    }

    public void addCard(PhysicalCard pc){
        addCard(pc,1);
    }

    public void removeCard(PhysicalCard pc, int q) throws InvalidQuantityRemovalException{
        if(cardList.containsKey(pc) && cardList.get(pc) >= q)
            cardList.replace(pc, cardList.get(pc) - q);
        else
            throw new InvalidQuantityRemovalException("Cannot remove more cards than the number owned");
    }

    public void removeCard(PhysicalCard pc) throws InvalidQuantityRemovalException{
        removeCard(pc, 1);
    }

    public TreeMap<PhysicalCard, Integer> getCardsList(){
        return new TreeMap<>(cardList);
    }

    public int getCardQuantity(PhysicalCard pc) {
        if(cardList.get(pc) != null){
            return cardList.get(pc);
        }
        return 0;
    }

}
