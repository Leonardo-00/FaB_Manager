package package_board;

import java.util.ArrayList;
import java.util.Map.Entry;

import Utils.Exceptions.InvalidAdException;
import Utils.Exceptions.InvalidAdIdException;
import Utils.Exceptions.InvalidUsernameException;
import Utils.constants.AdState;
import Utils.constants.IdGenerator;

import java.util.TreeMap;

import package_card.CardsList;
import package_card.ConcreteCard;
import package_card.Condition;
import package_card.PhysicalCard;
import package_collection_card.CollectionCardsList;
import package_observer.Observable;
import package_observer.Observer;
import package_server.Server;

public class Board extends Observable{

    private final Server server;
    private TreeMap<Integer, Ad> adsList;
    private ArrayList<Observer> observers;
    private IdGenerator idGen;

    public Board(Server instance){
        server = instance;
        observers = new ArrayList<>();
        idGen = new IdGenerator();
    }

    public Board(Board b){
        server = b.server;
        adsList = new TreeMap<>(b.adsList);
        observers = new ArrayList<>(b.observers);
        idGen = new IdGenerator(b.idGen);
    }

    public void setNextId(int id){
        idGen.setNextId(id);
    }

    public void loadAds(TreeMap<Integer, Ad> aList){
        adsList = aList;
    }

    public int getReservedCardQuantity(String user, PhysicalCard pc){
        int q=0;
        for(Ad a: adsList.values()){
            if(a instanceof Offer && a.getOwner().equals(user))
                if(((Offer)a).getPc().equals(pc))
                    q+=a.getQuantity();
        }
        return q;
    }

    public void addRequest(String u, ConcreteCard cc, Condition minCond, int quantity){
        checkMatching(new Request(idGen.getNextId(), u,cc,minCond, quantity)); 
    }

    public boolean addOffer(String u, PhysicalCard pc, int quantity){
        if(server.getCollection().checkCardAvailability(u, pc, quantity)){
            checkMatching(new Offer(idGen.getNextId(), u,pc, quantity));
            return true;
        }
        else
            return false;
    }

    void checkMatching(Ad newA){
        if(newA instanceof Request){
            Request r = (Request)newA;
            for(Ad a: adsList.values()){
                if(!newA.owner.equals(a.owner) && 
                    !(a.state == AdState.pending) && a instanceof Offer){
                    PhysicalCard pc = ((Offer) a).getPc();
                    if(pc.getConcrCard().equals(r.getCc()) && 
                        pc.getCondition().compareTo(r.getMinimalCondition()) >=0){
                        server.getTransactions().addTransactionFromAdsMatch(a, newA);
                        newA.state = AdState.pending;
                        a.state = AdState.pending;
                        notifyObservers();
                        break;
                    }
                }
            }
        }
        else{
            PhysicalCard pc = ((Offer)newA).getPc();
            for(Ad a: adsList.values()){
                if(!newA.owner.equals(a.owner) &&
                    !(a.state == AdState.pending) && a instanceof Request){
                    Request r = (Request)a;
                    if(pc.getConcrCard().equals(r.getCc()) && 
                        pc.getCondition().compareTo(r.getMinimalCondition()) >=0){
                        server.getTransactions().addTransactionFromAdsMatch(newA, a);
                        newA.state = AdState.pending;
                        a.state = AdState.pending;
                        notifyObservers();
                        break;
                    }
                }
            }
        }
        adsList.put(newA.getId(), newA);
    }

    public TreeMap<Integer, Ad> getActiveAdsList(){
        TreeMap<Integer, Ad> activeAds = new TreeMap<>();
        for(Ad a: adsList.values()){
            if(a.state == AdState.active){
                activeAds.put(a.getId(), a);
            }
        }
        return activeAds;
    }

    public TreeMap<Integer, Request> getRequestsList(){
        TreeMap<Integer, Request> requestsList = new TreeMap<>();
        for(Ad a:adsList.values()){
            if(a instanceof Request && a.state == AdState.active)
                requestsList.put(a.getId(), (Request) a.getCopy());
        }
        return requestsList;
    }

    public TreeMap<Integer, Offer> getOffersList(){
        TreeMap<Integer, Offer> offersList = new TreeMap<>();
        for(Ad a:adsList.values()){
            if(a instanceof Offer && a.state == AdState.active)
                offersList.put(a.getId(), (Offer)a.getCopy());
        }
        return offersList;
    }

