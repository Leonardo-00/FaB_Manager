package package_filter;

import java.util.TreeMap;

import package_card.ConcreteCard;
import package_collection_card.CollectionCardsList;

public final class ExpansionFilter extends AbstractFilter{

    public ExpansionFilter(FilterDecorator d, String e){
        super(d);
        expansion = e;
    }

    @Override
    public TreeMap<ConcreteCard, CollectionCardsList> filter(TreeMap<ConcreteCard, CollectionCardsList> collection) {
        for(ConcreteCard cc: collection.keySet()){
            if(!cc.getCard().getExpansion().equals(expansion))
                collection.remove(cc);
        }
        return collection;
    }

    private final String expansion;
}
