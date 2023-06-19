package package_card;

public class WeaponCard extends AbstractCard{

    private final int attack;

    public WeaponCard(int id, String n, String t, String e, int a){
        super(id,n,t,e);
        this.attack = a;
    }

    public int getAttack(){
        return attack;
    }
    
}
