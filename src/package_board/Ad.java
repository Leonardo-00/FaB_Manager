package package_board;

import java.util.Calendar;
import java.util.Date;

import Utils.constants.AdState;

public abstract class Ad {

    protected int id;
    protected final String owner;
    protected final Date dateOfCreation;
    int quantity;
    AdState state;

    Ad(int id, String u, int q) {
        this(id, u, q, Calendar.getInstance().getTime(), AdState.active);
    }

    Ad(int id, String u, int q, Date date, AdState s){
        this.id = id;
        owner = u;
        quantity = q;
        dateOfCreation = date;
        state = s;
    }

    public String getOwner() {
        return owner;
    }

    public Date getDateOfCreation() {
        return dateOfCreation;
    }

    public AdState getState() {
        return state;
    }

    public int getQuantity() {
        return quantity;
    }

    public abstract Ad getCopy();

    public int getId() {
        return id;
    }

}