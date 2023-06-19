package XMLParsing;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import Utils.Exceptions.CardArtException;
import Utils.Exceptions.CardColorException;
import Utils.Exceptions.CardRarityException;
import Utils.Exceptions.CorruptedCardException;
import Utils.Exceptions.CorruptedDeckException;
import Utils.Exceptions.IllegalCardAdditionException;
import Utils.Exceptions.InvalidAdException;
import Utils.Exceptions.InvalidTransactionStateException;
import Utils.Exceptions.InvalidUsernameException;
import Utils.constants.AdState;
import Utils.constants.DataPath;
import Utils.constants.TransactionState;
import package_board.Ad;
import package_board.Offer;
import package_board.Request;
import package_card.AbstractCard;
import package_card.ArtVersion;
import package_card.CardManager;
import package_card.Color;
import package_card.ConcreteCard;
import package_card.Condition;
import package_card.DeckCard;
import package_card.EquipmentCard;
import package_card.ExpansionCard;
import package_card.HeroCard;
import package_card.PhysicalCard;
import package_card.Rarity;
import package_card.WeaponCard;
import package_collection_card.Collection;
import package_collection_card.CollectionCardsList;
import package_deck.Deck;
import package_interaction.Comment;
import package_server.Server;
import package_transaction.Transaction;
import package_transaction.TransactionsManager;
import package_transaction.TransactionsManager.TransactionBuilder;
import package_user.Contact;
import package_user.UserInfo;
import package_user.UsersManager;


public class XMLParser{

    private Server server;

    public XMLParser(Server instance){
        server = instance;
    }


