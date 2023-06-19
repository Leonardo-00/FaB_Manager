package package_server;

import java.util.ArrayList;
import java.util.TreeMap;

import Utils.Exceptions.CorruptedCardException;
import Utils.Exceptions.CorruptedDeckException;
import XMLParsing.XMLParser;
import package_board.Board;
import package_card.AbstractCard;
import package_card.CardManager;
import package_card.ConcreteCard;
import package_card.ExpansionCard;
import package_collection_card.Collection;
import package_deck.DeckManager;
import package_transaction.TransactionsManager;
import package_user.UserInfo;
import package_user.UsersManager;

public class Server{
    
    public static boolean logged = false;
    private static Server instance = null;
    private XMLParser parser;
    private DeckManager decksCollection;
    private Collection userCollection;
    private CardManager cardsManager;
    private UsersManager usersManager;
    private TransactionsManager transactionsManager;
    private Board adsManager;
    public static String username;
    private String logString="";

    public Server() {
        parser = new XMLParser(this);
        decksCollection = new DeckManager(this);
        adsManager = new Board(this);
        transactionsManager = new TransactionsManager(this);
        usersManager = new UsersManager(parser.loadUsernames());
    }

    public static Server getInstance() {
        if (instance == null)
            instance = new Server();
        return instance;
    }

    public DeckManager getDeckManager() {
        return decksCollection;
    }

    public Collection getCollection() {
        return userCollection;
    }

    public CardManager getCards() {
        return cardsManager;
    }

    public UsersManager getUsers(){
        return usersManager;
    }

    public Board getBoard() {
        return adsManager;
    }

    public String getLogString(){
        return logString;
    }

    public TransactionsManager getTransactions() {
        return transactionsManager;
    }

    public boolean login(final String user, final String pw){
        UserInfo info = parser.getInfoByCredentials(user, pw);
        if(info != null){
            usersManager.setLoggedUserInfos(info);
            try{

                //Loading cards
                TreeMap<Integer, AbstractCard> aCards = new TreeMap<>();
                ArrayList<ExpansionCard> eCards = new ArrayList<>();
                TreeMap<Integer, ConcreteCard> cCards = new TreeMap<>();
                try{
                    parser.loadCards(aCards, eCards, cCards);
                }catch(CorruptedCardException e){
                    appendStringToLog(e.getMessage());
                    shutDown();
                }
                cardsManager = new CardManager(aCards, eCards, cCards);

                //Loading user collection

                userCollection = parser.loadUserCollection(user);

                //Loading decks
                decksCollection.loadDecks(parser.loadDecks());
                
                adsManager.loadAds(parser.loadAds());
                parser.loadTransactions();
                return logged = true;
            }
            catch(CorruptedDeckException e){
                appendStringToLog(e.getMessage());
            }
        }
        return false;
    }

    public void testModeLogin(){
        if(!logged){
            login("TestUser1","1234");
            Managers.dm = new DeckManager(decksCollection);
            Managers.c = new Collection(userCollection);
            Managers.b = new Board(adsManager);
            Managers.tm = new TransactionsManager(transactionsManager);
        }
    }

    public void resetManagers(){
        userCollection = new Collection(Managers.c);
        decksCollection = new DeckManager(Managers.dm);
        adsManager = new Board(Managers.b);
        transactionsManager = new TransactionsManager(Managers.tm);
    }

    public void appendStringToLog(String s){
        logString = logString + s + "\n";
    }

    public void shutDown() {
        updateDB();
        System.out.println(getLogString());
        System.out.println("[ Shutting off the system ]");
        System.exit(0);
    }

    public boolean userExists(String user){
        return usersManager.exists(user);
    }

    public String getLoggedUser(){
        return usersManager.getUserInfo().getUsername();
    }

    private void updateDB(){
        if(logged){
            parser.saveDecks(decksCollection.getAllDecks());
            parser.saveUserCollection(userCollection);
            parser.saveAds(adsManager.getAdsList());
            parser.saveTransactions(transactionsManager.getTransactionsList());
        }
    }

    public Collection getTmpColl(String user2) {
        return parser.loadUserCollection(user2);
    }

    public void saveTmpColl(Collection coll){
        parser.saveUserCollection(coll);
    }

    public void setNewPassword(String newPw){
        getUsers().setNewPassword(newPw);
    }

    public boolean checkCurrentPassword(String psw) {
        return true;
    }
}

class Managers{

    static Collection c;
    static DeckManager dm;
    static Board b;
    static TransactionsManager tm;
}