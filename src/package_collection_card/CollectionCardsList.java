package package_collection_card;

import package_card.CardsList;
import package_card.PhysicalCard;

public class CollectionCardsList extends CardsList{

    public CollectionCardsList(){
        super();
    }

    public CollectionCardsList(PhysicalCard pc, int q){
        this();
        cardList.put(pc, q);
    }

    public int getTotalQuantity(){
        int q = 0;
        for(PhysicalCard pc: cardList.keySet()){
            q+=cardList.get(pc);
        }
        return q;
    }
}