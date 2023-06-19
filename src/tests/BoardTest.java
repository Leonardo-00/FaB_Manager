package tests;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.TreeMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import Utils.Exceptions.InvalidTransactionIdException;
import Utils.Exceptions.InvalidTransactionStateException;
import Utils.Exceptions.InvalidUsernameException;
import Utils.constants.AdState;
import package_board.Ad;
import package_board.Board;
import package_board.Offer;
import package_card.ConcreteCard;
import package_card.Condition;
import package_card.PhysicalCard;
import package_server.Server;
import package_transaction.TransactionsManager;

public class BoardTest {

    static Server instance;
    static Board board;
    static RandGen rand;
    static CardDispenser cardDispenser;
    static int adMaxId;
    static int tMaxId;

    @BeforeClass
    public static void setup(){
        instance = TestManager.getInstance().getServer();
        rand = RandGen.getInstance();
        cardDispenser = CardDispenser.getInstance();
    }

    @Before
    public void setupBoard(){
        board = instance.getBoard();
        adMaxId = TestManager.getMax(board.getAdsList().keySet());
    }

    @Test
    public void testOfferInsertion(){

        //Setup: 
        //we put in user the username of the logged user, 
        //and in pc a random PhysicalCard
        String user = instance.getLoggedUser();
        PhysicalCard pc = cardDispenser.getRandomPhysicalCard();

        //We add a random positive quantity of the card pc 
        //to the user collection (positive since we add at least 1)
        instance.getCollection().addCard(pc, rand.getRandInt(true));
        int availableQuantity = instance.getCollection().getAvailableQuantity(pc);

        //We generate a random number between 1 and availableQuantity 
        //(the latter excluded)
        int randQ = rand.getRandInt(availableQuantity, true);
        int totalAds = board.getAdsList().size();

        //We try to insert an offer from the logged user, for the card pc, 
        //with a quantity that is for sure not available

        //This try will fail
        assertFalse(board.addOffer(user, pc, availableQuantity+1));

        //We check that the number of ads didn't change
        assertEquals(totalAds, board.getAdsList().size());

        //Now we retry to add an offer for the card pc but with a valid quantity
        assertTrue(board.addOffer(user, pc, randQ));

        //the number of ads now increased by 1
        assertEquals(totalAds+1, board.getAdsList().size());

        //We check that the ad with the highest id is the one we just created
        assertTrue(board.getAd(adMaxId+1) instanceof Offer);
        assertEquals(pc, ((Offer)board.getAd(adMaxId+1)).getPc());
        assertEquals(randQ, board.getAd(adMaxId+1).getQuantity());        
    }

    @Test
    public void testReservedQuantityChange(){

        //Usual setup
        String user = instance.getLoggedUser();
        PhysicalCard pc = cardDispenser.getRandomPhysicalCard();

        //We put in availableQuantity the current available quantity for the card pc
        int availableQuantity = instance.getCollection().getAvailableQuantity(pc);

        //We add to the collection a positive number of pc
        int randQ = rand.getRandInt(true);
        instance.getCollection().addCard(pc, randQ);

        //We check that the available quantity increased by randQ
        assertEquals
        (availableQuantity+randQ, instance.getCollection().getAvailableQuantity(pc));

        //We update the available quantity local variable
        availableQuantity = instance.getCollection().getAvailableQuantity(pc);

        //We put into the variable q a random number between 1 and availableQuantity
        int q = rand.getRandInt(availableQuantity-1)+1;

        //We try to insert an offer with an invalid quantity (in excess of the available one)
        //This try will fail
        assertFalse(board.addOffer(user, pc, availableQuantity+1));

        //Now we insert an offer with a valid quantity
        assertTrue(board.addOffer(user, pc, q));

        //We assert that the available quantity is decreased by q, 
        //the quantity of the offer newly created
        assertEquals
        (availableQuantity-q, instance.getCollection().getAvailableQuantity(pc));

        //We assert that the reserved quantity of the card pc for the user is exactly q
        assertEquals(q, board.getReservedCardQuantity(user, pc));
    }

