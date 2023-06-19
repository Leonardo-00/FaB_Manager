package package_collection_card;
import package_card.*;
import package_server.Server;
import package_observer.Observable;
import package_observer.Observer;

import java.util.*;
import java.util.Map.Entry;

import Utils.Exceptions.InvalidQuantityRemovalException;


public class Collection extends Observable{

    private final String user;
    private TreeMap<ConcreteCard, CollectionCardsList> collection;
    private ArrayList<Observer> observers;
    private final Server server;

    public Collection(String u, Server instance){
        user = u;
        server = instance;
        collection = new TreeMap<>();
        observers = new ArrayList<>();
    }

    public Collection(Collection c2) {
        this.user = c2.user;
        this.collection = new TreeMap<>(c2.collection);
        observers = new ArrayList<>(c2.observers);
        this.server = c2.server;
    }

    public String getUser() {
        return user;
    }
    
    public void addCard(PhysicalCard pc, int q){
        CollectionCardsList pq;
        if((pq = collection.get(pc.getConcrCard())) != null){
            pq.addCard(pc, q);
        }
        else{
            ConcreteCard cc = pc.getConcrCard();
            CollectionCardsList ccl = new CollectionCardsList();
            for(PhysicalCard card: server.getCards().getPhysCards(cc.getId())){
                if(!card.equals(pc))
                    ccl.addCard(card, 0);
                else
                    ccl.addCard(card, q);
            }
            collection.put(cc, ccl);
        }
    }

    public void addCard(PhysicalCard pc){
        addCard(pc, 1);
    }

    public void removeCard(PhysicalCard pc, int q) throws InvalidQuantityRemovalException{
        if(checkCardAvailability(user, pc, q))
            collection.get(pc.getConcrCard()).removeCard(pc, q);
        else
            throw new InvalidQuantityRemovalException
            ("There's not enough available quantity to remove of the selected card");
    }

    public void removeCard(PhysicalCard pc) throws InvalidQuantityRemovalException{
        removeCard(pc, 1);
    }

    public TreeMap<ConcreteCard, CollectionCardsList> getCollection() {
        return new TreeMap<>(collection);
    }

    public int getQuantity(PhysicalCard pc){
        try{
        return collection.get(pc.getConcrCard()).getCardQuantity(pc);
        }catch(Exception e){
            return 0;
        }
    }

    public boolean checkCardAvailability(String user, PhysicalCard pc, int quantity){
        return getQuantity(pc)>=quantity + getReservedQuantity(pc);
    }
    
    public int getReservedQuantity(PhysicalCard pc){
        return server.getBoard().getReservedCardQuantity(user, pc) + 
                server.getTransactions().getCardQuantity(user, pc);
    }

    public int getAvailableQuantity(PhysicalCard pc){
        return getQuantity(pc) - getReservedQuantity(pc);
    }

    public boolean exchangeCardsInCollections(String user1, String user2, TreeMap<String, CardsList> list){
        if(!user1.equals(user)){
            String tmp = user1;
            user1 = user2;
            user2 = tmp;
        }
        Collection tmpColl = server.getTmpColl(user2);
        TreeMap<ConcreteCard, CollectionCardsList> tmpUserColl = new TreeMap<>(collection);
        try{
            for(Entry<PhysicalCard, Integer> e: list.get(user1).getCardsList().entrySet()){
                removeCard(e.getKey(), e.getValue());
                tmpColl.addCard(e.getKey(), e.getValue());
            }
            for(Entry<PhysicalCard, Integer> e: list.get(user2).getCardsList().entrySet()){
                tmpColl.removeCard(e.getKey(), e.getValue());
                addCard(e.getKey(), e.getValue());
            }
            server.saveTmpColl(tmpColl);
            return true;
        }
        catch(InvalidQuantityRemovalException e){
            server.appendStringToLog(e.getMessage());
            collection = tmpUserColl;
            return false;
        }
    }

    @Override
    public void addObserver(Observer o) {
        observers.add(o);        
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    protected void notifyObservers() {
        for(Observer o: observers)
            o.update();
    }

}