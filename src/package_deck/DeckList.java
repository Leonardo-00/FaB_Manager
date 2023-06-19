package package_deck;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import Utils.Exceptions.IllegalCardAdditionException;
import Utils.constants.Rules;
import package_card.AbstractCard;
import package_card.DeckCard;
import package_card.EquipmentCard;
import package_card.HeroCard;
import package_card.WeaponCard;

public class DeckList{

    private HeroCard hero;
    private boolean cc;
    private TreeSet<EquipmentCard> equipmentList;
    private TreeMap<WeaponCard, Integer> weaponList;
    private TreeMap<DeckCard, Integer> deckCards;

    DeckList(HeroCard hero, TreeSet<EquipmentCard> equip, TreeMap<WeaponCard, Integer> weapons, TreeMap<DeckCard, Integer> deckCards){
        cc = ((HeroCard)(this.hero = hero)).isAdult();
        equipmentList = (equip != null)?equip:new TreeSet<>();
        weaponList = (weapons != null)?weapons:new TreeMap<>();
        this.deckCards = (deckCards != null)?deckCards:new TreeMap<>();
    }

    DeckList(HeroCard hero){
        this(hero, null, null, null);
    }

    DeckList(DeckList list){
        hero = list.hero;
        cc = list.cc;
        equipmentList = new TreeSet<>(list.equipmentList);
        weaponList = new TreeMap<>(list.weaponList);
        deckCards = new TreeMap<>(list.deckCards);
    }

    void addCard(AbstractCard card, int quantity) throws IllegalCardAdditionException{
        if(card instanceof DeckCard)
            addDeckCard((DeckCard)card, quantity);
        else if(card instanceof WeaponCard)
            addWeapon((WeaponCard)card, quantity);
        else if(card instanceof EquipmentCard)
            addEquipment((EquipmentCard)card);
    }

    private void addDeckCard(DeckCard dc, int quantity) throws IllegalCardAdditionException{
        if(!checkCardLegality(dc)){
            throw new IllegalCardAdditionException("Trying to add to the deck an illegal card for the hero");
        }
        if(!checkDeckLimit(dc, quantity)){
            throw new IllegalCardAdditionException("Trying to add to the deck a card that would make it surpass the size limit");
        }
        if(!checkSpecializationCard(dc)){
            throw new IllegalCardAdditionException("Trying to add to the deck a specialization card for a different hero");
        }
        if(!checkMentorCard(dc)){
            throw new IllegalCardAdditionException("Trying to add a mentor card to a constructed format deck");
        }
        int max = Rules.BLITZ_CARD_QUANTITY_LIMIT;
        if(cc){
            max = Rules.CC_CARD_QUANTITY_LIMIT;
        }
        int q = deckCards.getOrDefault(dc, 0);
        if(q+quantity > max)
            throw new IllegalCardAdditionException("Trying to add to the deck a card that would exceed the card limit");

        if(q>0)
            deckCards.replace(dc, q+quantity);
        else
            deckCards.put(dc, quantity);
    }

    private void addWeapon(WeaponCard wc, int quantity) throws IllegalCardAdditionException{
        if(!checkCardLegality(wc))
            throw new IllegalCardAdditionException("Trying to add to the deck an illegal weapon for the hero");
        if(!checkEquipmentLimit(wc, quantity))
            throw new IllegalCardAdditionException("Trying to add to the deck a weapon that would make it surpass the size limit");
        int max = Rules.WEAPON_QUANTITY_LIMIT;
        int q = weaponList.getOrDefault(wc, 0);
        if(q+quantity > max)
            throw new IllegalCardAdditionException("Trying to add to the deck a weapon that would exceed the card limit");
        if(q>0)
            weaponList.replace(wc, q+quantity);
        else
            weaponList.put(wc, quantity);
    }

    private void addEquipment(EquipmentCard ec) throws IllegalCardAdditionException{
        if(!checkCardLegality(ec))
            throw new IllegalCardAdditionException("Trying to add to the deck an illegal equipment for the hero");
        if(!checkEquipmentLimit(ec, 1))
            throw new IllegalCardAdditionException("Trying to add to the deck an equipment that would make it surpass the size limit");
        if(equipmentList.contains(ec))
            throw new IllegalCardAdditionException("Trying to add to the deck an equipment that would exceed the card limit");
        equipmentList.add(ec);
    }