    public void assignNewCardsID(){
        // Instantiate the Factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(new File(DataPath.cardsFile));

            // optional, but recommended
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            int currentConcrID=1, newConcrCards = 0, currentIncrement = 0;
            NodeList abstractList = doc.getElementsByTagName("card");
            try{
                for (int temp = 0; temp < abstractList.getLength(); temp++) {

                    Node node = abstractList.item(temp);

                    if (node.getNodeType() == Node.ELEMENT_NODE) {

                        Element element = (Element) node;

                        NodeList expansionList = ((Element)(element.getElementsByTagName("expansionCards").item(0))).getElementsByTagName("expCard");

                        for(int tmp = 0; tmp < expansionList.getLength(); tmp++){
                            Node node1 = expansionList.item(tmp);
                            if(node1.getNodeType() == Node.ELEMENT_NODE){
                                Element element1 = (Element)node1;

                                NodeList concreteList = ((Element)(element1.getElementsByTagName("concreteCards").item(0))).getElementsByTagName("concrCard");

                                for(int tmp2 = 0; tmp2 < concreteList.getLength(); tmp2++){

                                    Node node2 = concreteList.item(tmp2);
                                    if(node2.getNodeType() == Node.ELEMENT_NODE){
                                        Element element2 = (Element)node2;
                                        if(element2.getAttributeNode("id") != null){
                                            if(currentIncrement > 0){
                                                increaseCardsID(currentConcrID, currentIncrement);
                                                currentIncrement = 0;
                                            }
                                            currentConcrID = Integer.parseInt(element2.getAttribute("id"))+newConcrCards;
                                            element2.setAttribute("id", ""+(currentConcrID++));
                                        }
                                        else{
                                            currentIncrement++;
                                            element2.setAttribute("id", ""+currentConcrID++);
                                            newConcrCards++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            catch(NumberFormatException e){
                server.appendStringToLog(e.getMessage());
            }
                
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(doc);
            StreamResult streamResult = new StreamResult(new File(DataPath.cardsFile));
            transformer.transform(domSource, streamResult);

            
        }
        catch (ParserConfigurationException | SAXException | DOMException | IOException | TransformerException e) {
            server.appendStringToLog(e.getMessage());
        }
    }

    private void increaseCardsID(int ID, int increment) throws IOException{

        //This method increases the id of all the abstract cards starting from the given ID;
        //It gets invoked everytime a new abstract card is added in the cards.xml file


        //First section:  increasement in the ads.xml file
        Path path = Paths.get(DataPath.adsFile);
        Charset charset = StandardCharsets.UTF_8;

        StringBuffer content = new StringBuffer(new String(Files.readAllBytes(path), charset));
        StringBuffer newContent = new StringBuffer();
        String startDelimiter = StringConstants.adsStart;
        String endDelimiter = StringConstants.adsEnd;
        while(content.toString().contains(startDelimiter)){
            newContent.append(content.substring(0, content.indexOf(startDelimiter)));
            content = new StringBuffer(content.substring(content.indexOf(startDelimiter), content.length()));
            int q = Integer.parseInt(content.substring(startDelimiter.length(), content.indexOf(endDelimiter)));
            if(q >= ID)
                content.replace(startDelimiter.length(), content.indexOf(endDelimiter), ""+(q+increment));
            newContent.append(content.substring(0, content.indexOf(endDelimiter)));
            content = new StringBuffer(content.substring(content.indexOf(endDelimiter), content.length()));
        }
        newContent.append(content.substring(0, content.length()));
        Files.write(path, newContent.toString().getBytes(charset));



        //Second section:  increasement in the user_collections.xml file
        path = Paths.get(DataPath.collectionFile);
        charset = StandardCharsets.UTF_8;

        content = new StringBuffer(new String(Files.readAllBytes(path), charset));
        newContent = new StringBuffer();
        startDelimiter = StringConstants.collectionStart;
        endDelimiter = StringConstants.collectionEnd;
        while(content.toString().contains(startDelimiter)){
            newContent.append(content.substring(0, content.indexOf(startDelimiter)));
            content = new StringBuffer(content.substring(content.indexOf(startDelimiter), content.length()));
            int q = Integer.parseInt(content.substring(startDelimiter.length(), content.indexOf(endDelimiter)));
            if(q >= ID)
                content.replace(startDelimiter.length(), content.indexOf(endDelimiter), ""+(q+increment));
            newContent.append(content.substring(0, content.indexOf(endDelimiter)));
            content = new StringBuffer(content.substring(content.indexOf(endDelimiter), content.length()));
        }
        newContent.append(content.substring(0, content.length()));
        Files.write(path, newContent.toString().getBytes(charset));

    }
    
    public void loadCards(TreeMap<Integer,AbstractCard> aList, ArrayList<ExpansionCard> eList,
                          TreeMap<Integer,ConcreteCard> cList) throws CorruptedCardException{
        // Instantiate the Factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(new File(DataPath.cardsFile));

            // optional, but recommended
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();
            // get <staff>
            NodeList abstractList = doc.getElementsByTagName("card");
            for (int temp = 0; temp < abstractList.getLength(); temp++) {

                Node node = abstractList.item(temp);

                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    Element element = (Element) node;

                    try {
                        AbstractCard ac;
                        {
                            int id = Integer.parseInt(element.getAttribute("id"));
                            String name = element.getElementsByTagName("name").item(0).getTextContent();
                            String type = element.getElementsByTagName("type").item(0).getTextContent();
                            String effect = element.getElementsByTagName("effect").item(0).getTextContent();

                            switch(element.getAttribute("cardType")){

                                case "Deck": {
                                    int cost = Integer.parseInt(element.getElementsByTagName("cost").item(0).getTextContent());
                                    Color c;
                                    {
                                        String color = element.getElementsByTagName("color").item(0).getTextContent();
                                        c = switch (color) {
                                            case "R" -> Color.red;
                                            case "Y" -> Color.yellow;
                                            case "B" -> Color.blue;
                                            case "U" -> Color.undefined;
                                            default -> throw new CardColorException();
                                        };
                                    }
                                    boolean p = Boolean.parseBoolean(element.getElementsByTagName("pitchable").item(0).getTextContent());
                                    int attack = Integer.parseInt(element.getElementsByTagName("attack").item(0).getTextContent());
                                    int defence = Integer.parseInt(element.getElementsByTagName("defence").item(0).getTextContent());
                                    ac = new DeckCard(id, name, type, effect, cost, c, p, attack, defence);
                                    break;
                                }

                                case "Hero": {
                                    String Class = element.getElementsByTagName("class").item(0).getTextContent();
                                    String talent = element.getElementsByTagName("talent").item(0).getTextContent();
                                    int health = Integer.parseInt(element.getElementsByTagName("health").item(0).getTextContent());
                                    int intellect = Integer.parseInt(element.getElementsByTagName("intellect").item(0).getTextContent());
                                    boolean adult = !type.contains("Young");
                                    ArrayList<String> lt = new ArrayList<>();

                                    NodeList legalTypesList = ((Element)(element.getElementsByTagName("legalTypes").item(0))).getElementsByTagName("lt");

                                    for(int tmp = 0; tmp < legalTypesList.getLength(); tmp++)
                                        lt.add(legalTypesList.item(tmp).getTextContent());

                                    ac = new HeroCard(id, name, Class, talent, type, effect, health, intellect, adult, lt);
                                    break;
                                }

                                case "Weapon": {
                                    int attack = Integer.parseInt(element.getElementsByTagName("attack").item(0).getTextContent());
                                    ac = new WeaponCard(id, name, type, effect, attack);
                                    break;
                                }

                                case "Equipment": {
                                    int defence = Integer.parseInt(element.getElementsByTagName("defence").item(0).getTextContent());
                                    ac = new EquipmentCard(id, name, type, effect, defence);
                                    break;
                                }

                                default: {
                                    throw new CorruptedCardException("The card type of the card with id: "+id+" is not one of the accepted ones");
                                }
                            }
                        }
                        aList.put(ac.getID(),ac);

                        NodeList expansionList = ((Element)(element.getElementsByTagName("expansionCards").item(0))).getElementsByTagName("expCard");

                        for(int tmp = 0; tmp < expansionList.getLength(); tmp++){
                            Node node1 = expansionList.item(tmp);
                            if(node1.getNodeType() == Node.ELEMENT_NODE){
                                Element element1 = (Element)node1;

                                String exp = element1.getElementsByTagName("expansion").item(0).getTextContent();
                                int id = Integer.parseInt(element1.getElementsByTagName("id").item(0).getTextContent());
                                Rarity r;
                                {
                                    String rarity = element1.getElementsByTagName("rarity").item(0).getTextContent();
                                    r = switch (rarity) {
                                        case "T" -> Rarity.Token;
                                        case "C" -> Rarity.Common;
                                        case "R" -> Rarity.Rare;
                                        case "S" -> Rarity.SuperRare;
                                        case "M" -> Rarity.Majestic;
                                        case "L" -> Rarity.Legendary;
                                        case "F" -> Rarity.Fabled;
                                        case "P" -> Rarity.Promo;
                                        default -> throw new CardRarityException();
                                    };
                                }
                                ExpansionCard eC = new ExpansionCard(ac, exp, id, r);
                                eList.add(eC);

                                NodeList concreteList = ((Element)(element1.getElementsByTagName("concreteCards").item(0))).getElementsByTagName("concrCard");

                                for(int tmp2 = 0; tmp2 < concreteList.getLength(); tmp2++){

                                    Node node2 = concreteList.item(tmp2);
                                    if(node2.getNodeType() == Node.ELEMENT_NODE){
                                        Element element2 = (Element)node2;

                                        int cID = Integer.parseInt(element2.getAttribute("id"));
                                        String foil = element2.getElementsByTagName("foilType").item(0).getTextContent();
                                        ArtVersion a;
                                        {
                                            String art = element2.getElementsByTagName("variantArt").item(0).getTextContent();
                                            a = switch (art) {
                                                case "Normal" -> ArtVersion.Normal;
                                                case "Variant" -> ArtVersion.Variant;
                                                case "Extended" -> ArtVersion.Extended;
                                                default -> throw new CardArtException();
                                            };
                                        }
                                        cList.put(cID, new ConcreteCard(cID, eC, foil, a));
                                    }
                                }
                            }
                        }
                    }
                    catch(NumberFormatException| CardColorException | CardRarityException | CardArtException e){

                    }
                }
            }
        }
        catch (ParserConfigurationException | SAXException | IOException e) {
            server.appendStringToLog(e.getMessage());
        }
    }

    public UserInfo getInfoByCredentials(String userID, String pw){
        // Instantiate the Factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        
        UserInfo uI = null;
        try {
            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(new File(DataPath.userData));

            // optional, but recommended
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();
            // get <staff>
            NodeList usersList = doc.getElementsByTagName("user");
            Element element, element1;
            Node node, node1;
            for (int temp = 0; temp < usersList.getLength(); temp++) {

                node = usersList.item(temp);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    element = (Element) node;
                    if(element.getElementsByTagName("userID").item(0).getTextContent().equals(userID) && element.getElementsByTagName("password").item(0).getTextContent().equals(pw)){
                        uI = new UserInfo(element.getAttribute("username"), pw);

                        NodeList contactsList = ((Element)(element.getElementsByTagName("contacts").item(0))).getElementsByTagName("contact");
                        for(int tmp = 0; tmp < contactsList.getLength(); tmp++){
                            node1 = contactsList.item(tmp);

                            if(node1.getNodeType() == Node.ELEMENT_NODE){
                                element1 = (Element)node1;
                                uI.addContact(new Contact(element1.getTextContent(), element1.getAttribute("type")));
                            }
                        }
                    }
                }
            }
        }
        catch (ParserConfigurationException | SAXException | IOException e) {
            server.appendStringToLog(e.getMessage());
        }
        return uI;
    }

    public TreeSet<String> loadUsernames(){
        // Instantiate the Factory
        TreeSet<String> usersList = new TreeSet<>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(new File(DataPath.userData));

            // optional, but recommended
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();
            NodeList uList = doc.getElementsByTagName("user");
            for (int temp = 0; temp < uList.getLength(); temp++) {
                Node node = uList.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    usersList.add(element.getAttribute("username"));
                }
            }
        }
        catch (ParserConfigurationException | SAXException | IOException e) {
            server.appendStringToLog(e.getMessage());
        }
        return usersList;
    }

    public TreeMap<Integer, Deck> loadDecks() throws CorruptedDeckException{
        
        TreeMap<Integer, Deck> decks = new TreeMap<>();
        
        CardManager cards = server.getCards();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(new File(DataPath.decksFile));    //DataPath.decksFile

            // optional, but recommended
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            int maxId = 0;
            boolean userDeck = false, publicVis= false;
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            NodeList dList = doc.getElementsByTagName("deck");
            Deck d;
            for (int temp = 0; temp < dList.getLength(); temp++){

                Node node = dList.item(temp);

                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    Element element = (Element) node;

                    
                    try {
                        int id = Integer.parseInt(element.getAttribute("id"));
                        maxId = (id>maxId)?id:maxId;
                        userDeck = element.getElementsByTagName("username").item(0).getTextContent().equals(server.getUsers().getUserInfo().getUsername());
                        publicVis = Boolean.parseBoolean(element.getElementsByTagName("public").item(0).getTextContent());
                        if(userDeck||publicVis){
                            String username = element.getElementsByTagName("username").item(0).getTextContent();
                            String title = element.getElementsByTagName("title").item(0).getTextContent();
                            String description = element.getElementsByTagName("description").item(0).getTextContent();
                            Date lastEdit = formatter.parse(element.getElementsByTagName("lastEdit").item(0).getTextContent());
                            
                            AbstractCard card = cards.getAbstractCard(Integer.parseInt(element.getElementsByTagName("heroCard").item(0).getTextContent()));
                            HeroCard hero;
                            if(card instanceof HeroCard)
                                hero = (HeroCard)card;
                            else   
                                throw new CorruptedDeckException("The deck with id: "+id+" is corrupted: the id for the hero card doesn't correspond to a HeroCard");

                            d = new Deck(id, username, title, description, hero, publicVis, lastEdit);

                            NodeList cardsList = element.getElementsByTagName("card");
                            for(int tmp = 0; tmp < cardsList.getLength(); tmp++){
                                card = cards.getAbstractCard(Integer.parseInt(((Element)cardsList.item(tmp)).getElementsByTagName("id").item(0).getTextContent()));
                                int quantity = Integer.parseInt(((Element)cardsList.item(tmp)).getElementsByTagName("quantity").item(0).getTextContent());
                                if(!(card instanceof HeroCard))
                                    d.addCard(card, quantity);
                                else
                                    throw new CorruptedDeckException("The deck with id: "+id+" is corrupted: cannot add in the deck a HeroCard");
                            }
                            cardsList = element.getElementsByTagName("equipmentCardID");
                            for(int tmp = 0; tmp < cardsList.getLength(); tmp++){
                                card = cards.getAbstractCard(Integer.parseInt(((Element)cardsList.item(tmp)).getTextContent()));
                                if(!(card instanceof HeroCard))
                                    d.addCard(card, 1);
                                else
                                    throw new CorruptedDeckException("The deck with id: "+id+" is corrupted: cannot add in the deck a HeroCard");
                            }
                            cardsList = element.getElementsByTagName("weaponCard");
                            for(int tmp = 0; tmp < cardsList.getLength(); tmp++){
                                card = cards.getAbstractCard(Integer.parseInt(((Element)cardsList.item(tmp)).getElementsByTagName("id").item(0).getTextContent()));
                                int quantity = Integer.parseInt(((Element)cardsList.item(tmp)).getElementsByTagName("quantity").item(0).getTextContent());
                                if(!(card instanceof HeroCard))
                                    d.addCard(card, quantity);
                                else
                                    throw new CorruptedDeckException("The deck with id: "+id+" is corrupted: cannot add in the deck a HeroCard");
                            }

                            NodeList comments = element.getElementsByTagName("comment"), replies;
                            Element comment, reply;
                            for(int tmp = 0; tmp < comments.getLength(); tmp++){
                                comment = ((Element)comments.item(tmp));
                                String user = comment.getElementsByTagName("user").item(0).getTextContent();
                                String text = comment.getElementsByTagName("text").item(0).getTextContent();
                                Date dOC = formatter.parse(comment.getElementsByTagName("dateOfCreation").item(0).getTextContent());
                                Comment c = new Comment(user, text, dOC);
                                replies = comment.getElementsByTagName("reply");
                                for(int tmp2 = 0; tmp2 < replies.getLength(); tmp2++){
                                    reply = ((Element)replies.item(tmp2));
                                    user = reply.getElementsByTagName("user").item(0).getTextContent();
                                    text = reply.getElementsByTagName("text").item(0).getTextContent();
                                    dOC = formatter.parse(reply.getElementsByTagName("dateOfCreation").item(0).getTextContent());
                                    c.replyComment(new Comment(user, text, dOC));
                                }
                                d.addComment(c);
                            }
                            decks.put(d.getSerialCode(), d);
                        }
                    }
                    catch(NumberFormatException | ParseException | IllegalCardAdditionException e){
                        if(e instanceof IllegalCardAdditionException)
                            System.out.println(e.getMessage());
                    }
                }
            }
            server.getDeckManager().setNextId(maxId+1);
        }
        catch (ParserConfigurationException | SAXException | IOException e) {
            server.appendStringToLog(e.getMessage());
        }
        return decks;
    }

