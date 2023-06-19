package package_filter;

import java.util.TreeMap;

import package_card.ConcreteCard;
import package_collection_card.CollectionCardsList;

public abstract class AbstractFilter implements FilterDecorator{

    public abstract TreeMap<ConcreteCard, CollectionCardsList> filter(TreeMap<ConcreteCard, CollectionCardsList> collection);

    public AbstractFilter(FilterDecorator d){
        decorator = d;
    }

    protected final FilterDecorator decorator;
}
