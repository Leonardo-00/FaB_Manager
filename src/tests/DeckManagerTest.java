package tests;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import Utils.Exceptions.DeckNotFoundException;
import Utils.Exceptions.InvalidDeckAccessException;
import package_card.DeckCard;
import package_card.EquipmentCard;
import package_card.HeroCard;
import package_card.WeaponCard;
import package_deck.Deck;
import package_deck.DeckList;
import package_deck.DeckManager;
import package_deck.DeckManager.EditableDeck;
import package_server.Server;
    
public class DeckManagerTest {

    static Server instance;
    static DeckManager dm;
    static CardDispenser cardDispenser;
    static RandGen rand;
    static int maxId;

    @BeforeClass
    public static void setup(){
        instance = TestManager.getInstance().getServer();
        cardDispenser = CardDispenser.getInstance();
        rand = RandGen.getInstance();
    }
    
    @Before
    public void setupDeckCollection(){
        dm = instance.getDeckManager();
        maxId = TestManager.getMax(dm.getAllDecks().keySet());
    }
        
    @Test
    public void testDeckImportExport() {

        //Example deck text to be imported in the system
        String testDeckString;
        {
        testDeckString= """
        Deck build - via https://fabdb.net :

        New deck

        Class: Brute
        Hero: Rhinar, Reckless Rampage
        Weapons: Mandible Claw, Romping Club
        Equipment: Barkbone Strapping, Scabskin Leathers

        (3) Alpha Rampage (red)
        (3) Breakneck Battery (red)
        (3) Pummel (red)
        (3) Savage Feast (red)
        (3) Bloodrush Bellow (yellow)
        (3) Bone Head Barrier (yellow)
        (3) Breakneck Battery (yellow)
        (3) Pummel (yellow)
        (3) Savage Feast (yellow)
        (3) Breakneck Battery (blue)
        (3) Pummel (blue)
        (3) Reckless Swing (blue)
        (3) Sand Sketched Plan (blue)
        (3) Savage Feast (blue)

        See the full deck at: https://fabdb.net/decks/PJQgxKbm/""";
        }
        //Calling the method for generating a deck 
        //starting from a given string from the user
        assertTrue(dm.importDeck(testDeckString));


        try {

            //With the method exportDeck you get a string in the same style of the imported one
            String exportedDeck = dm.exportDeck(maxId+1);

            //We check that the two string are equals, apart from the header, 
            //that are different for the two because one comes from the system and one does not
            assertEquals
            (exportedDeck.substring(exportedDeck.indexOf("\n\n")+1, exportedDeck.length()-1),
            testDeckString.substring(testDeckString.indexOf("\n\n")+1,
                                     testDeckString.indexOf("See the full deck")-2));
        } catch (DeckNotFoundException e) {
            e.printStackTrace();
            assertEquals(false, true);
        }
    }

    @Test
    public void testDeckCreation(){

        Deck d;

        //We get a random hero card from the helper class CardDispenser
        HeroCard hc = cardDispenser.getRandomHeroCard();

        //We create a deck with all the possible parameters, that is name, description and the hero
        String testDeckName = "Mazzo di test "+rand.getRandInt(true);
        dm.addDeck(testDeckName, "Un mazzo di test", hc);
        try{
            d = dm.getDeck(maxId+1);
            assertEquals(d.getOwner(), instance.getLoggedUser());
            assertEquals(d.getTitle(), testDeckName);
            assertEquals(d.getCards().getHeroCard(), hc);

            //Everytime we create a new deck, its default visibility is private
            assertFalse(d.isVisible());
            assertEquals(d.getDescription(), "Un mazzo di test");
        }catch(DeckNotFoundException e){

        }

        //Now we create a deck giving only the mandatory parameters, name and hero

        //The created deck will come with a default description
        dm.addDeck(testDeckName, hc);
        try{
            d = dm.getDeck(maxId+2);
            assertEquals(d.getDescription(), "Default description");
        }catch(DeckNotFoundException e){

        }
    }

    @Test
    public void testGettingInexistentDeck(){

        //If we call the getDeck method with an invalid index, for example the maxId present + 1, an Exception is thrown
        try{
            dm.getDeck(maxId+1);
            assertEquals(true, false);
        }
        catch(DeckNotFoundException e){
            assertEquals("No deck with id: "+(maxId+1)+ " found", e.getMessage());
        }
    }
    
