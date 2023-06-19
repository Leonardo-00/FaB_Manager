package package_interaction;

import java.util.Calendar;
import java.util.Date;

public class Rating {
    private final int value;
    private final Date date;

    public Rating(int value) {
        this.value = checkValue(value);
        this.date = Calendar.getInstance().getTime();
    }

    public int getValue() {
        return value;
    }
    public String getDate() {
        return date.toString();
    }

    private int checkValue(int value){
        if(value >= 0 && value <= 5)
            return value;
        else
            if(value < 0)
                return 0;
            else
                return 5;
    }

}
