package package_card;

public class DeckCard extends AbstractCard{
    
    private final int cost;
    private final Color color;
    private final boolean pitchable;
    private final int attack;
    private final int defence;

    public DeckCard(int id, String n, String t, String e, int co, Color c, boolean p, int a, int d){
        super(id,n,t,e);
        cost = co;
        color = c;
        pitchable = p;
        attack = a;
        defence = d;
    }

    public int getCost() {
        return cost;
    }
    public Color getColor() {
        return color;
    }
    public int getAttack() {
        return attack;
    }
    public int getDefence() {
        return defence;
    }
    public boolean isPitchable(){
        return pitchable;
    }


    public int compareTo(AbstractCard ac) {
        if(ac instanceof DeckCard){
            DeckCard dc = (DeckCard)ac;
            int cmp = this.color.compareTo(dc.color);
            if(cmp < 0)
                return -1;
            else if(cmp==0){
                return super.compareTo(ac);
            }
            else{
                return 1;
            }
        }
        else{
            return super.compareTo(ac);
        }
    }
}
