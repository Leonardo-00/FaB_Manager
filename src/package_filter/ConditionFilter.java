package package_filter;

import package_card.Condition;
import package_card.ConcreteCard;
import package_card.PhysicalCard;
import package_collection_card.*;

import java.util.TreeMap;
import java.util.Map.Entry;

public final class ConditionFilter extends AbstractFilter{


    public ConditionFilter(FilterDecorator d, Condition c) {
        super(d);
        cond = c;
    }

    public TreeMap<ConcreteCard, CollectionCardsList> filter(TreeMap<ConcreteCard, CollectionCardsList> collection){
        for(Entry<ConcreteCard, CollectionCardsList> e: collection.entrySet()){
            for(PhysicalCard pc: e.getValue().getCardsList().keySet()){
                if(pc.getCondition().compareTo(cond) < 0)
                    e.getValue().getCardsList().replace(pc, 0);
            }
        }
        return collection;
    }

    private final Condition cond;
}
