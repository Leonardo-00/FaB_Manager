package package_transaction;

import package_board.Ad;
import package_board.Offer;
import package_board.Request;
import package_card.Condition;
import package_card.PhysicalCard;
import package_collection_card.CollectionCardsList;
import package_observer.Observable;
import package_observer.Observer;
import package_server.Server;

import java.util.ArrayList;
import java.util.TreeMap;

import Utils.Exceptions.BuilderException;
import Utils.Exceptions.InsufficientQuantityException;
import Utils.Exceptions.InvalidAdIdException;
import Utils.Exceptions.InvalidQuantityRemovalException;
import Utils.Exceptions.InvalidTransactionIdException;
import Utils.Exceptions.InvalidTransactionStateException;
import Utils.Exceptions.InvalidUsernameException;
import Utils.constants.IdGenerator;
import Utils.constants.TransactionState;

public class TransactionsManager extends Observable {

    private final Server server;
    private TreeMap<Integer, Transaction> transactionsList;
    private final ArrayList<Observer> observers;
    private IdGenerator idGen;

    public TransactionsManager(Server instance){
        server = instance;
        transactionsList = new TreeMap<>();
        idGen = new IdGenerator();
        observers = new ArrayList<>();
    }

    public TransactionsManager(TransactionsManager tm) {
        server = tm.server;
        transactionsList = new TreeMap<>(tm.transactionsList);
        observers = new ArrayList<>(tm.observers);
        idGen = new IdGenerator(tm.idGen);
    }

    public void setNextId(int id){
        idGen.setNextId(id);
    }

    public TransactionBuilder getTransactionBuilder(){
        return new TransactionBuilder(server);
    }

    public TreeMap<Integer, Transaction> getTransactionsList() {
        return new TreeMap<>(transactionsList);
    }

    public Transaction getTransaction(int id) throws InvalidTransactionIdException{
        if (transactionsList.containsKey(id))
            return transactionsList.get(id);
        else
            throw new InvalidTransactionIdException("There is not a transaction with id: "+id);
    }

    void addTransactionToList(int id, Transaction transaction){
        transactionsList.put(id, transaction);
    }

    public int getCardQuantity(String user, PhysicalCard pc) {
        int q = 0;
        for (Transaction t : transactionsList.values()) {
            if (t.isActive())
                q += t.getQuantity(user, pc);
        }
        return q;
    }

    public void addTransactionFromAcceptedAd(String secondUser, Ad a) throws InvalidUsernameException{
        if(server.getUsers().exists(secondUser)){
            Transaction t = new Transaction(server.getUsers().getUserInfo().getUsername(), 
                                            secondUser, null, a.getId(), 
                                            TransactionState.firstUserConfirmed);
            if(a instanceof Request){
                Request r = (Request)a;
                CollectionCardsList list = server.getCollection().getCollection().get(r.getCc());
                Condition[] conditions = Condition.values();
                for(int i = conditions.length-1; 
                    conditions[i].compareTo(r.getMinimalCondition()) >= 0; i--){
                    PhysicalCard pc = server.getCards().getPhysCard(r.getCc(), conditions[i]);
                    int q = list.getCardQuantity(pc);
                    if(q > 0){
                        if(q >= r.getQuantity())
                            t.addCard(t.getFirstUser(), pc, r.getQuantity());
                        else
                            t.addCard(t.getFirstUser(), pc, q);
                        break;
                    }
                }
            }
            else{
                Offer o = (Offer)a;
                t.addCard(t.getSecondUser(), o.getPc(), o.getQuantity());
            }
            addTransactionToList(idGen.getNextId(), t);
            notifyObservers();
        }
        else
            throw new InvalidUsernameException("Invalid username: "+secondUser);
    }

    public void addTransactionFromAdsMatch(Ad offer, Ad request){
        Transaction t = new Transaction(offer.getOwner(), request.getOwner(), 
                                        offer.getId(), request.getId(), 
                                        TransactionState.fromAdsMatch);
        int offerQ = offer.getQuantity(), requestQ = request.getQuantity();
        t.addCard(offer.getOwner(), ((Offer)offer).getPc(), (offerQ>requestQ)?requestQ:offerQ);
        addTransactionToList(idGen.getNextId(), t);
        notifyObservers();
    }