    @Test
    public void testCkeckMatchingMethod(){
        //Setup
        tMaxId = TestManager.getMax(instance.getTransactions().getTransactionsList().keySet());
        String user = instance.getLoggedUser();
        ConcreteCard cc = cardDispenser.getRandomConcreteCard();
        Condition[] conditions = Condition.values();
        int requestCondition = rand.getRandInt(conditions.length, true);
        int q1 = rand.getRandInt(true);

        //We insert a request from a different user than the "logged" one, 
        //with the previously set up parameters 
        board.addRequest("TestUser2", cc, conditions[requestCondition], q1);

        //We insert in the collection of the logged user a PhysicalCard 
        //corresponding to the ConcreteCard requested by the other user, but with a lower
        //condition than the minimum requested
        int failedOfferCondition = rand.getRandInt(requestCondition);
        PhysicalCard pc = instance.getCards().getPhysCard(cc, conditions[failedOfferCondition]);
        instance.getCollection().addCard(pc, q1);

        //We then add an offer of the PhysicalCard added
        board.addOffer(user, pc, q1);

        //We see that the two ads do not match
        assertEquals(AdState.active, board.getAd(adMaxId+1).getState());
        assertEquals(AdState.active, board.getAd(adMaxId+2).getState());
        assertEquals(tMaxId, instance.getTransactions().getTransactionsList().size());

        //Now we repeat the process, but this time with a valid condition for the PhysicalCard
        int matchingOfferCondition = rand.getRandInt(conditions.length - requestCondition) + 
                                     requestCondition;
        pc = instance.getCards().getPhysCard(cc, conditions[matchingOfferCondition]);
        instance.getCollection().addCard(pc, q1);
        board.addOffer(user, pc, q1);

        //We see that the request (the one with id: adMaxId+1) 
        //and the last offer (the one with id: adMaxId+3) result pending, 
        //the state ads are in when there's a transaction that points to them
        //The first offer still results active
        assertEquals(AdState.pending, board.getAd(adMaxId+1).getState());
        assertEquals(AdState.active, board.getAd(adMaxId+2).getState());
        assertEquals(AdState.pending, board.getAd(adMaxId+3).getState());

        //We also assert that the checkMatching method in the Board class has succesfully 
        //created a new transaction
        assertEquals(tMaxId+1, instance.getTransactions().getTransactionsList().size());
    }

    @Test
    public void testActiveAds(){

        //We retrieve the list of all the ads in the system
        TreeMap<Integer, Ad> totalAds = board.getAdsList();

        //We create a copy of the list of all active ads
        TreeMap<Integer, Ad> activeAds = new TreeMap<>(board.getActiveAdsList());

        //For every active ad in the first list we check that 
        //it is also contained in the copy list we made, if it is then we remove it from it
        for(int id: totalAds.keySet()){
            if(totalAds.get(id).getState().equals(AdState.active)){
                if(activeAds.containsKey(id)){
                    activeAds.remove(id);
                }
                else
                    assertTrue(false);
            }
        }

        //Once we iterated the list of all ads, we check that there are no other
        //active ads in the copy list
        assertTrue(activeAds.isEmpty());
    }

    @Test
    public void testAdQuantityChange(){

        //Setup
        tMaxId = TestManager.getMax(instance.getTransactions().getTransactionsList().keySet());
        String user = instance.getLoggedUser();
        ConcreteCard cc = cardDispenser.getRandomConcreteCard();
        PhysicalCard pc = cardDispenser.getRandomPhysicalCard(cc.getId());
        int q = rand.getRandInt(true);
        instance.getCollection().addCard(pc, q);

        //We create two matching ads, an offer and a request
        board.addOffer(user, pc, q);
        int requestQ = rand.getRandInt(q, true);
        board.addRequest("TestUser2", cc, pc.getCondition(), requestQ);

        try{
            TransactionsManager t = instance.getTransactions();

            //When a transaction is created by two ads matching, 
            //both owners of these must accept the start of the transaction
            //We simulate the two users accepting it
            t.acceptTransaction(tMaxId+1, user);
            t.acceptTransaction(tMaxId+1, "TestUser2");

            //Then, after a sequence of adding/removing cards 
            //from one or the other list of the transaction, 
            //the users agree (both must do it) to conclude the transaction
            t.concludeTransactionWithSuccess(tMaxId+1, user);
            t.concludeTransactionWithSuccess(tMaxId+1, "TestUser2");

            //We check that the quantities in the ads 
            //interested in the closed transaction changed as planned:

            //The request should be concluded, 
            //since its quantity should have been fulfilled in the transaction
            assertEquals(AdState.concluded, board.getAd(adMaxId+2).getState());
            assertEquals(0, board.getAd(adMaxId+2).getQuantity());

            //The offer should return to an active state, and its remaining quantity should be
            //equals to the original one (q) minus the quantity of the request
            assertEquals(AdState.active, board.getAd(adMaxId+1).getState());
            assertEquals(q - requestQ, board.getAd(adMaxId+1).getQuantity());
        }catch(InvalidTransactionIdException | InvalidUsernameException | 
               InvalidTransactionStateException e){
            assertTrue(false);
        }
    }

    @After
    public void reset(){
        instance.resetManagers();
    }


    @AfterClass
    public static void clean(){
        FileResetter.resetFiles();
    }
}
