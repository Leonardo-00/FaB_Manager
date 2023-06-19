package package_board;

import java.util.Date;

import Utils.constants.AdState;
import package_card.ConcreteCard;
import package_card.Condition;

public final class Request extends Ad {

    private final ConcreteCard cc;
    private final Condition minimalCondition;

    public Request(int id, String u, ConcreteCard c, Condition cond, int quantity) {
        super(id, u, quantity);
        this.cc = c;
        this.minimalCondition = cond;
    }

    public Request(int id, String u, int q, Date d, ConcreteCard cc, Condition c, AdState s){
        super(id, u, q, d, s);
        this.cc = cc;
        minimalCondition = c;
    }

    public ConcreteCard getCc() {
        return cc;
    }

    public Condition getMinimalCondition() {
        return minimalCondition;
    }

    public Request getCopy() {
        return new Request(id, owner, cc, minimalCondition, quantity);
    }

}
