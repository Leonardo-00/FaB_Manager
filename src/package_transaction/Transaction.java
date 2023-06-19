package package_transaction;

import package_card.CardsList;
import package_card.PhysicalCard;

import java.util.TreeMap;

import Utils.Exceptions.InvalidQuantityRemovalException;
import Utils.constants.TransactionState;

/**
 * Stati di una trattativa
 * 
 * 1:   Trattativa creata a partire da un annuncio accettato, l'utente 1 è quello che ha accettato l'annuncio,
 * l'utente 2 è il proprietario dell'annuncio e deve accettare la trattativa per portarla in uno stato attivo
 * 
 * 2:   Trattativa creata a partire da un match tra 2 annunci, entrambi gli utenti devono accettare la trattativa
 * 
 * 3:   Trattativa creata a partire da un match tra 2 annunci, uno dei due utenti ha accettato e diventa il primo
 * (se non è il primo si effettua uno scambio tra gli utenti)
 * 
 * OPPURE
 * 
 * Trattativa creata manualmente da un utente, che sarà l'utente 1, mentre l'utente 2 sarà quello con cui il primo
 * ha richiesto di effettuare una trattativa, e questo dovrà accettare la trattativa
 * 
 * 4:   Stato attivo della trattativa, entrambi gli utenti possono aggiungere e/o rimuovere carte alla propria lista
 * 
 * 5:   Uno dei due utenti ha dichiarato di voler concludere la trattativa, che verrà quindi chiusa senza che lo scambio di carte
 * venga effettuato, e la trattativa verrà eliminata dal sistema dopo un lasso di tempo
 * 
 * 6:   Uno dei due utenti ha dichiarato di voler portare a termine la trattativa positivamente, quest'ultimo diventerà l'utente 1
 * se non lo era già, e l'utente 2 può accettare la proposta e portare la trattativa nello stato finale oppure rifiutare per riportare
 * la trattativa nello stato attivo
 * 
 * 7:   La trattativa è stata conclusa con successo, viene effettuato lo scambio delle carte tra le 2 collezioni degli utenti e vengono, eventualmente,
 * diminuite le quantità delle carte degli annunci interessati nella trattativa, qualora esistano; La trattativa rimane in questo stato (per un tempo indefinito)
 * ed è possibile per i due utenti lasciare un feedback per l'altro
 */

public class Transaction{

    private final TreeMap<String, CardsList> transactionLists;
    private String firstUser;
    private String secondUser;
    private Integer firstAdID;
    private Integer secondAdID;
    private TransactionState state;


    public Transaction(String u1, String u2, Integer id1, Integer id2, TransactionState s) {
        transactionLists = new TreeMap<>();
        transactionLists.put(u1, new CardsList());
        firstUser = u1;
        transactionLists.put(u2, new CardsList());
        secondUser = u2;
        firstAdID = id1;
        secondAdID = id2;
        state = s;
    }

    Transaction(){
        transactionLists = new TreeMap<>();
    }

    boolean addCard(String user, PhysicalCard pc, int q) {
        transactionLists.get(user).addCard(pc, q);
        return true;
    }

    boolean addCard(String user, PhysicalCard pc) {
        return addCard(user, pc, 1);
    }

    void removeCard(String user, PhysicalCard pc, int q) throws InvalidQuantityRemovalException{
        transactionLists.get(user).removeCard(pc, q);
    }

    void removeCard(String user, PhysicalCard pc) throws InvalidQuantityRemovalException{
        removeCard(user, pc, 1);
    }

    void swapUsers() {
        String tmp = firstUser;
        firstUser = secondUser;
        secondUser = tmp;

        Integer a = firstAdID;
        firstAdID = secondAdID;
        secondAdID = a;
    }

    public TransactionState getState() {
        return state;
    }

    void setState(TransactionState s) {
        state = s;
    }

    public Integer getFirstAdID() {
        return firstAdID;
    }

    public Integer getSecondAdID() {
        return secondAdID;
    }

    public String getFirstUser() {
        return firstUser;
    }

    public String getSecondUser() {
        return secondUser;
    }

    public CardsList getFirstList() {
        return transactionLists.get(firstUser);
    }

    public CardsList getSecondList() {
        return transactionLists.get(secondUser);
    }

    public int getQuantity(String user, PhysicalCard pc) {
        return transactionLists.get(user).getCardQuantity(pc);
    }

    boolean isFirstUser(String s) {
        return s.equals(firstUser);
    }

    boolean isSecondUser(String s) {
        return s.equals(secondUser);
    }

    public boolean containsUser(String s) {
        return isFirstUser(s) || isSecondUser(s);
    }

    public boolean isActive() {
        return state == TransactionState.active;
    }

    public TreeMap<String, CardsList> getList() {
        return new TreeMap<>(transactionLists);
    }

}


