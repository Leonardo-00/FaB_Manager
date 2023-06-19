package package_filter;

import package_card.ArtVersion;
import package_card.Rarity;
import package_card.ConcreteCard;
import package_collection_card.*;
import package_observer.Observer;

import java.util.TreeMap;

public class CollectionSectionController extends Observer{

    private final Collection collectionManager;
    //private TreeMap<ConcreteCard, CollectionCardsList> collection;
    private String searchFilter = "";
    private String cardType = "";
    private String expansion = "";
    private Rarity rarity;
    private String foilType = "";
    private ArtVersion variantArt;
    private FilterDecorator filterChain;

    public CollectionSectionController(Collection c){
        c.addObserver(this);
        collectionManager = c;
        resetFilters();
        filterChain = new DefaultFilter();
        update();
    }

    public void setSearchValue(String s){
        searchFilter = s;
    }

    public void setCardTypeValue(String s){
        cardType = s;
    }

    public void setExpansionValue(String s){
        expansion = s;
    }

    public void setRarityValue(Rarity r){
        rarity = r;
    }

    public void setFoilTypeValue(String s){
        foilType = s;
    }

    public void setVariantArtValue(ArtVersion a){
        variantArt = a;
    }

    public void resetFilters(){
        searchFilter = "";
        cardType = "";
        expansion = "";
        rarity = null;
        foilType = "";
        variantArt = null;
    }

    public TreeMap<ConcreteCard, CollectionCardsList> showCollection(){
        //Metodo per passare alla vista della schermata della collezione
        TreeMap<ConcreteCard, CollectionCardsList> viewCollection = collectionManager.getCollection();
        return filterChain.filter(viewCollection);
    }

    public void applyFilters(){
        filterChain = new ArtFilter(new FoilFilter(new RarityFilter(new ExpansionFilter(new TypeFilter(new SearchFilter(new DefaultFilter(), searchFilter), cardType), expansion), rarity), foilType), variantArt);
    }

    public void update(){
        //collection = filterChain.filter(collectionManager.getCollection());
    }

}
