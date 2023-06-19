package package_card;

public final class PhysicalCard implements Comparable<PhysicalCard>{

    private final ConcreteCard concrCard;
    private final Condition condition;
    
    PhysicalCard(ConcreteCard cc, Condition cond) {
        concrCard = cc;
        condition = cond;
    }

    public ConcreteCard getConcrCard() {
        return concrCard;
    }

    public Condition getCondition() {
        return condition;
    }

    public boolean equals(PhysicalCard pC) {
        if(pC != null)
            return concrCard.equals(pC.concrCard) && condition.equals(pC.condition);
        else
            return false;
    }

    public int compareTo(PhysicalCard pc) {
        int c = concrCard.compareTo(pc.concrCard);
        
        if(c < 0){
            return -1;
        }
        else if(c == 0){
            return condition.compareTo(pc.condition);
        }
        else
            return 1;
    }
}
