package package_filter;

import java.util.TreeMap;

import package_card.ConcreteCard;
import package_collection_card.CollectionCardsList;

interface FilterDecorator {

    TreeMap<ConcreteCard, CollectionCardsList> filter(TreeMap<ConcreteCard, CollectionCardsList> collection);

}
