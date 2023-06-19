package package_card;

public class EquipmentCard extends AbstractCard{

    private int defence;

    public EquipmentCard(int id, String n, String t, String e, int d){
        super(id,n,t,e);
        this.defence = d;
    }

    public int getDefence(){
        return defence;
    }    
}