    public void acceptAd(int id) throws InvalidAdIdException, InvalidAdException{
        Ad a = getAd(id);
        if(a == null)
            throw new InvalidAdIdException("There is no ad with id: "+id);
        if(a.state != AdState.active)
            throw new InvalidAdException("Cannot accept an ad which is not active");
        if(a.owner == server.getLoggedUser())
            throw new InvalidAdException("The user cannot accept his own ad");
        try{
            if(a instanceof Request){
                boolean canAccept = false;
                Request r = (Request)a;
                CollectionCardsList list = server.getCollection().getCollection().
                                            getOrDefault(r.getCc(), new CollectionCardsList());
                Condition[] conditions = Condition.values();
                for(int i = conditions.length-1; i >= 0 && 
                    conditions[i].compareTo(r.getMinimalCondition()) >= 0; i--){
                    PhysicalCard pc = server.getCards().getPhysCard(r.getCc(), conditions[i]);
                    if(list.getCardQuantity(pc) > 0){
                        canAccept = true;
                        break;
                    }
                }
                if(!canAccept)
                    throw new InvalidAdException
                    ("The user doesn't own enough cards to accept the ad with id: "+id);
            }
            server.getTransactions().addTransactionFromAcceptedAd(a.owner, a);
        }catch(InvalidUsernameException e){

        }
    }

    public Ad getAd(int id){
        return adsList.get(id);
    }

    public void removeAd(int id){
        adsList.remove(id);
    }

    /**Questo metodo può essere utilizzato sia per diminuire la quantità richiesta a fronte di una trattativa conclusa con successo, 
    *  ma anche per modificare la quantità, in caso si voglia introdurre la funzionalità di modificare annunci esistenti*/
    public void modifyQuantity(int id, int q) throws InvalidAdIdException, InvalidAdException{
        Ad ad = adsList.get(id);
        if(ad == null){
            throw new InvalidAdIdException("There is no ad with id "+id);
        }
        if(ad.state == AdState.concluded || ad.state == AdState.deleted)
            throw new InvalidAdException("Cannot modify an ad which is not active");
        ad.quantity+=q;
        if(ad.quantity<=0)
            ad.state = AdState.concluded;
        else
            ad.state = AdState.active;
        notifyObservers();
    }

    public void suspendAd(int id){
        Ad ad = adsList.get(id);
        if(ad != null && (ad.state == AdState.active))
            ad.state = AdState.pending;
            notifyObservers();
    }

    public void releaseAd(int id) {
        Ad ad = adsList.get(id);
        if(ad != null && (ad.state == AdState.pending))
            ad.state = AdState.active;
            notifyObservers();
    }

    public TreeMap<Integer, Ad> getAdsList() {
        return new TreeMap<>(adsList);
    }

    /**
    * Questo metodo modifica le quantità negli annunci associati ad una trattativa quando questa viene conclusa con successo.
    * È possibile che uno(o entrambi) degli annunci venga completamente soddisfatto dalla trattativa, e in tal caso questo vedrà il
    * suo stato cambiare in rimosso (removed), ma il suo codice rimarrà associato alla trattativa per permettere una facile estensione
    * per l'analisi delle trattative del sistema.
    * 
    * Nel caso in cui un annuncio non sia esaurito, la sua quantità risulterà diminuita e questo verrà reso nuovamente visibile ad altri
    * utenti.
    * */
    public void updateAdsFromSuccessfulTransaction(String user1, String user2, int id1, int id2,
                                                    TreeMap<String, CardsList> list){
        String offerer="";
        Ad ad1 = getAd(id1), ad2 = getAd(id2);
        PhysicalCard offerCard, tmp;
        ConcreteCard requestCard;
        Condition requestCondition;
        Offer o=null;
        Request r=null;
        if(ad1 == null && ad2 != null){
            ad1 = ad2;
            ad2 = null;
            String tmpString = user1;
            user1 = user2;
            user2 = tmpString;
        }
        boolean firstDone = (ad1 == null), secondDone = (ad2 == null);

        
        if(ad1 != null){
            if(ad1 instanceof Offer){
                offerer = user1;
                o = (Offer)ad1;
                r = (Request)ad2;
            }
            else{
                offerer = user2;
                o = (Offer)ad2;
                r = (Request)ad1;
            }
            offerCard = (o!=null)?o.getPc():null;
            requestCard = (r!=null)?r.getCc():null;
            requestCondition = (r!=null)?r.getMinimalCondition():null;
            for(Entry<PhysicalCard, Integer> e: list.get(offerer).getCardsList().entrySet()){
                tmp = e.getKey();
                try{
                    if((!firstDone)&&tmp.equals(offerCard)){
                        modifyQuantity(o.getId(), -e.getValue());
                        if((firstDone = true) && secondDone)
                            break;
                    }
                    if((!secondDone)&&tmp.getConcrCard().equals(requestCard)&&
                        tmp.getCondition().compareTo(requestCondition) >= 0){
                        modifyQuantity(r.getId(), -e.getValue());
                        if(firstDone && (secondDone = true))
                            break;
                    }
                }
                catch(Exception ex){

                }
            }
            notifyObservers();
        }
    }

    public void addObserver(Observer o){
        observers.add(o);
    }

    public void removeObserver(Observer o){
        observers.remove(o);
    }
    
    protected void notifyObservers(){
        for(Observer o: observers)
            o.update();
    }
}