    public void addManualTransaction(String user2) throws InvalidUsernameException {
        if(server.getUsers().exists(user2)){
            Transaction t = new Transaction(server.getUsers().getUserInfo().getUsername(), 
                                            user2, null, null, 
                                            TransactionState.firstUserConfirmed);
            addTransactionToList(idGen.getNextId(), t);
            notifyObservers();
        }
        else
            throw new InvalidUsernameException("Invalid username: "+user2);
    }

    public boolean acceptTransaction(int id, String user) 
    throws InvalidTransactionIdException, InvalidTransactionStateException {

        Transaction t = getTransaction(id);
        switch (t.getState()) {
            case fromAdsMatch: {
                if (!(t.containsUser(user))) {
                    return false;
                } else {
                    if (t.isSecondUser(user)) {
                        t.swapUsers();
                    }
                    t.setState(TransactionState.firstUserConfirmed);
                    return true;
                }
            }
            case firstUserConfirmed: {
                if (t.isSecondUser(user)) {
                    t.setState(TransactionState.active);
                    return true;
                } else
                    return false;
            }
            default:
                throw new InvalidTransactionStateException
                ("Invalid operation, the transaction" +
                " is not in a state that allows acceptance");
        }
    }

    public void addCardToTransaction(int id, String user, PhysicalCard pc, int q) 
    throws InvalidTransactionIdException, InvalidUsernameException, 
           InvalidTransactionStateException, InsufficientQuantityException {

        Transaction t = getTransaction(id);
        if(!t.containsUser(user))
            throw new InvalidUsernameException("The transaction with id: "+ 
                                                id+ " does not contain the user: "+user);

        if(!t.isActive())
            throw new InvalidTransactionStateException
            ("Cannot add card to a non active transaction");

        if (!server.getCollection().checkCardAvailability(user, pc, q))
            throw new InsufficientQuantityException
            ("The logged user hasn't enough card to add to the transaction list");
        
        t.addCard(user, pc, q);
    }

    public void removeCardFromTransaction(int id, String user, PhysicalCard pc, int q) 
    throws InvalidQuantityRemovalException, InvalidTransactionIdException,
           InvalidUsernameException, InvalidTransactionStateException{
    
        if (transactionsList.containsKey(id)) {
            Transaction t = transactionsList.get(id);
            if(!t.containsUser(user))
                throw new InvalidUsernameException("No user named "+user+
                                                   " exists in the transaction with id "+id);
            if(!t.isActive())
                throw new InvalidTransactionStateException
                ("Can't modify a transaction that's not active");
            t.removeCard(user, pc, q);
        }
        else
            throw new InvalidTransactionIdException("There is not a transaction with id: "+id);
    }

    public void concludeTransactionWithoutSuccess(int id, String user) 
    throws InvalidTransactionIdException {

        if (transactionsList.containsKey(id)) {
            Transaction t = transactionsList.get(id);
            if (t.containsUser(user)&&t.isActive()) {
                t.setState(TransactionState.failed);
                server.getBoard().releaseAd(t.getFirstAdID());
                server.getBoard().releaseAd(t.getSecondAdID());
            }
        }
        else
            throw new InvalidTransactionIdException
            ("There is not a transaction with id: "+id);
    }

    public boolean concludeTransactionWithSuccess(int id, String user) 
    throws InvalidTransactionIdException, InvalidUsernameException {

        if (transactionsList.containsKey(id)) {
            Transaction t = transactionsList.get(id);
            if (t.containsUser(user)) {
                if (t.getState() == TransactionState.active) {
                    if (t.isSecondUser(user)) {
                        t.swapUsers();
                    }
                    t.setState(TransactionState.closing);
                    return true;
                } 
                else if (t.getState() == TransactionState.closing &&
                         t.isSecondUser(user)) {
                    t.setState(TransactionState.concluded);
                    applyExchange(t);
                    return true;
                }
                else
                    return false;
            } 
            else
                throw new InvalidUsernameException
                ("The selected transaction doesn't contain the user: "+user);
        }
        else
            throw new InvalidTransactionIdException
            ("There is not a transaction with id: "+id);
    }


