package package_deck;

import java.util.Calendar;
import java.util.Date;
import java.util.Stack;
import java.util.TreeMap;

import Utils.Exceptions.CommentNotFoundException;
import Utils.Exceptions.IllegalCardAdditionException;
import package_card.AbstractCard;
import package_card.HeroCard;
import package_interaction.Comment;
import package_interaction.Commentable;
import package_interaction.Rating;
import package_interaction.Reviewable;


public class Deck implements Commentable, Reviewable{
    private final int serialCode;
    private final String owner;
    private String title;
    private String description;
    private DeckList deckList;
    private Date lastEdit;
    private final Stack<Comment> comments;
    private final TreeMap<String, Rating> ratings;
    private boolean visible; //False = Private -- True = Public

    //CONSTRUCTORS
    
    public Deck(int id, String owner, String title, String description, HeroCard hero, boolean visibility){
        this.serialCode = id;
        this.owner = owner;
        this.title = title;
        this.description = description;
        this.deckList = new DeckList(hero);
        setLastEdit();
        this.comments = new Stack<>();
        this.visible = visibility; //PRIVATE
        this.ratings = new TreeMap<>();
    }

    public Deck(int id, String owner, String title , String description, HeroCard hero){
        this(id, owner, title, description, hero, false);
    }

    //Copy Constructor
    Deck(int id, Deck d, String copyOwner){
        this.serialCode = id;
        this.owner = copyOwner;
        this.title = d.title+" - copy";
        this.description = d.description;
        this.deckList = new DeckList(d.deckList);
        setLastEdit();
        this.visible = false;
        this.comments = new Stack<>();
        this.ratings = new TreeMap<>();
    }

    public Deck(int id, String owner, String title, String description, HeroCard hero, boolean visibility, Date lastEdit){
        this.serialCode = id;
        this.owner = owner;
        this.title = title;
        this.description = description;
        this.deckList = new DeckList(hero);
        this.lastEdit = lastEdit;
        this.comments = new Stack<>();
        this.visible = visibility; //PRIVATE
        this.ratings = new TreeMap<>();
    }

    //GETTER AND SETTERS
    public int getSerialCode() {
        return serialCode;
    }

    public String getOwner() {
        return owner;
    }

    public DeckList getCards() {
        return new DeckList(deckList);
    }

    public Date getLastEdit(){
        return lastEdit;
    }

    public void setLastEdit(){
        lastEdit = Calendar.getInstance().getTime();
    }

    public Stack<Comment> getComments() {
        return comments;
    }
    public boolean isVisible() {
        return visible;
    }
    public void changeVisibility(){
        this.visible = !this.visible;
    }

    public float getRating(){
        return calculateRating();
    }

    private float calculateRating(){
        final int n = ratings.size();
        float sum = 0;
        for(Rating r: ratings.values())
            sum+=r.getValue();
        return sum/n;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
        setLastEdit();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        setLastEdit();
    }

    //METHODS
    public void addCard(AbstractCard card) throws IllegalCardAdditionException{
        addCard(card,1);
    }

    public void addCard(AbstractCard card, int quantity) 
                throws IllegalCardAdditionException{
        if(card instanceof HeroCard)
            throw new IllegalCardAdditionException
            ("Trying to add to the deck an invalid card");
        deckList.addCard(card, quantity);        
        setLastEdit();
    }

    public boolean removeCard(AbstractCard card){
        return removeCard(card,1);
    }

    public boolean removeCard(AbstractCard card, int quantity){
        boolean removed = deckList.removeCard(card, quantity);
        if(removed)
            setLastEdit();
        return removed;
    }

    @Override
    public void addComment(final String user, final String comment){
        comments.push(new Comment(user, comment));
    }

    public void addComment(Comment c){
        comments.push(c);
    }

    @Override
    public void responseComment(String user, String answer, Comment comment) throws CommentNotFoundException {
        if(comments.contains(comment))
            comment.replyComment(new Comment(user, answer));
        else
            throw new CommentNotFoundException();
    }

    @Override
    public void addReview(final String user, int value){
        if(!user.equals(this.owner))
            ratings.put(user, new Rating(value));
    }

    @Override
    public String toString(){
        StringBuilder d = new StringBuilder("Title: " + getTitle() + " Description: " + getDescription());
        d.append("\n Cards: \n");

        /*for(AbstractCard a : getCards().keySet()){
            d.append(a.toString());
            d.append("Quantity");
            d.append(getCards().get(a).toString());
            d.append("\n");
        }
        */
        return d.toString();
    }

    public String exportDeck(){

        StringBuffer deckBuffer = new StringBuffer();

        deckBuffer.append("Deck build - via FaBManager :\n\n");
        deckBuffer.append(this.title+"\n\n");
        deckBuffer.append("Class: "+this.deckList.getHeroCard().getHeroClass()+"\n");
        deckBuffer.append("Hero: "+this.deckList.getHeroCard().getName()+"\n");
        deckBuffer.append("Weapons: "+this.deckList.getWeaponsList()+"\n");
        deckBuffer.append("Equipment: "+this.deckList.getEquipmentList()+"\n\n");
        deckBuffer.append(this.deckList.getDeckList());

        return deckBuffer.toString();
    }
}

