package package_deck;

import java.util.TreeMap;

import Utils.Exceptions.CardNotFoundException;
import Utils.Exceptions.CorruptedCardException;
import Utils.Exceptions.DeckNotFoundException;
import Utils.Exceptions.IllegalCardAdditionException;
import Utils.Exceptions.InvalidDeckAccessException;
import Utils.constants.IdGenerator;
import package_card.AbstractCard;
import package_card.CardManager;
import package_card.Color;
import package_card.HeroCard;
import package_server.Server;

public class DeckManager{
    private TreeMap<Integer, Deck> userDecks;
    private TreeMap<Integer, Deck> publicDecks;
    private final Server server;
    private IdGenerator idGen;


    //Constructor used by the system
    public DeckManager(Server instance){
        server = instance;
        userDecks = new TreeMap<>();
        publicDecks = new TreeMap<>();
        idGen = new IdGenerator();
        
    }

    //Copy constructor used only for test purposes
    public DeckManager(DeckManager dc) {
        this.userDecks = new TreeMap<>(dc.userDecks);
        this.publicDecks = new TreeMap<>(dc.publicDecks);
        this.server = dc.server;
        this.idGen = new IdGenerator(dc.idGen);
    }

    //Method called during initialization of the system
    public void loadDecks(TreeMap<Integer, Deck> decks){
        for(Deck d: decks.values()){
            if(d.getOwner().equals(server.getLoggedUser()))
                userDecks.put(d.getSerialCode(), d);
            else
                publicDecks.put(d.getSerialCode(), d); 
        }  
    }

    public void setNextId(int id){
        idGen.setNextId(id);
    }

    public TreeMap<Integer, Deck> getUserDecks() {
        return new TreeMap<>(userDecks);
    }

    public TreeMap<Integer, Deck> getPublicDecks(){
        return new TreeMap<>(publicDecks);
    }

    public void addDeck(String title, String description, HeroCard hc){
        addDeck(new Deck(idGen.getNextId(), server.getLoggedUser(), title, description, hc));
    }

    public void addDeck(String title, HeroCard hc){
        addDeck(new Deck(idGen.getNextId(), server.getLoggedUser(), title, "Default description", hc));
    }

    public void addDeck(Deck deck){
        userDecks.put(deck.getSerialCode(), deck);
    }


    /*
    Method to create a copy of a public deck from another user in the logged user collection

    Throws @DeckNotFoundException if there is no public deck with the given id
    */
    public void copyDeck(int id) throws DeckNotFoundException{
        if(!publicDecks.containsKey(id))
            throw new DeckNotFoundException("No public deck with id: "+id+" found");
        Deck d = new Deck(idGen.getNextId(), publicDecks.get(id), server.getLoggedUser());
        userDecks.put(d.getSerialCode(),d);
    }

    public Deck getDeck(int id) throws DeckNotFoundException{
        Deck d =userDecks.get(id);
        
        if(d==null){
            d = publicDecks.get(id);
            if(d==null)
                throw new DeckNotFoundException("No deck with id: "+id+ " found");
            else
                return d;
        }
        else
            return d;
            
    }

    public boolean removeDeck(int id) throws DeckNotFoundException, InvalidDeckAccessException{
        if(!userDecks.containsKey(id) && publicDecks.containsKey(id))
            throw new InvalidDeckAccessException("A user other than the owner of the deck can't try to delete it");
        if(userDecks.remove(id) == null)
            throw new DeckNotFoundException("The deck with id: "+id+" was not found");
        return true;
    }

    public void changeDeckVisibility(int id) throws DeckNotFoundException, InvalidDeckAccessException{
        if(checkLegalOperation(id))
            getDeck(id).changeVisibility();
        else
            throw new InvalidDeckAccessException("A user other than the owner of the deck can't try to change its visibility");
    }

    void saveDeckEdit(Deck d){
        userDecks.replace(d.getSerialCode(), d);
    }

    public EditableDeck editDeck(int id) 
    throws InvalidDeckAccessException, DeckNotFoundException{
        if(!checkLegalOperation(id))
            throw new InvalidDeckAccessException
            ("A user other than the owner of the deck can't try to modify it");

        return new EditableDeck(getDeck(id));
    }


