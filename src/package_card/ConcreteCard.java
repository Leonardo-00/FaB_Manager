package package_card;

public final class ConcreteCard implements Comparable<ConcreteCard>{

    private final int id;
    private final ExpansionCard card;
    private final String foilType;
    private final ArtVersion artVersion; 			//Rappresenta l'informazione riguardo un eventuale variant art della carta

    public int getId(){
        return id;
    }

    public ExpansionCard getCard(){
        return card;}

    public String getFoilType() {
        return foilType;}

    public ArtVersion getArtVersion(){
        return artVersion;}

    public ConcreteCard(int id, ExpansionCard ec, String fT, ArtVersion aV) {
        this.id = id;
        this.card = ec;
        this.foilType = fT;
        this.artVersion = aV;
    }

    public boolean equals(ConcreteCard cc){
        if(cc != null)
            return id == cc.id;
        else
            return false;
    }

    public int compareTo(ConcreteCard cc) {
        return Integer.valueOf(id).compareTo(cc.id);
    }

}
