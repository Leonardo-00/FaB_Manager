package package_filter;

import java.util.TreeMap;

import package_card.ConcreteCard;
import package_collection_card.CollectionCardsList;

public final class SearchFilter extends AbstractFilter{

    public SearchFilter(FilterDecorator d, String s){
        super(d);
        string = s;
    }

    public TreeMap<ConcreteCard, CollectionCardsList> filter(TreeMap<ConcreteCard, CollectionCardsList> collection){
        for(ConcreteCard cc: collection.keySet()){
            if(!cc.getCard().getCard().getName().contains(string))
                collection.remove(cc);
        }
        return collection;
    }

    private final String string;
}