    /*
     * This method takes a text, representing a deck with the format used by the site fabdb.net
     * 
     * The deck created when importing is added to the list of the logged user deck, with a private visibility, as standard
     */
    public boolean importDeck(String deckString){

        CardManager cards = server.getCards();
        try{

            String currentString = deckString.substring(deckString.indexOf("\n\n")+2);

            String title = currentString.substring(0, currentString.indexOf("\n"));

            currentString = currentString.substring(title.length()+2); 
            
            currentString = currentString.substring(currentString.indexOf("Hero"));

            String heroCardName = currentString.substring(6, currentString.indexOf("\n"));

            HeroCard hero = (HeroCard)cards.getAbstractCard(heroCardName);

            Deck d = new Deck(idGen.getNextId(), server.getUsers().getUserInfo().getUsername(), title, "Default description", hero);
            currentString = currentString.substring(6+heroCardName.length()+1);

            String weaponsString = currentString.substring(9, currentString.indexOf("\nEquipment"));
            currentString = currentString.substring(weaponsString.length()+10);

            String weapon;
            AbstractCard card;
            do{

            if(weaponsString.contains(",")){
                weapon = weaponsString.substring(0, weaponsString.indexOf(","));
                try{
                    if(!Character.isTitleCase(weaponsString.charAt(weaponsString.indexOf(",")+2))){
                        StringBuffer tmp = new StringBuffer(weaponsString.substring(weaponsString.indexOf(",")+1));
                        weapon = new StringBuffer(weapon+","+(tmp.substring(0, tmp.indexOf(",")))).toString();
                    }
                }catch(IndexOutOfBoundsException e){

                }
                
                weaponsString = weaponsString.substring(weapon.length()+2);
            }
            else{
                weapon = weaponsString;
                weaponsString = weaponsString.substring(weapon.length());
            }
            card = cards.getAbstractCard(weapon);

            if(card.getType().contains("(1H)")){
                d.addCard(card,2);
            }
            else
                d.addCard(card);
            
            }while(!(weaponsString.equals("")));

            String equipmentString = currentString.substring(11, currentString.indexOf("\n\n"));
            currentString = currentString.substring(equipmentString.length()+13);
            String equipment;
            do{

                if(equipmentString.contains(",")){
                    equipment = equipmentString.substring(0, equipmentString.indexOf(","));
                    equipmentString = equipmentString.substring(equipment.length()+2);
                }
                else{
                    equipment = equipmentString;
                    equipmentString = equipmentString.substring(equipment.length());
                }

                card = cards.getAbstractCard(equipment);
                d.addCard(card);
            }while(!(equipmentString.equals("")));

            int quantity, offset;
            String deckCard;
            Color c;
            do{
                if(currentString.charAt(0) != '('){
                    break;
                }
                deckCard = currentString.substring(0, currentString.indexOf("\n"));

                currentString = currentString.substring(deckCard.length()+1);

                quantity = Integer.parseInt(deckCard.substring(1, deckCard.indexOf(")")));
                
                if(deckCard.substring(deckCard.length()-4, deckCard.length()-1).equals("red")){
                    c = Color.red;
                    offset = 6;
                }
                else if(deckCard.substring(deckCard.length()-7, deckCard.length()-1).equals("yellow")){
                    c = Color.yellow;
                    offset = 9;
                }
                else if(deckCard.substring(deckCard.length()-5, deckCard.length()-1).equals("blue")){
                    c = Color.blue;
                    offset = 7;
                }
                else if(deckCard.substring(deckCard.length()-10, deckCard.length()-1).equals("undefined")){
                    c = Color.undefined;
                    offset = 12;
                }
                else
                    throw new CorruptedCardException("There is an invalid color in one of the deck cards");

                AbstractCard ac = cards.getAbstractCard(deckCard.substring(4, deckCard.length()-offset), c);

                d.addCard(ac, quantity);
                
            }while(!currentString.isEmpty());

            userDecks.put(d.getSerialCode(), d);
            return true;
        }
        catch(CardNotFoundException | CorruptedCardException | IllegalCardAdditionException e){
            return false;
        }
    }

    public String exportDeck(int id) throws DeckNotFoundException{
        return getDeck(id).exportDeck();
    }

    public TreeMap<Integer, Deck> getAllDecks(){
        TreeMap<Integer, Deck> decks = new TreeMap<>();
        for(Deck d: userDecks.values())
            decks.put(d.getSerialCode(), d);
        for(Deck d: publicDecks.values())
            decks.put(d.getSerialCode(), d);
        return decks;
    }

    private boolean checkLegalOperation(int id){
        return !publicDecks.containsKey(id);
    }


    public class EditableDeck{

        //Incomplete class, just for testing purposes

        private Deck deck;

        private EditableDeck(Deck d){
            deck = d;
        }

        public void editDeckTitle(String newTitle){
            if(deck != null)
                deck.setTitle(newTitle);
        }

        public void saveChanges(){
            saveDeckEdit(deck);
            deck = null;
        }
    }
}