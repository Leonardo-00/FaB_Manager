package package_filter;

import java.util.TreeMap;

import package_card.ConcreteCard;
import package_collection_card.CollectionCardsList;

public final class DefaultFilter implements FilterDecorator{

    public TreeMap<ConcreteCard, CollectionCardsList> filter(TreeMap<ConcreteCard, CollectionCardsList> collection){return collection;}
}
