package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import Utils.Exceptions.InsufficientQuantityException;
import Utils.Exceptions.InvalidAdException;
import Utils.Exceptions.InvalidAdIdException;
import Utils.Exceptions.InvalidQuantityRemovalException;
import Utils.Exceptions.InvalidTransactionIdException;
import Utils.Exceptions.InvalidTransactionStateException;
import Utils.Exceptions.InvalidUsernameException;
import Utils.constants.TransactionState;
import package_board.Board;
import package_card.ConcreteCard;
import package_card.Condition;
import package_card.PhysicalCard;
import package_collection_card.Collection;
import package_server.Server;
import package_transaction.Transaction;
import package_transaction.TransactionsManager;



public class TransactionManagerTest {

    static Server instance;
    static TransactionsManager tm;
    static RandGen rand;
    static CardDispenser cardDispenser;
    static int maxId;

    @BeforeClass
    public static void setup(){
        instance = TestManager.getInstance().getServer();
        rand = RandGen.getInstance();
        cardDispenser = CardDispenser.getInstance();
    }

    @Before
    public void setTransactionManager(){
        tm = instance.getTransactions();
        maxId = TestManager.getMax(tm.getTransactionsList().keySet());
    }

    @Test
    public void testInexistentTransactionQuery(){
        try{
            tm.getTransaction(maxId+1);
            assertTrue(false);
        }
        catch(InvalidTransactionIdException e){
            assertEquals("There is not a transaction with id: "+(maxId+1), e.getMessage());
        }
    }

    @Test
    public void testManualTransactionInsertion(){
        String user1 = instance.getLoggedUser(), user2 = "TestUser2";
        try {
            //We try to insert a transaction giving an inexistent username
            //This will cause an exception to be thrown
            tm.addManualTransaction("TestUserX");
            assertTrue(false);
        } catch (InvalidUsernameException e) {
            assertEquals("Invalid username: TestUserX", e.getMessage());
        }
        try{
            //Now we try to insert a transaction with the second test user
            tm.addManualTransaction(user2);

            //We check that the two users in the new transaction are the logged one as first user 
            //and "TestUser2" as the second user
            Transaction t = tm.getTransaction(maxId+1);
            assertEquals(user1, t.getFirstUser());
            assertEquals(user2, t.getSecondUser());

            //When a transaction is created manually it results in a state 
            //called 'firstUserConfirmed'
            //In fact the first user is the one that requested it, so he has already confirmed
            //To become active, the transaction must be accepted by the second user
            assertEquals(TransactionState.firstUserConfirmed, t.getState());

            //A manually created transaction doesn't involve any ads, 
            //so both the ads' ids are "null"
            assertTrue(t.getFirstAdID() == null);
            assertTrue(t.getSecondAdID() == null);
            
            //The lists of the transactions will both be empty
            assertTrue(t.getFirstList().getCardsList().isEmpty());
            assertTrue(t.getSecondList().getCardsList().isEmpty());

            //When the second user accepts the transaction, this becomes active
            tm.acceptTransaction(maxId+1, user2);
            assertEquals(TransactionState.active, t.getState());

        }catch (InvalidUsernameException | InvalidTransactionIdException |
                InvalidTransactionStateException e){
            assertTrue(false);
        }
    }

    @Test
    public void testAcceptedAdTransactionInsertion(){

        //Setup
        //We create a request from the second test user 
        //for a quantity q of a ConcreteCard cc with a minimalCondition cond
        Board b = instance.getBoard();
        Collection coll = instance.getCollection();
        int adMaxId = TestManager.getMax(b.getAdsList().keySet());
        ConcreteCard cc = cardDispenser.getRandomConcreteCard();
        Condition cond = Condition.values()[rand.getRandInt(Condition.values().length)];
        int q = rand.getRandInt(true)+1, 
            q2 = rand.getRandInt(q, true);
        b.addRequest("TestUser2", cc, cond, q);
        
        try{
            //We simulate the acceptance of the request just made
            //This will fail, cause the logged user doesn't have any card 
            //that satisfies the request from the second user
            b.acceptAd(adMaxId+1);
            assertTrue(false);
        }catch(InvalidAdException | InvalidAdIdException e){
            assertTrue(e instanceof InvalidAdException);
            assertEquals
            ("The user doesn't own enough cards to accept the ad with id: "+(adMaxId+1), e.getMessage());
        }
        //We now add to the collection of the logged user a quantity q2 (strictly lower than q)
        //of a PhysycalCard that satisfies the request
        PhysicalCard pc = instance.getCards().getPhysCard(cc, cond);
        coll.addCard(pc, q2);

        try{
            //We retry to accept the request, this time with success
            b.acceptAd(adMaxId+1);
            Transaction t = tm.getTransaction(maxId+1);

            //We check that the first user is the logged user
            //and that his list of cards in the transaction consists only 
            //of a quantity q2 of the card pc
            assertEquals(instance.getLoggedUser(), t.getFirstUser());
            assertTrue(t.getFirstList().getCardQuantity(pc) == q2);
            assertEquals(1, t.getFirstList().getCardsList().size());

            //There is no ad from the first user, so the first ad id is "null"
            assertTrue(t.getFirstAdID() == null);

            //The second user of the transaction is the second test user
            assertEquals("TestUser2", t.getSecondUser());

            //The id of the ad of the second user refers to the request
            //just created in the setup
            assertTrue(t.getSecondAdID().intValue() == adMaxId+1);

            //The list of the second user is empty
            assertTrue(t.getSecondList().getCardsList().isEmpty());

            //The state of the transaction is 'firstUserConfirmed',
            //since the first user is the one that accepted the ad
            //hence he already accept the transaction
            assertEquals(TransactionState.firstUserConfirmed, t.getState());

            tm.acceptTransaction(maxId+1, "TestUser2");
            assertEquals(TransactionState.active, t.getState());
        }catch(InvalidAdException | InvalidAdIdException | InvalidTransactionIdException |
               InvalidTransactionStateException e){
            System.out.println(e instanceof InvalidQuantityRemovalException);
            assertTrue(false);
        }
    }