    public void saveDecks(TreeMap<Integer, Deck> decks){

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(new File(DataPath.decksFile));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Element root = doc.getDocumentElement();

            NodeList decksList = root.getElementsByTagName("deck");
            int index = 0;
            Element element;
            boolean saved;
            for(Entry<Integer, Deck> e: decks.entrySet()){
                saved = false;
                while(!saved){
                    element = (Element)decksList.item(index);
                    if(element == null){
                        Element deckElement = doc.createElement("deck");
                        root.appendChild(doc.createTextNode("\t"));
                        root.appendChild(deckElement);
                        insertRow(doc, root, 0);
                        deckElement.setAttribute("id", ""+e.getValue().getSerialCode());
                        insertRow(doc, deckElement, 2);
                        element = doc.createElement("username");
                        element.appendChild(doc.createTextNode(e.getValue().getOwner()));
                        deckElement.appendChild(element);
                        insertRow(doc, deckElement, 2);
                        element = doc.createElement("title");
                        element.appendChild(doc.createTextNode(e.getValue().getTitle()));
                        deckElement.appendChild(element);
                        insertRow(doc, deckElement, 2);
                        element = doc.createElement("description");
                        element.appendChild(doc.createTextNode(e.getValue().getDescription()));
                        deckElement.appendChild(element);
                        insertRow(doc, deckElement, 2);
                        element = doc.createElement("lastEdit");
                        element.appendChild(doc.createTextNode(formatter.format(e.getValue().getLastEdit())));
                        deckElement.appendChild(element);
                        insertRow(doc, deckElement, 2);
                        element = doc.createElement("heroCard");
                        element.appendChild(doc.createTextNode(""+e.getValue().getCards().getHeroCard().getID()));
                        deckElement.appendChild(element);
                        insertRow(doc, deckElement, 2);
                        element = doc.createElement("weaponCards");
                        deckElement.appendChild(element);
                        Element cardElement, cardAttributeElement;
                        for(Map.Entry<WeaponCard, Integer> entry : e.getValue().getCards().getWeaponCards().entrySet()){
                            insertRow(doc, element, 3);
                            cardElement = doc.createElement("weaponCard");
                            insertRow(doc, cardElement, 4);
                            element.appendChild(cardElement);
                            cardAttributeElement = doc.createElement("id");
                            cardAttributeElement.appendChild(doc.createTextNode(""+entry.getKey().getID()));
                            cardElement.appendChild(cardAttributeElement);
                            insertRow(doc, cardElement, 4);
                            cardAttributeElement = doc.createElement("quantity");
                            cardAttributeElement.appendChild(doc.createTextNode(""+entry.getValue()));
                            cardElement.appendChild(cardAttributeElement);
                            insertRow(doc, cardElement, 3);
                        }
                        insertRow(doc, element, 2);
                        insertRow(doc, deckElement, 2);
                        element = doc.createElement("equipmentCards");
                        deckElement.appendChild(element);
                        for(EquipmentCard ec: e.getValue().getCards().getEquipmentCards()){
                            insertRow(doc, element, 3);
                            cardAttributeElement = doc.createElement("equipmentCardID");
                            cardAttributeElement.appendChild(doc.createTextNode(""+ec.getID()));
                            element.appendChild(cardAttributeElement);
                        }
                        insertRow(doc, element, 2);
                        insertRow(doc, deckElement, 2);
                        element = doc.createElement("deckCards");
                        deckElement.appendChild(element);
                        for(Map.Entry<DeckCard, Integer> entry : e.getValue().getCards().getDeckCards().entrySet()){
                            insertRow(doc, element, 3);
                            cardElement = doc.createElement("card");
                            insertRow(doc, cardElement, 4);
                            element.appendChild(cardElement);
                            cardAttributeElement = doc.createElement("id");
                            cardAttributeElement.appendChild(doc.createTextNode(""+entry.getKey().getID()));
                            cardElement.appendChild(cardAttributeElement);
                            insertRow(doc, cardElement, 4);
                            cardAttributeElement = doc.createElement("quantity");
                            cardAttributeElement.appendChild(doc.createTextNode(""+entry.getValue()));
                            cardElement.appendChild(cardAttributeElement);
                            insertRow(doc, cardElement, 3);
                        }
                        insertRow(doc, element, 2);
                        insertRow(doc, deckElement, 2);
                        element = doc.createElement("public");
                        element.appendChild(doc.createTextNode(""+e.getValue().isVisible()));
                        deckElement.appendChild(element);
                        insertRow(doc, deckElement, 2);
                        element = doc.createElement("comments");
                        deckElement.appendChild(element);
                        Stack<Comment> comments = e.getValue().getComments();
                        if(!comments.empty()){
                            Element com, reply, replies, e1;
                            while(!comments.isEmpty()){
                                Comment c = comments.pop();
                                insertRow(doc, element, 3);
                                com = doc.createElement("comment");
                                element.appendChild(com);
                                insertRow(doc, com, 4);
                                e1 = doc.createElement("user");
                                e1.appendChild(doc.createTextNode(c.getUser()));
                                com.appendChild(e1);
                                insertRow(doc, com, 4);
                                e1 = doc.createElement("text");
                                e1.appendChild(doc.createTextNode(c.getComment()));
                                com.appendChild(e1);
                                insertRow(doc, com, 4);
                                e1 = doc.createElement("dateOfCreation");
                                e1.appendChild(doc.createTextNode(formatter.format(c.getDateOFCreation())));
                                com.appendChild(e1);
                                replies = doc.createElement("replies");
                                insertRow(doc, com, 4);
                                com.appendChild(replies);
                                ArrayDeque<Comment> rep = c.getAllReplies();
                                while(!rep.isEmpty()){
                                    Comment r = rep.removeFirst();
                                    insertRow(doc, replies, 5);
                                    reply = doc.createElement("reply");
                                    replies.appendChild(reply);
                                    insertRow(doc, reply, 6);
                                    e1 = doc.createElement("user");
                                    e1.appendChild(doc.createTextNode(r.getUser()));
                                    reply.appendChild(e1);
                                    insertRow(doc, reply, 6);
                                    e1 = doc.createElement("text");
                                    e1.appendChild(doc.createTextNode(r.getComment()));
                                    reply.appendChild(e1);
                                    insertRow(doc, reply, 6);
                                    e1 = doc.createElement("dateOfCreation");
                                    e1.appendChild(doc.createTextNode(formatter.format(r.getDateOFCreation())));
                                    reply.appendChild(e1);
                                    insertRow(doc, reply, 5);
                                }
                                insertRow(doc, replies, 4);
                                insertRow(doc, com, 3);
                            }
                            insertRow(doc, element, 2);
                        }
                        insertRow(doc, deckElement, 1);
                        saved = true;
                    }
                    else{
                        if(e.getKey() == Integer.parseInt(element.getAttribute("id"))){
                            NodeList deckElements = element.getChildNodes();
                            Deck d = e.getValue();
                            deckElements.item(1).getChildNodes().item(0).setTextContent(d.getOwner());
                            deckElements.item(3).getChildNodes().item(0).setTextContent(d.getTitle());
                            deckElements.item(5).getChildNodes().item(0).setTextContent(d.getDescription());
                            deckElements.item(7).getChildNodes().item(0).setTextContent(formatter.format(d.getLastEdit()));
                            deckElements.item(9).getChildNodes().item(0).setTextContent(""+d.getCards().getHeroCard().getID());
                            for(int i = 0; i < 10; i++)
                                element.removeChild(deckElements.item(11));
                            
                            Element ele = doc.createElement("weaponCards");
                            element.appendChild(ele);
                            Element cardElement, cardAttributeElement;
                            for(Map.Entry<WeaponCard, Integer> entry : d.getCards().getWeaponCards().entrySet()){
                                insertRow(doc, ele, 3);
                                cardElement = doc.createElement("weaponCard");
                                insertRow(doc, cardElement, 4);
                                ele.appendChild(cardElement);
                                cardAttributeElement = doc.createElement("id");
                                cardAttributeElement.appendChild(doc.createTextNode(""+entry.getKey().getID()));
                                cardElement.appendChild(cardAttributeElement);
                                insertRow(doc, cardElement, 4);
                                cardAttributeElement = doc.createElement("quantity");
                                cardAttributeElement.appendChild(doc.createTextNode(""+entry.getValue()));
                                cardElement.appendChild(cardAttributeElement);
                                insertRow(doc, cardElement, 3);
                            }
                            insertRow(doc, ele, 2);
                            insertRow(doc, element, 2);

                            ele = doc.createElement("equipmentCards");
                            element.appendChild(ele);
                            for(EquipmentCard ec: d.getCards().getEquipmentCards()){
                                insertRow(doc, ele, 3);
                                cardAttributeElement = doc.createElement("equipmentCardID");
                                cardAttributeElement.appendChild(doc.createTextNode(""+ec.getID()));
                                ele.appendChild(cardAttributeElement);
                            }
                            insertRow(doc, ele, 2);
                            insertRow(doc, element, 2);
                            ele = doc.createElement("deckCards");
                            element.appendChild(ele);
                            for(Map.Entry<DeckCard, Integer> entry : d.getCards().getDeckCards().entrySet()){
                                insertRow(doc, ele, 3);
                                cardElement = doc.createElement("card");
                                insertRow(doc, cardElement, 4);
                                ele.appendChild(cardElement);
                                cardAttributeElement = doc.createElement("id");
                                cardAttributeElement.appendChild(doc.createTextNode(""+entry.getKey().getID()));
                                cardElement.appendChild(cardAttributeElement);
                                insertRow(doc, cardElement, 4);
                                cardAttributeElement = doc.createElement("quantity");
                                cardAttributeElement.appendChild(doc.createTextNode(""+entry.getValue()));
                                cardElement.appendChild(cardAttributeElement);
                                insertRow(doc, cardElement, 3);
                            }
                            insertRow(doc, ele, 2);
                            insertRow(doc, element, 2);
                            ele = doc.createElement("public");
                            ele.appendChild(doc.createTextNode(""+e.getValue().isVisible()));
                            element.appendChild(ele);
                            insertRow(doc, element, 2);
                            ele = doc.createElement("comments");
                            element.appendChild(ele);
                            Stack<Comment> comments = d.getComments();
                            Element com, reply, replies, e1;
                            while(!comments.isEmpty()){
                                Comment c = comments.pop();
                                insertRow(doc, ele, 3);
                                com = doc.createElement("comment");
                                ele.appendChild(com);
                                insertRow(doc, com, 4);
                                e1 = doc.createElement("user");
                                e1.appendChild(doc.createTextNode(c.getUser()));
                                com.appendChild(e1);
                                insertRow(doc, com, 4);
                                e1 = doc.createElement("text");
                                e1.appendChild(doc.createTextNode(c.getComment()));
                                com.appendChild(e1);
                                insertRow(doc, com, 4);
                                e1 = doc.createElement("dateOfCreation");
                                e1.appendChild(doc.createTextNode(formatter.format(c.getDateOFCreation())));
                                com.appendChild(e1);
                                replies = doc.createElement("replies");
                                insertRow(doc, com, 4);
                                com.appendChild(replies);
                                ArrayDeque<Comment> rep = c.getAllReplies();
                                while(!rep.isEmpty()){
                                    Comment r = rep.removeFirst();
                                    insertRow(doc, replies, 5);
                                    reply = doc.createElement("reply");
                                    replies.appendChild(reply);
                                    insertRow(doc, reply, 6);
                                    e1 = doc.createElement("user");
                                    e1.appendChild(doc.createTextNode(r.getUser()));
                                    reply.appendChild(e1);
                                    insertRow(doc, reply, 6);
                                    e1 = doc.createElement("text");
                                    e1.appendChild(doc.createTextNode(r.getComment()));
                                    reply.appendChild(e1);
                                    insertRow(doc, reply, 6);
                                    e1 = doc.createElement("dateOfCreation");
                                    e1.appendChild(doc.createTextNode(formatter.format(r.getDateOFCreation())));
                                    reply.appendChild(e1);
                                    insertRow(doc, reply, 5);
                                }
                                insertRow(doc, replies, 4);
                                insertRow(doc, com, 3);
                            }
                            insertRow(doc, ele, 2);
                            insertRow(doc, element, 1);
                            saved = true;
                        }
                    }
                    index++;
                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(doc);
            StreamResult streamResult = new StreamResult(new File(DataPath.decksFile));
            transformer.transform(domSource, streamResult);

        }catch(ParserConfigurationException | TransformerException | SAXException | IOException e){
            server.appendStringToLog(e.getMessage());
        }
    }

    public Collection loadUserCollection(String username){
        Collection coll = new Collection(username, server);
        CardManager cards = server.getCards();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(new File(DataPath.collectionFile));

            // optional, but recommended
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            int quantity;
            Condition c;
            NodeList collectionsList = doc.getElementsByTagName("userCollection"), concreteCards, conditions;
            for (int temp = 0; temp < collectionsList.getLength(); temp++) {

                Node node = collectionsList.item(temp);

                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    Element element = (Element) node;

                    if(element.getAttribute("user").equals(username)){
                        concreteCards = element.getElementsByTagName("concrCard");
                        for(int tmp = 0; tmp < concreteCards.getLength(); tmp++){

                            element = (Element)concreteCards.item(tmp);
                            conditions = element.getChildNodes();
                            for(int i = 1; i < conditions.getLength(); i+=2){
                                quantity = Integer.parseInt(conditions.item(i).getTextContent());
                                String condition = conditions.item(i).getNodeName();
                                c = switch(condition){
                                    case "NM" -> Condition.NM;
                                    case "M" -> Condition.M;
                                    case "EX" -> Condition.EX;
                                    case "GO" -> Condition.GO;
                                    case "LP" -> Condition.LP;
                                    case "PL" -> Condition.PL;
                                    case "PO" -> Condition.PO;
                                    default -> throw new Exception();
                                };
                                coll.addCard(cards.getPhysCard(cards.getConcreteCard(Integer.parseInt(element.getAttribute("id"))), c), quantity);
                            }
                        }
                    }
                }
            }
        }catch(Exception e){
            server.appendStringToLog(e.getMessage());
        }
        return coll;
    }

    public void saveUserCollection(Collection coll){

        CardManager cards = server.getCards();
        // Instantiate the Factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(new File(DataPath.collectionFile));

            doc.normalize();

            Node root = doc.getDocumentElement();


            try{
                NodeList collectionsList = ((Element)root).getElementsByTagName("userCollection");

                for (int temp = 0; temp < collectionsList.getLength(); temp++) {

                    Node node = collectionsList.item(temp);

                    if (node.getNodeType() == Node.ELEMENT_NODE) {

                        Element element = (Element) node;

                        if(element.getAttribute("user").equals(coll.getUser())){
                            Node nextNode = element.getNextSibling();
                            root.removeChild(node);
                            Element ele = doc.createElement("userCollection");
                            ele.setAttribute("user", coll.getUser());
                            root.insertBefore(ele, nextNode);
                            
                            for(Entry<ConcreteCard, CollectionCardsList> e: coll.getCollection().entrySet()){
                                if(e.getValue().getTotalQuantity()>0){
                                    Element concrCard = doc.createElement("concrCard");
                                    concrCard.setAttribute("id", ""+e.getKey().getId());
                                    insertRow(doc, ele, 2);
                                    for(Condition c: Condition.values()){
                                        insertRow(doc, concrCard, 3);
                                        Element condition = doc.createElement(""+c);
                                        int quantity = e.getValue().getCardQuantity(cards.getPhysCard(e.getKey(), c));
                                        condition.appendChild(doc.createTextNode(""+quantity));
                                        concrCard.appendChild(condition);
                                    }
                                    insertRow(doc, concrCard, 2);
                                    ele.appendChild(concrCard);
                                }
                            }
                            if(ele.getChildNodes().getLength() > 0)
                                insertRow(doc, ele, 1);
                            break;
                        }
                    }
                }
            }
            catch(NumberFormatException e){
            }
                
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(doc);
            StreamResult streamResult = new StreamResult(new File(DataPath.collectionFile));
            transformer.transform(domSource, streamResult);

            
        }
        catch (ParserConfigurationException | SAXException | DOMException | IOException | TransformerException e) {
            server.appendStringToLog(e.getMessage());
        }
    }

    public TreeMap<Integer, Ad> loadAds(){
        TreeMap<Integer, Ad> adsList = new TreeMap<>();
        CardManager cards = server.getCards();
        UsersManager users = server.getUsers();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(new File(DataPath.adsFile));

            // optional, but recommended
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            int quantity, id, maxID=0;
            Condition c;
            Ad ad;
            String user;
            Date date;
            ConcreteCard cc;
            PhysicalCard pc;
            AdState state;
            NodeList aList = doc.getElementsByTagName("ad");
            for (int temp = 0; temp < aList.getLength(); temp++) {
                Node node = aList.item(temp);

                if (node.getNodeType() == Node.ELEMENT_NODE){
                    Element adElement = (Element) node;
                    id = Integer.parseInt(adElement.getAttribute("id"));
                    maxID = (id>maxID)?id:maxID;
                    if(users.exists(user = adElement.getElementsByTagName("user").item(0).getTextContent())){
                        date = formatter.parse(adElement.getElementsByTagName("dateOfCreation").item(0).getTextContent());
                        quantity = Integer.parseInt(adElement.getElementsByTagName("quantity").item(0).getTextContent());
                        state = switch(adElement.getElementsByTagName("state").item(0).getTextContent()){
                            case "active" -> AdState.active;
                            case "pending" -> AdState.pending;
                            case "deleted" -> AdState.deleted;
                            case "concluded" -> AdState.concluded;
                            default -> throw new Exception("Invalid ad state for ad with id: "+id);
                        };
                        cc = cards.getConcreteCard(Integer.parseInt(adElement.getElementsByTagName("cardID").item(0).getTextContent()));
                        if(adElement.getAttribute("type").equals("Offer")){
                            c = switch (adElement.getElementsByTagName("cond").item(0).getTextContent()) {
                                case "NM" -> Condition.NM;
                                case "M" -> Condition.M;
                                case "EX" -> Condition.EX;
                                case "GO" -> Condition.GO;
                                case "LP" -> Condition.LP;
                                case "PL" -> Condition.PL;
                                case "PO" -> Condition.PO;
                                default -> throw new Exception("Invalid condition in ad with id: "+id);
                            };
                            pc = cards.getPhysCard(cc, c);
                            ad = new Offer(id, user, quantity, date, state, pc);
                            adsList.put(id, ad);
                        }
                        else if(adElement.getAttribute("type").equals("Request")){
                            c = switch (adElement.getElementsByTagName("minCond").item(0).getTextContent()) {
                                case "NM" -> Condition.NM;
                                case "M" -> Condition.M;
                                case "EX" -> Condition.EX;
                                case "GO" -> Condition.GO;
                                case "LP" -> Condition.LP;
                                case "PL" -> Condition.PL;
                                case "PO" -> Condition.PO;
                                default -> throw new Exception("Invalid condition in ad with id: "+id);
                            };
                            ad = new Request(id, user, quantity, date, cc, c, state);
                            adsList.put(id, ad);
                        }
                        else{
                            throw new InvalidAdException("Invalid type in ad with id: "+id);
                        }
                    }
                    else{
                        throw new Exception("Invalid user in ad with id: "+id);
                    }
                }
            }
            server.getBoard().setNextId(maxID+1);
        }catch(Exception e){
            server.appendStringToLog(e.getMessage());
        }
        return adsList;
    }

    public void saveAds(TreeMap<Integer, Ad> adsList){
        // Instantiate the Factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.newDocument();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            doc.normalize();

            Element root = doc.createElement("ads");
            doc.appendChild(root);

            insertRow(doc, root, 0);
            Element adElement, element;
            Ad ad;
            try{
                for(Entry<Integer, Ad> e: adsList.entrySet()){
                    insertRow(doc, root, 1);
                    adElement = doc.createElement("ad");
                    root.appendChild(adElement);
                    adElement.setAttribute("id", ""+e.getKey());
                    ad = e.getValue();
                    insertRow(doc, adElement, 2);
                    element = doc.createElement("user");
                    element.appendChild(doc.createTextNode(ad.getOwner()));
                    adElement.appendChild(element);
                    insertRow(doc, adElement, 2);
                    element = doc.createElement("dateOfCreation");
                    element.appendChild(doc.createTextNode(formatter.format(ad.getDateOfCreation())));
                    adElement.appendChild(element);
                    if(ad instanceof Offer){
                        Offer o = (Offer)ad;
                        insertRow(doc, adElement, 2);
                        element = doc.createElement("cardID");
                        element.appendChild(doc.createTextNode(""+o.getPc().getConcrCard().getId()));
                        adElement.appendChild(element);
                        adElement.setAttribute("type", "Offer");
                        insertRow(doc, adElement, 2);
                        element = doc.createElement("cond");
                        element.appendChild(doc.createTextNode(o.getPc().getCondition().toString()));
                        adElement.appendChild(element);
                    }
                    else{
                        Request r = (Request)ad;
                        insertRow(doc, adElement, 2);
                        element = doc.createElement("cardID");
                        element.appendChild(doc.createTextNode(""+r.getCc().getId()));
                        adElement.appendChild(element);
                        adElement.setAttribute("type", "Request");
                        insertRow(doc, adElement, 2);
                        element = doc.createElement("minCond");
                        element.appendChild(doc.createTextNode(r.getMinimalCondition().toString()));
                        adElement.appendChild(element);
                    }
                    insertRow(doc, adElement, 2);
                    element = doc.createElement("quantity");
                    element.appendChild(doc.createTextNode(""+ad.getQuantity()));
                    adElement.appendChild(element);
                    insertRow(doc, adElement, 2);
                    element = doc.createElement("state");
                    element.appendChild(doc.createTextNode(""+ad.getState().toString()));
                    adElement.appendChild(element);
                    insertRow(doc, adElement, 1);
                }
                insertRow(doc, root, 0);
            }
            catch(NumberFormatException e){
                server.appendStringToLog(e.getMessage());
            }
                
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(doc);
            StreamResult streamResult = new StreamResult(new File(DataPath.adsFile));
            transformer.transform(domSource, streamResult);
        }
        catch (ParserConfigurationException | DOMException | TransformerException e) {
            server.appendStringToLog(e.getMessage());
        }
    }

    public void loadTransactions(){

        CardManager cards = server.getCards();
        UsersManager users = server.getUsers();
        TransactionsManager transactions = server.getTransactions();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(new File(DataPath.transactionsFile));

            // optional, but recommended
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            int quantity, id, maxID=0;
            TransactionBuilder builder = transactions.getTransactionBuilder();
            String user1, user2, tmp;
            ConcreteCard cc;
            Condition cond;
            PhysicalCard pc;
            NodeList tList = doc.getElementsByTagName("transaction"), cardList;
            Node tNode, cardNode;
            for (int temp = 0; temp < tList.getLength(); temp++) {
                tNode = tList.item(temp);

                if (tNode.getNodeType() == Node.ELEMENT_NODE){
                    Element tElement = (Element) tNode;
                    builder.buildNewTransaction();
                    builder.setID(id = Integer.parseInt(tElement.getAttribute("id")));
                    maxID = (id>maxID)?id:maxID;
                    user1 = tElement.getElementsByTagName("firstUser").item(0).getTextContent();
                    user2 = tElement.getElementsByTagName("secondUser").item(0).getTextContent();
                    if(!users.exists(user1))
                        throw new InvalidUsernameException("The username "+user1+" doesn't exist");
                    if(!users.exists(user2))
                        throw new InvalidUsernameException("The username "+user2+" doesn't exist");
                    builder.setFirstUser(user1);
                    builder.setSecondUser(user2);
                    TransactionState state= switch(tElement.getElementsByTagName("state").item(0).getTextContent()){

                        case "fromAdMatch" -> TransactionState.fromAdsMatch;
                        case "firstUserConfirmed" -> TransactionState.firstUserConfirmed;
                        case "active" -> TransactionState.active;
                        case "closing" -> TransactionState.closing;
                        case "concluded" -> TransactionState.concluded;
                        case "suspended" -> TransactionState.suspended;
                        default -> throw new InvalidTransactionStateException("The transaction with id "+id+" is corrupted: invalid state");
                    };
                    builder.setState(state);
                    tmp = tElement.getElementsByTagName("firstAd").item(0).getTextContent();
                    if(tmp.equals("null"))
                        builder.setFirstAd(null);
                    else
                        builder.setFirstAd(Integer.parseInt(tmp));
                    tmp = tElement.getElementsByTagName("secondAd").item(0).getTextContent();
                    if(tmp.equals("null"))
                        builder.setSecondAd(null);
                    else
                        builder.setSecondAd(Integer.parseInt(tmp));
                    builder.build();

                    cardList = tElement.getElementsByTagName("firstList").item(0).getChildNodes();
                    for(int i = 0; i < cardList.getLength(); i++){
                        cardNode = cardList.item(i);
                        if(cardNode.getNodeType() == Node.ELEMENT_NODE){
                            Element cardElement = (Element)cardNode;
                            quantity = Integer.parseInt(cardElement.getElementsByTagName("quantity").item(0).getTextContent());
                            cc = cards.getConcreteCard(Integer.parseInt(cardElement.getAttribute("id")));
                            cond = switch (cardElement.getElementsByTagName("cond").item(0).getTextContent()) {
                                case "NM" -> Condition.NM;
                                case "M" -> Condition.M;
                                case "EX" -> Condition.EX;
                                case "GO" -> Condition.GO;
                                case "LP" -> Condition.LP;
                                case "PL" -> Condition.PL;
                                case "PO" -> Condition.PO;
                                default -> throw new Exception("Invalid condition in transaction with id: "+id);
                            };
                            pc = cards.getPhysCard(cc, cond);
                            builder.addCardToTransaction(user1, pc, quantity);
                        }
                    }
                    cardList = tElement.getElementsByTagName("secondList").item(0).getChildNodes();
                    for(int i = 0; i < cardList.getLength(); i++){
                        cardNode = cardList.item(i);
                        if(cardNode.getNodeType() == Node.ELEMENT_NODE){
                            Element cardElement = (Element)cardNode;
                            quantity = Integer.parseInt(cardElement.getElementsByTagName("quantity").item(0).getTextContent());
                            cc = cards.getConcreteCard(Integer.parseInt(cardElement.getAttribute("id")));
                            cond = switch (cardElement.getElementsByTagName("cond").item(0).getTextContent()) {
                                case "NM" -> Condition.NM;
                                case "M" -> Condition.M;
                                case "EX" -> Condition.EX;
                                case "GO" -> Condition.GO;
                                case "LP" -> Condition.LP;
                                case "PL" -> Condition.PL;
                                case "PO" -> Condition.PO;
                                default -> throw new Exception("Invalid condition in transaction with id: "+id);
                            };
                            pc = cards.getPhysCard(cc, cond);
                            builder.addCardToTransaction(user2, pc, quantity);
                        }
                    }
                    builder.reset();
                }
            }
            transactions.setNextId(maxID+1);
        }catch(Exception e){
            server.appendStringToLog(e.getMessage());
        }
    }

    public void saveTransactions(TreeMap<Integer, Transaction> tList){
        // Instantiate the Factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.newDocument();
            doc.normalize();

            Element root = doc.createElement("transactions");
            doc.appendChild(root);

            insertRow(doc, root, 0);
            Element tElement, element, cardElement, ele;
            Transaction t;
            TreeMap<PhysicalCard, Integer> cardsList;
            try{
                for(Entry<Integer, Transaction> e: tList.entrySet()){
                    insertRow(doc, root, 1);
                    tElement = doc.createElement("transaction");
                    root.appendChild(tElement);
                    tElement.setAttribute("id", ""+e.getKey());
                    t = e.getValue();
                    insertRow(doc, tElement, 2);
                    element = doc.createElement("firstUser");
                    element.appendChild(doc.createTextNode(t.getFirstUser()));
                    tElement.appendChild(element);
                    insertRow(doc, tElement, 2);
                    element = doc.createElement("secondUser");
                    element.appendChild(doc.createTextNode(t.getSecondUser()));
                    tElement.appendChild(element);
                    insertRow(doc, tElement, 2);
                    element = doc.createElement("state");
                    element.appendChild(doc.createTextNode(""+t.getState()));
                    tElement.appendChild(element);
                    insertRow(doc, tElement, 2);
                    element = doc.createElement("firstAd");
                    if(t.getFirstAdID() != null)
                        element.appendChild(doc.createTextNode(""+t.getFirstAdID()));
                    else
                        element.appendChild(doc.createTextNode("null"));
                    tElement.appendChild(element);
                    insertRow(doc, tElement, 2);
                    element = doc.createElement("secondAd");
                    if(t.getSecondAdID() != null)
                        element.appendChild(doc.createTextNode(""+t.getSecondAdID()));
                    else
                        element.appendChild(doc.createTextNode("null"));
                    tElement.appendChild(element);

                    insertRow(doc, tElement, 2);
                    element = doc.createElement("firstList");
                    tElement.appendChild(element);
                    cardsList = t.getFirstList().getCardsList();
                    if(!cardsList.isEmpty()){
                        for(Entry<PhysicalCard, Integer> card: cardsList.entrySet()){
                            insertRow(doc, element, 3);
                            cardElement = doc.createElement("card");
                            element.appendChild(cardElement);
                            cardElement.setAttribute("id", ""+card.getKey().getConcrCard().getId());
                            insertRow(doc, cardElement, 4);
                            ele = doc.createElement("quantity");
                            ele.appendChild(doc.createTextNode(""+card.getValue()));
                            cardElement.appendChild(ele);
                            insertRow(doc, cardElement, 4);
                            ele = doc.createElement("cond");
                            ele.appendChild(doc.createTextNode(""+card.getKey().getCondition()));
                            cardElement.appendChild(ele);
                            insertRow(doc, cardElement, 3);
                        }
                        insertRow(doc, element, 2);
                    }
                    insertRow(doc, tElement, 2);
                    element = doc.createElement("secondList");
                    tElement.appendChild(element);
                    cardsList = t.getSecondList().getCardsList();
                    if(!cardsList.isEmpty()){
                        insertRow(doc, element, 3);
                        for(Entry<PhysicalCard, Integer> card: cardsList.entrySet()){
                            cardElement = doc.createElement("card");
                            element.appendChild(cardElement);
                            cardElement.setAttribute("id", ""+card.getKey().getConcrCard().getId());
                            insertRow(doc, cardElement, 4);
                            ele = doc.createElement("quantity");
                            ele.appendChild(doc.createTextNode(""+card.getValue()));
                            cardElement.appendChild(ele);
                            insertRow(doc, cardElement, 4);
                            ele = doc.createElement("cond");
                            ele.appendChild(doc.createTextNode(""+card.getKey().getCondition()));
                            cardElement.appendChild(ele);
                            insertRow(doc, cardElement, 3);
                        }
                        insertRow(doc, element, 2);
                    }
                    insertRow(doc, tElement, 1);
                }
                insertRow(doc, root, 0);
            }
            catch(NumberFormatException e){
                server.appendStringToLog(e.getMessage());
            }
                
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(doc);
            StreamResult streamResult = new StreamResult(new File(DataPath.transactionsFile));
            transformer.transform(domSource, streamResult);
        }
        catch (ParserConfigurationException | DOMException | TransformerException e) {
            server.appendStringToLog(e.getMessage());
        }
    }

    public void metodoProvaXML(){

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(new File(DataPath.decksFile));

            // optional, but recommended
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();

            NodeList decks = root.getElementsByTagName("deck");
            System.out.println(decks.item(1).getChildNodes().item(3).getTextContent());
        }
        catch(ParserConfigurationException | SAXException | IOException e){
            server.appendStringToLog(e.getMessage());
        }
    }
   
    public void insertCardInCollection(String user, PhysicalCard pc, int quantity){
        if(server.getUsers().exists(user)){

            // Instantiate the Factory
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            try {

                // optional, but recommended
                // process XML securely, avoid attacks like XML External Entities (XXE)
                dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

                // parse XML file
                DocumentBuilder db = dbf.newDocumentBuilder();

                Document doc = db.parse(new File(DataPath.collectionFile));

                doc.normalize();

                Node root = doc.getDocumentElement();

                try{
                    NodeList collectionsList = ((Element)root).getElementsByTagName("userCollection");

                    for (int temp = 0; temp < collectionsList.getLength(); temp++){

                        Node node = collectionsList.item(temp);

                        if (node.getNodeType() == Node.ELEMENT_NODE) {

                            Element element = (Element) node;

                            if(element.getAttribute("user").equals(user)){
                                NodeList concrList = element.getElementsByTagName("concrCard");
                                if(concrList.getLength() > 0){
                                    for(int i = 0; i < concrList.getLength(); i++){
                                        Element concrCard = (Element)(concrList.item(i));
                                        if(concrCard.getAttribute("id").equals(""+pc.getConcrCard().getId())){
                                            Node pcNode = concrCard.getElementsByTagName(pc.getCondition().toString()).item(0);
                                            int q = Integer.parseInt(pcNode.getTextContent()) + quantity;
                                            pcNode.setTextContent(""+q);
                                            break;
                                        }
                                        if(Integer.parseInt(concrCard.getAttribute("id")) > pc.getConcrCard().getId()){
                                            Element newConcrCard = doc.createElement("concrCard");
                                            newConcrCard.setAttribute("id", ""+pc.getConcrCard().getId());
                                            element.insertBefore(newConcrCard, concrCard);
                                            insertRow(doc, element, 2);
                                            for(Condition c: Condition.values()){
                                                insertRow(doc, newConcrCard, 3);
                                                Element condition = doc.createElement(""+c);
                                                int q=0;
                                                if(c.equals(pc.getCondition())){
                                                    q = quantity;
                                                }
                                                condition.appendChild(doc.createTextNode(""+q));
                                                newConcrCard.appendChild(condition);
                                            }
                                            insertRow(doc, newConcrCard, 2);
                                            insertRow(doc, element, 2);
                                            break;
                                        }
                                    }
                                }
                                else{
                                    Element newConcrCard = doc.createElement("concrCard");
                                    newConcrCard.setAttribute("id", ""+pc.getConcrCard().getId());
                                    element.appendChild(newConcrCard);
                                    insertRow(doc, element, 2);
                                    for(Condition c: Condition.values()){
                                        insertRow(doc, newConcrCard, 3);
                                        Element condition = doc.createElement(""+c);
                                        int q=0;
                                        if(c.equals(pc.getCondition())){
                                            q = quantity;
                                        }
                                        condition.appendChild(doc.createTextNode(""+q));
                                        newConcrCard.appendChild(condition);
                                    }
                                    insertRow(doc, newConcrCard, 2);
                                    insertRow(doc, element, 2);
                                }
                                break;
                            }
                        }
                    }
                }
                catch(NumberFormatException e){
                }
                    
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource domSource = new DOMSource(doc);
                StreamResult streamResult = new StreamResult(new File(DataPath.collectionFile));
                transformer.transform(domSource, streamResult);

                
            }
            catch (ParserConfigurationException | SAXException | DOMException | IOException | TransformerException e) {
                server.appendStringToLog(e.getMessage());
            }
        }
    }

    private void insertRow(Document doc, Element e, int t){
        StringBuffer s = new StringBuffer("\n");
        for(int i = 0; i < t; i++)
            s.append("\t");
        e.appendChild(doc.createTextNode(s.toString()));
    }

}


class StringConstants{
    static final String adsStart = "<cardID>";
    static final String adsEnd = "</cardID>";
    static final String collectionStart = "<concrCard id=\"";
    static final String collectionEnd = "\">";
}