    @Test
    public void testDeckCopy(){

        //The method copyDeck is used when the user wants to create a personal copy of a deck found in the public list
        try {

            //We take the first public deck for testing purposes
            Deck testDeck = dm.getPublicDecks().firstEntry().getValue();

            //We call the copyDeck method
            dm.copyDeck(testDeck.getSerialCode());

            //We retrieve the newly created deck, searching for the last entry of the user decks
            Deck newDeck = dm.getUserDecks().lastEntry().getValue();

            //We assert that the two variables are referring to different objects
            assertNotEquals(testDeck, newDeck);

            //We check that this deck has his appropriate id
            assertEquals(newDeck.getSerialCode(), maxId+1);

            //When you create a copy of a deck with name "x", its title will be "x - copy"
            assertEquals(newDeck.getTitle(), testDeck.getTitle()+" - copy");

            //The owner of the new deck must be the logged user
            assertEquals(newDeck.getOwner(), instance.getLoggedUser());

            //No differences in the description of the two decks
            assertEquals(testDeck.getDescription(), newDeck.getDescription());

            //The visibility of the new deck will, by design, be private
            assertFalse(newDeck.isVisible());

            //Every eventual comment of the copied deck won't be copied,
            //since the copy deck is a new instance, completely detached from the original
            assertTrue(newDeck.getComments().empty());

            //We must check that the copy is deep enough for the deck content to be separated objects,
            //but with the same content

            DeckList testList = testDeck.getCards(), copyList = newDeck.getCards();

            assertNotEquals(testList, copyList);

            assertEquals(testList.getHeroCard(), copyList.getHeroCard());

            TreeMap<DeckCard, Integer> testDeckCards = testList.getDeckCards(), 
                                       copyDeckCards = copyList.getDeckCards();

            assertEquals(testDeckCards, copyDeckCards);
            assertFalse(testDeckCards == copyDeckCards);

            for(Entry<DeckCard, Integer> e: testDeckCards.entrySet()){
                assertEquals(e.getValue(), copyDeckCards.get(e.getKey()));
                copyDeckCards.remove(e.getKey());
            }
            assertTrue(copyDeckCards.isEmpty());

            TreeSet<EquipmentCard> testEquipCards = testList.getEquipmentCards(), 
                                   copyEquipCards = copyList.getEquipmentCards();

            assertEquals(testEquipCards, copyEquipCards);
            assertFalse(testEquipCards == copyEquipCards);

            for(EquipmentCard ec: testEquipCards){
                assertTrue(copyEquipCards.contains(ec));
                copyEquipCards.remove(ec);
            }
            assertTrue(copyEquipCards.isEmpty());

            TreeMap<WeaponCard, Integer> testWeaponCards = testList.getWeaponCards(),
                                         copyWeaponCards = copyList.getWeaponCards();

            assertEquals(testWeaponCards, copyWeaponCards);
            assertFalse(testWeaponCards == copyWeaponCards);

            for(Entry<WeaponCard, Integer> e: testWeaponCards.entrySet()){
                assertEquals(e.getValue(), copyWeaponCards.get(e.getKey()));
                copyWeaponCards.remove(e.getKey());
            }
            assertTrue(copyWeaponCards.isEmpty());

        } catch (DeckNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testDeckOwnership(){

        //We retrieve the username of the logged user
        String loggedUser = instance.getLoggedUser();

        //We check that there is no deck in the public decks list owned by the logged user
        for(Deck d: dm.getPublicDecks().values())
            assertNotEquals(loggedUser, d.getOwner());

        //We check that every deck in the user decks list is owned by the logged user
        for(Deck d: dm.getUserDecks().values())
            assertEquals(loggedUser, d.getOwner());
    }

    @Test
    public void testDeckEdit(){

        //We create a test deck
        dm.addDeck("Test title 1", "Test description 1", 
                   CardDispenser.getInstance().getRandomHeroCard());

        try {

            //We save in a variable the last edit date of the deck before modifying it
            Date preEditDate = (dm.getDeck(maxId+1)).getLastEdit();

            //We simulate modifications to the newly created deck
            EditableDeck ed = dm.editDeck(maxId+1);

            //We insert this call to sleep() just to ensure that the two dates 
            //will be distinguishable
            Thread.sleep(1);  
                                     
            ed.editDeckTitle("New title");
            ed.saveChanges();

            //We retrieve the deck, after it has been modified
            Deck editedDeck = dm.getDeck(maxId+1);

            //We check that the current last edit date comes after 
            //the date we first saved in the variable
            assertTrue(editedDeck.getLastEdit().after(preEditDate));

            //We check that the modification was made permanent
            assertEquals("New title", editedDeck.getTitle());
        }
        catch (DeckNotFoundException | InvalidDeckAccessException | InterruptedException e) {
            assertTrue(false);
        }
    }
    
    @After
    public void reset(){
        instance.resetManagers();
    }
}