    private void applyExchange(Transaction t){
        server.getBoard().updateAdsFromSuccessfulTransaction(
            t.getFirstUser(), t.getSecondUser(), t.getFirstAdID(), t.getSecondAdID(), 
            t.getList());

        server.getCollection().exchangeCardsInCollections(
            t.getFirstUser(), t.getSecondUser(), t.getList());
    }


    public class TransactionBuilder {

        private boolean building;
        private int id;
        private String firstUser;
        private String secondUser;
        private Integer firstAdID = -1;
        private Integer secondAdID = -1;
        private TransactionState state;
        private Transaction t;

        private TransactionsManager transactionsManager;
    
        private TransactionBuilder(Server server){
            transactionsManager = server.getTransactions();
            reset();
        }
    
        public void buildNewTransaction() throws BuilderException{
            if(building){
                throw new BuilderException("Cannot start the creation of a new transaction while currently building another one");
            }
            building = true;
        }

        public void setID(int id) throws InvalidTransactionIdException, BuilderException, InvalidAdIdException{
            if(!building){
                throw new BuilderException("Cannot invoke builder methods if no transaction is being built");
            }
            if(this.id > 0)
                throw new BuilderException("The parameter id has already been set for the current transaction");
            if(transactionsManager.getTransactionsList().containsKey(id))
                throw new InvalidTransactionIdException("A transaction with id: "+id+" already exists");
            if(id < 1)
                throw new InvalidAdIdException("The given id is not valid for a transaction");    
            this.id = id;
        }
    
        public void setFirstUser(String username) throws BuilderException{
            if(!building){
                throw new BuilderException("Cannot invoke builder methods if no transaction is being built");
            }
            if(firstUser != null)
                throw new BuilderException("The parameter firstUser has already been set for the current transaction");
            firstUser = username;
        }

        public void setSecondUser(String username) throws BuilderException{
            if(!building){
                throw new BuilderException("Cannot invoke builder methods if no transaction is being built");
            }
            if(secondUser != null)
                throw new BuilderException("The parameter secondUser has already been set for the current transaction");
            secondUser = username;
        }

        public void setFirstAd(Integer id) throws BuilderException{
            if(!building){
                throw new BuilderException("Cannot invoke builder methods if no transaction is being built");
            }
            if(firstAdID != -1)
                throw new BuilderException("The parameter firstAd has already been set for the current transaction");
            firstAdID = id;
        }

        public void setSecondAd(Integer id) throws BuilderException{
            if(!building){
                throw new BuilderException("Cannot invoke builder methods if no transaction is being built");
            }
            if(secondAdID != -1)
                throw new BuilderException("The parameter secondAd has already been set for the current transaction");
            secondAdID = id;
        }

        public void setState(TransactionState s) throws BuilderException, InvalidTransactionStateException{
            if(!building){
                throw new BuilderException("Cannot invoke builder methods if no transaction is being built");
            }
            if(state != null)
                throw new BuilderException("The parameter state has already been set for the current transaction");
            state = s;            
        }

        public void build() throws BuilderException, InvalidTransactionIdException{
            if(isReady()){
                addTransactionToList(id, new Transaction(firstUser, secondUser, firstAdID, secondAdID, state));
                t = getTransaction(id);
            }
            else{
                throw new BuilderException("Cannot invoke the build method when at least a parameter still has to be set");
            }
        }

        public void addCardToTransaction(String user, PhysicalCard pc, int quantity){
            t.addCard(user, pc, quantity);
        }

        public void reset(){
            building = false;
            firstAdID = -1;
            secondAdID = -1;
            firstUser = null;
            secondUser = null;
            state = null;
            id = -1;
        }

        private boolean isReady(){
            return (building)&&((firstAdID == null)||(firstAdID != -1))&&((secondAdID == null)||(secondAdID != -1))&&(firstUser!=null)&&(secondUser!=null)&&(state!=null)&&(id>0);
        }
        
    }

    public void addObserver(Observer o) {
        observers.add(o);
    }

    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    protected void notifyObservers() {
        for (Observer o : observers)
            o.update();
    }

}