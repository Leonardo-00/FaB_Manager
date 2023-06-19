package package_card;

import java.util.ArrayList;

public class HeroCard extends AbstractCard{

    private final String talent;
    private final String Class;
    private final int health;
    private final int intellect;
    private final boolean adult;
    private final ArrayList<String> legalTypes;

    public HeroCard(int id, String n, String Class, String talent, String t, String e, int h, int i, boolean a, ArrayList<String> lt){
        super(id,n,t,e);
        this.Class = Class;
        this.talent = talent;
        this.health = h;
        this.intellect = i;
        this.adult = a;
        this.legalTypes = lt;
    }
    public String getTalent(){
        return talent;
    }
    public String getHeroClass(){
        return Class;
    }
    public int getHealth(){
        return health;
    }
    public int getIntellect(){
        return intellect;
    }
    public boolean isAdult(){
        return adult;
    }
    public ArrayList<String> getLegalTypes(){
        return new ArrayList<>(legalTypes);
    }
    
}
