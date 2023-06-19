package package_card;

public abstract class AbstractCard implements Comparable<AbstractCard>{
    protected final int id;
    protected final String name;
    protected final String type;
    protected final String effect;

    public AbstractCard(int id, String n, String t, String e) {
        this.id = id;
        this.name = n;
        this.type = t;
        this.effect = e;
    }

    public int getID(){
        return id;
    }
    public String getName() {
        return name;
    }
    public String getType() {
        return type;
    }
    public String getEffect() {
        return effect;
    }

    public int compareTo(AbstractCard rhs){
        int cmp = this.name.compareTo(rhs.name);
        if(cmp < 0 || (cmp==0&&(this.id<rhs.id)))
            return -1;
        else
            if(cmp==0&&(this.id==rhs.id))
                return 0;
            else
                return 1;
    }
}
