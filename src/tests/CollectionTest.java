package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import java.util.TreeMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import Utils.Exceptions.InvalidQuantityRemovalException;
import XMLParsing.XMLParser;
import package_card.CardsList;
import package_card.ConcreteCard;
import package_card.PhysicalCard;
import package_collection_card.Collection;
import package_collection_card.CollectionCardsList;
import package_server.Server;

public class CollectionTest {

    static Server instance;
    static Collection coll;
    static RandGen rand;
    static CardDispenser cardDispenser;

    @BeforeClass
    public static void setup() throws IOException{
        instance = TestManager.getInstance().getServer();
        rand = RandGen.getInstance();
        cardDispenser = CardDispenser.getInstance();
    }

    @Before
    public void setupCollection(){
        coll = instance.getCollection();
    }

    @Test
    public void testCardInsertion(){

        //We ask the card dispenser for a random PhysicalCard
        PhysicalCard pc = cardDispenser.getRandomPhysicalCard();

        //We save the current quantity for pc in the user collection
        int q = coll.getQuantity(pc);

        //We generate a random positive number
        int dq = rand.getRandInt(true);

        //We call the addCard method for pc with the generated quantity
        coll.addCard(pc, dq);

        //We assert that the quantity for pc is now equal to q + dq
        assertEquals(q+dq, coll.getQuantity(pc));
    }

    @Test
    public void testCardRemoval(){

        //We setup the collection so that it contains a positive 
        //quantity for the physical card pc
        PhysicalCard pc = cardDispenser.getRandomPhysicalCard();
        int q = coll.getQuantity(pc), dq;
        dq = rand.getRandInt(true);
        coll.addCard(pc,dq);

        //Now the total quantity of the card pc is q + dq
        try {
            //We try to remove a quantity that surpasses the total quantity
            //this way an InvalidQuantityRemovalException will be thrown
            coll.removeCard(pc, q+dq+1);
            assertTrue(false);
        } catch (InvalidQuantityRemovalException e) {
            assertEquals
            ("There's not enough available quantity to remove of the selected card", 
             e.getMessage());
        }
        //The available quantity for a card is lower than the total one when the user 
        //has an offer for it or has it in a transaction list

        int availableQuantity = coll.getAvailableQuantity(pc);
        try {
            //We try to remove a quantity that surely surpasses the available quantity
            //an InvalidQuantityRemovalException will be thrown this time too
            coll.removeCard(pc, availableQuantity+dq+1);
            assertTrue(false);
        } catch (InvalidQuantityRemovalException e) {
            assertEquals
            ("There's not enough available quantity to remove of the selected card",
            e.getMessage());
        }
        try {
            //Lastly we try to remove a quantity that is surely available, 
            //the quantity that we just added
            coll.removeCard(pc, dq);
            assertEquals(coll.getQuantity(pc), q);
        } catch (InvalidQuantityRemovalException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testCardsExchange(){

        //We get 2 random instances of PhysicalCard
        PhysicalCard pc1 = cardDispenser.getRandomPhysicalCard();
        PhysicalCard pc2 = cardDispenser.getRandomPhysicalCard();

        //We put in q1 the quantity of pc1 in the user collection
        int q1 = coll.getQuantity(pc1);

        //We repeat the process for pc2 and we put the quantity in q2
        int q2 = coll.getQuantity(pc2);

        //We generate a random quantity to be added to the user collection for pc1, 
        //so that we know this is surely available
        int dq1 = rand.getRandInt(true);
        coll.addCard(pc1, dq1);

        //We generate another random quantity, this one to be added in the collection of another user
        int dq2 = rand.getRandInt(true);

        //To do this we must have access to the XMLParser class
        XMLParser parser = new XMLParser(instance);
        parser.insertCardInCollection("TestUser2", pc2, dq2);   
        //This method insertCardInCollection() is for testing purposes only

        //We simulate an exchangeList putting manually the cards in it
        TreeMap<String, CardsList> exchangeList = new TreeMap<>();

        TreeMap<ConcreteCard, CollectionCardsList> preExchangeUserCollection = coll.getCollection();

        //We first try with an (obviously) invalid value for pc1
        exchangeList.put(coll.getUser(), new CardsList(pc1, q1+dq1+1));
        exchangeList.put("TestUser2", new CardsList(pc2,dq2));

        //If we try to execute the exchange the method will fail and return false
        assertFalse(coll.exchangeCardsInCollections(coll.getUser(), "TestUser2", exchangeList));

        //We assert that the user collection didn't get modified during the failed exchange
        assertEquals(preExchangeUserCollection, coll.getCollection());

        //We remove the quantity in excess from the exchangeList
        try {
            exchangeList.get(coll.getUser()).removeCard(pc1, q1+1);
        } catch (InvalidQuantityRemovalException e){
        }

        //We now assert that the method exchangeCardInCollection() will complete the exchange and return true
        assertTrue(coll.exchangeCardsInCollections(coll.getUser(), "TestUser2", exchangeList));

        //We check that the quantities for pc1 and pc2 in the logged user collection are what they should be
        assertEquals(q1, coll.getQuantity(pc1));
        assertEquals(q2+dq2,coll.getQuantity(pc2));
    }

    @After
    public void reset(){
        instance.resetManagers();
    }

    @AfterClass
    public static void clean() throws IOException{
        FileResetter.resetFiles();
    }
}


