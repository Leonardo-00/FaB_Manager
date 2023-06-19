package package_card;

import java.util.ArrayList;
import java.util.TreeMap;

import Utils.Exceptions.CardNotFoundException;

public class CardManager{

    private final TreeMap<Integer, AbstractCard> abstractCards;
    private final ArrayList<ExpansionCard> expansionCards;
    private final TreeMap<Integer, ConcreteCard> concreteCards;
    private final ArrayList<PhysicalCard> physicalCards;
    
    public CardManager(){
        abstractCards = new TreeMap<>();
        expansionCards = new ArrayList<>();
        concreteCards = new TreeMap<>();
        
        physicalCards = new ArrayList<>();
        for(ConcreteCard cc: concreteCards.values()){
            for(Condition c: Condition.values()){
                physicalCards.add(new PhysicalCard(cc,c));
            }
        }
    }

    public CardManager(TreeMap<Integer, AbstractCard> aCards, ArrayList<ExpansionCard> eCards, TreeMap<Integer, ConcreteCard> cCards){

        abstractCards = aCards;
        expansionCards = eCards;
        concreteCards = cCards;
        physicalCards = new ArrayList<>();
        for(ConcreteCard cc: concreteCards.values()){
            for(Condition c: Condition.values()){
                physicalCards.add(new PhysicalCard(cc,c));
            }
        }
    }

    public ArrayList<AbstractCard> getAbstractCards(){
        return new ArrayList<>(abstractCards.values());
    }

    public ArrayList<ConcreteCard> getConcreteCards(){
        return new ArrayList<>(concreteCards.values());
    }

    public TreeMap<Integer, ConcreteCard> getConcreteCards(String name, String expansion){
        TreeMap<Integer, ConcreteCard> cCards = new TreeMap<>();
        for(ConcreteCard cc: concreteCards.values()){
            if(cc.getCard().getCard().name.equals(name) && cc.getCard().getExpansion().equals(expansion))
                cCards.put(cc.getId(), cc);
        }
        return cCards;
    }

    public ArrayList<String> getExpCards(String name){
        ArrayList<String> tmp = new ArrayList<>();
        for(ExpansionCard ec: expansionCards){
            if(ec.getCard().getName().equals(name))
                tmp.add(ec.getExpansion());
        }
        return tmp;
    }

    public PhysicalCard getPhysCard(ConcreteCard cc, Condition cond) {
        PhysicalCard tmp = new PhysicalCard(cc,cond);
        for(PhysicalCard pC: physicalCards){
            if(pC.equals(tmp))
                return pC;
        }
        return null;
    }

    public ArrayList<PhysicalCard> getPhysCards(int id) {
        ArrayList<PhysicalCard> tmp = new ArrayList<>();
        for(PhysicalCard pC: physicalCards){
            if(pC.getConcrCard().getId() == id)
                tmp.add(pC);
        }
        return tmp;
    }

    public Condition[] getConditions(){
        return Condition.values();
    }

    public AbstractCard getAbstractCard(int id){
        return abstractCards.get(id);
    }

    public ConcreteCard getConcreteCard(int id){
        return concreteCards.get(id);
    }

    public AbstractCard getAbstractCard(String cardName) throws CardNotFoundException{
        for(AbstractCard ac: abstractCards.values()){
            if(ac.getName().equals(cardName))
                return ac;
        }
        throw new CardNotFoundException();
    }

    public AbstractCard getAbstractCard(String card, Color c) throws CardNotFoundException{
        for(AbstractCard ac: abstractCards.values()){
            if(ac.getName().equals(card) && ac instanceof DeckCard && ((DeckCard)ac).getColor().equals(c))
                return ac;
        }
        throw new CardNotFoundException();
    }

    public TreeMap<Integer, HeroCard> getHeroCards(){
        TreeMap<Integer, HeroCard> heroCards = new TreeMap<>();
        for(AbstractCard ac: abstractCards.values()){
            if(ac instanceof HeroCard)
                heroCards.put(ac.getID(), (HeroCard)ac);
        }
        return heroCards;
    }

    public ArrayList<HeroCard> getHeroCardsList(){
        ArrayList<HeroCard> heroCards = new ArrayList<>();
        for(AbstractCard ac: abstractCards.values()){
            if(ac instanceof HeroCard)
                heroCards.add((HeroCard)ac);
        }
        return heroCards;
    }
}

