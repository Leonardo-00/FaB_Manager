package package_board;

import java.util.Date;

import Utils.constants.AdState;
import package_card.PhysicalCard;

public class Offer extends Ad {

    private final PhysicalCard pc;

    public Offer(int id, String u, PhysicalCard pc, int quantity) {
        super(id, u, quantity);
        this.pc = pc;
    }

    public Offer(int id, String u, int q, Date date, AdState s, PhysicalCard pc){
        super(id, u,q,date,s);
        this.pc = pc;
    }

    public PhysicalCard getPc() {
        return pc;
    }

    public Offer getCopy() {
        return new Offer(id, owner, pc, quantity);
    }

}
