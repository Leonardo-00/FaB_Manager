package package_filter;

import java.util.TreeMap;

import package_card.ConcreteCard;
import package_collection_card.CollectionCardsList;

public final class TypeFilter extends AbstractFilter{

    public TypeFilter(FilterDecorator d, String s){
        super(d);
        type = s;
    }

    public TreeMap<ConcreteCard, CollectionCardsList> filter(TreeMap<ConcreteCard, CollectionCardsList> collection){
        for(ConcreteCard cc: collection.keySet()){
            if(!cc.getCard().getCard().getType().contains(type))
                collection.remove(cc);
        }
        return collection;
    }

    private final String type;
}
