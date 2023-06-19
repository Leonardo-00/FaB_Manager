package package_filter;

import java.util.TreeMap;
import java.util.Map.Entry;
import package_card.ArtVersion;

import package_card.ConcreteCard;
import package_collection_card.CollectionCardsList;

public final class ArtFilter extends AbstractFilter{

    public ArtFilter(FilterDecorator d, ArtVersion a){
        super(d);
        art = a;
    }

    public TreeMap<ConcreteCard, CollectionCardsList> filter(TreeMap<ConcreteCard, CollectionCardsList> collection) {
        for(Entry<ConcreteCard, CollectionCardsList> e: collection.entrySet()){
            if(!e.getKey().getArtVersion().equals(art))
                collection.remove(e.getKey());
        }
        return collection;
    }

    private final ArtVersion art;
}
