package package_filter;

import java.util.TreeMap;

import package_card.ConcreteCard;
import package_card.Rarity;
import package_collection_card.CollectionCardsList;

public final class RarityFilter extends AbstractFilter{

    public RarityFilter(FilterDecorator d, Rarity r){
        super(d);
        rarity = r;
    }

    @Override
    public TreeMap<ConcreteCard, CollectionCardsList> filter(TreeMap<ConcreteCard, CollectionCardsList> collection){
        for(ConcreteCard cc: collection.keySet()){
            if(!cc.getCard().getRarity().equals(rarity))
                collection.remove(cc);
        }
        return collection;
    }

    private final Rarity rarity;
}