    @Test
    public void testMatchingAdsTransactionInsertion(){

        //Setup
        //We create two ads, one from the logged user and one from the second test user
        //These ads will match, in the sense that the PhysicalCard of the offer
        //will satisfy the card wanted of the request and its minimum condition
        Board b = instance.getBoard();
        int adMaxId = TestManager.getMax(b.getAdsList().keySet());
        String user = instance.getLoggedUser();
        ConcreteCard cc = cardDispenser.getRandomConcreteCard();
        Condition[] conditions = Condition.values();
        int requestCondition = rand.getRandInt(conditions.length, true);
        int q1 = rand.getRandInt(true);
        b.addRequest("TestUser2", cc, conditions[requestCondition], q1);
        int matchingOfferCondition = rand.getRandInt(conditions.length - requestCondition) 
                                     + requestCondition;
        PhysicalCard pc = instance.getCards().getPhysCard(cc, conditions[matchingOfferCondition]);
        instance.getCollection().addCard(pc, q1);
        b.addOffer(user, pc, q1);

        try{
            //We take the transaction created by the ads matching
            Transaction t = tm.getTransaction(maxId+1);

            //A transaction created in this way starts in the 'fromAdMatch' state
            //To go to the active state both users must accept it
            //When one of the two users accepts it, he becomes the first user and 
            //the transaction goes into the 'firstUserAccepted' state, then when the 
            //second user accepts, it goes to the 'active' state
            assertEquals(TransactionState.fromAdsMatch, t.getState());

            //A transaction created by a matching puts the user owner of the offer as first user
            //and the other as the second

            //We check that the ids of the transaction correspond to the two new ads created
            assertTrue(t.getFirstAdID().intValue() == adMaxId+2);
            assertTrue(t.getSecondAdID().intValue() == adMaxId+1);

            //We check that the list of the request owner is empty, while the list of the
            //offer owner contains only the card pc and with a quantity of the minumum between
            //the quantity of the offer and the quantity of the request (in this case they are the same)
            assertTrue(t.getSecondList().getCardsList().isEmpty());
            assertEquals(1, t.getFirstList().getCardsList().size());
            assertEquals(q1, t.getFirstList().getCardQuantity(pc));
        }
        catch(InvalidTransactionIdException e){
            
        }
        
    }

    @Test
    public void testCardQuantityQuery(){

        //Setup
        //We get a PhysicalCard pc, the logged user and register in q 
        //the quantity of pc the user currently has in transactions
        //We then randomly generate a second quantity q2
        PhysicalCard pc = cardDispenser.getRandomPhysicalCard();
        String user = instance.getLoggedUser();
        int q = tm.getCardQuantity(user, pc);
        int q2 = rand.getRandInt();
        try {
            //We create a manual transaction
            tm.addManualTransaction("TestUser2");
            //We add a quantity of q2 of pc to the user collection
            instance.getCollection().addCard(pc, q2);
            //We try to add the same quantity to the transaction list of the first user
            tm.addCardToTransaction(maxId+1, user, pc, q2);
            assertEquals(q + q2, tm.getCardQuantity(user, pc));
        } catch (InvalidUsernameException | InvalidTransactionIdException |
                 InvalidTransactionStateException | InsufficientQuantityException e) {
            //We verify that an InvalidTransactionStateException was correctly thrown
            assertTrue(e instanceof InvalidTransactionStateException);
            //This happens because the transaction still isn't active, 
            //cause the second user has to accept it
        }
        try{
            //We make the second user accept the transaction, that will now be active
            tm.acceptTransaction(maxId+1, "TestUser2");
            //We try again to add the quantity
            tm.addCardToTransaction(maxId+1, user, pc, q2);
            //And verify that this time the total quantity of pc 
            //the user now has in transaction is q + q2
            assertEquals(q + q2, tm.getCardQuantity(user, pc));
        }catch (InvalidUsernameException | InvalidTransactionIdException |
                InvalidTransactionStateException | InsufficientQuantityException e){
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
