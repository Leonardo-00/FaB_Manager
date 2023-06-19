package tests;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

import Utils.constants.DataPath;
import package_card.CardManager;
import package_card.ConcreteCard;
import package_card.HeroCard;
import package_card.PhysicalCard;
import package_server.Server;

class FileResetter {

    static private int counter=0;
    static private String collectionFile;
    static private Path collectionPath;
    static private String adFile;
    static private Path adPath;
    static private String transactionFile;
    static private Path transactionPath;
    static private Charset charset;

    static void setFileContents(){
        if(counter==0){
            counter=3;
            collectionPath = Paths.get(DataPath.collectionFile);
            adPath = Paths.get(DataPath.adsFile);
            transactionPath = Paths.get(DataPath.transactionsFile);
            charset = StandardCharsets.UTF_8;
            try {
                collectionFile = new String(Files.readAllBytes(collectionPath), charset);
                adFile = new String(Files.readAllBytes(adPath), charset);
                transactionFile = new String(Files.readAllBytes(transactionPath), charset);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static void resetFiles(){
        try {
            Files.write(collectionPath, collectionFile.getBytes(charset));
            Files.write(adPath, adFile.getBytes(charset));
            Files.write(transactionPath, transactionFile.getBytes(charset));
        } catch (IOException e) { 
            e.printStackTrace();
        }
    }
    
}

class RandGen{

    static private RandGen instance;
    private Random rand;

    static RandGen getInstance(){
        if(instance == null)
            instance = new RandGen();
        return instance;
    }

    private RandGen(){
        rand = new Random();
    }

    /***
    Returns an integer value between 1 and bound excluded. The parameter bound must be positive  
    */
    public int getRandInt(int bound, boolean mustBePositive){
        int r;
        do{
            r = rand.nextInt(bound);
        }while(mustBePositive && r < 1);
        return r;
    }

    public int getRandInt(int bound){
        return getRandInt(bound, false);
    }

    public int getRandInt(boolean mustBePositive){
        return getRandInt(100, mustBePositive);
    }

    public int getRandInt(){
        return getRandInt(100, false);
    }
}

class CardDispenser{

    private CardManager cardManager;
    private static CardDispenser instance;
    private RandGen rand;

    static public CardDispenser getInstance(){
        if(instance == null)
            instance = new CardDispenser();
        return instance;
    }

    public ConcreteCard getRandomConcreteCard(){
        ArrayList<ConcreteCard> cCards = cardManager.getConcreteCards();
        int randomId = rand.getRandInt(cCards.size());
        return cCards.get(randomId);
    }

    public PhysicalCard getRandomPhysicalCard(int id){
        ArrayList<PhysicalCard> pCards = cardManager.getPhysCards(id);
        int randId = rand.getRandInt(pCards.size());
        return pCards.get(randId);        
    }

    public PhysicalCard getRandomPhysicalCard(){
        ConcreteCard cc = getRandomConcreteCard();
        ArrayList<PhysicalCard> pCards = cardManager.getPhysCards(cc.getId());
        int randId = rand.getRandInt(pCards.size());
        return pCards.get(randId);        
    }

    public HeroCard getRandomHeroCard(){
        ArrayList<HeroCard> heroCards = cardManager.getHeroCardsList();
        int randId = rand.getRandInt(heroCards.size());
        return heroCards.get(randId);
    }

    private CardDispenser(){
        cardManager = Server.getInstance().getCards();
        rand = RandGen.getInstance();
    }
}