    boolean removeCard(AbstractCard card, int quantity){
        if(card instanceof DeckCard)
            return removeDeckCard((DeckCard)card, quantity);
        else if(card instanceof WeaponCard)
            return removeWeapon((WeaponCard)card, quantity);
        else
            return removeEquipment((EquipmentCard)card);
    }

    private boolean removeDeckCard(DeckCard dc, int quantity){
        if(!deckCards.containsKey(dc)){
            return false;
        }
        int tmp = deckCards.get(dc)-quantity;
        if(tmp>0){
            deckCards.remove(dc);
            deckCards.put(dc, tmp);
            return true;
        }
        else{
            return false;
        }
        
    }

    private boolean removeWeapon(WeaponCard wc, int quantity){
        if(!weaponList.containsKey(wc)){
            return false;
        }
        int tmp = weaponList.get(wc)-quantity;
        if(tmp>0){
            weaponList.remove(wc);
            weaponList.put(wc, tmp);
            return true;
        }
        else{
            return false;
        }
    }

    private boolean removeEquipment(EquipmentCard ec){
        return equipmentList.remove(ec);
    }

    public HeroCard getHeroCard(){
        return hero;
    }

    public TreeSet<EquipmentCard> getEquipmentCards(){
        return new TreeSet<>(equipmentList);
    }

    public TreeMap<WeaponCard, Integer> getWeaponCards(){
        return new TreeMap<>(weaponList);
    }

    public TreeMap<DeckCard, Integer> getDeckCards(){
        return new TreeMap<>(deckCards);
    }

    String getWeaponsList() {
        StringBuffer weaponsList = new StringBuffer();
        Set<WeaponCard> wcs = weaponList.keySet();
        Iterator<WeaponCard> i = wcs.iterator();
        WeaponCard wc;
        while(i.hasNext()){
            wc = i.next();
            weaponsList.append(wc.getName());
            if(i.hasNext())
                weaponsList.append(", ");
        }
        return weaponsList.toString();
    }

    String getEquipmentList() {
        StringBuffer equipmentsList = new StringBuffer();
        Iterator<EquipmentCard> i = equipmentList.iterator();
        EquipmentCard ec;
        while(i.hasNext()){
            ec = i.next();
            equipmentsList.append(ec.getName());
            if(i.hasNext())
                equipmentsList.append(", ");
        }
        return equipmentsList.toString();
    }

    String getDeckList() {
        StringBuffer cardsList = new StringBuffer();
        Iterator<DeckCard> i = deckCards.keySet().iterator();
        DeckCard dc;
        while(i.hasNext()){
            dc = i.next();
            cardsList.append("("+deckCards.get(dc)+") "+dc.getName()+" ("+dc.getColor()+")\n");
        }
        return cardsList.toString();
    }

    private boolean checkDeckLimit(AbstractCard ac, int quantity){
        if(!cc){
            return deckCards.size()+quantity <= Rules.BLITZ_DECK_LIMIT;
        }
        else{
            return deckCards.size()+equipmentList.size()+weaponList.size()+quantity <= Rules.CC_TOTAL_LIMIT;
        }
    }

    private boolean checkEquipmentLimit(AbstractCard ac, int quantity){
        if(!cc){
            return equipmentList.size()+weaponList.size()+quantity <= Rules.BLITZ_EQUIPMENT_LIMIT;
        }
        else{
            return deckCards.size()+equipmentList.size()+weaponList.size()+quantity <= Rules.CC_TOTAL_LIMIT;
        }
    }

    private boolean checkCardLegality(AbstractCard ac){
        for(String type: hero.getLegalTypes()){
            int l = type.length();
            if(ac.getType().substring(0, l).equals(type))
                return true;
        }
        return false;
    }

    private boolean checkSpecializationCard(DeckCard dc){
        String effect = dc.getEffect();
        return (!effect.contains("Specialization"))||(hero.getName().contains(effect.substring(0, effect.indexOf("Specialization")-2)));
    }

    private boolean checkMentorCard(DeckCard dc){
        String effect = dc.getEffect();
        return (!effect.contains("Mentor"))||(!cc);
    }
}