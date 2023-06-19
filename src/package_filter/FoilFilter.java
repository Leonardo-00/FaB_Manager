package package_filter;

import java.util.TreeMap;

import package_card.ConcreteCard;
import package_collection_card.CollectionCardsList;

public final class FoilFilter extends AbstractFilter{

    public FoilFilter(FilterDecorator d, String f){
        super(d);
        foil = f;
    }

    public TreeMap<ConcreteCard, CollectionCardsList> filter(TreeMap<ConcreteCard, CollectionCardsList> collection){
        for(ConcreteCard cc: collection.keySet()){
            if(!cc.getFoilType().equals(foil))
                collection.remove(cc);
        }

        return collection;
    }

    private final String foil;
}
