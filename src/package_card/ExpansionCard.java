package package_card;

public final class ExpansionCard {

    private final AbstractCard card;
    private final String expansion;
    private final int idNumber;
    private final Rarity rarity;

    public ExpansionCard(AbstractCard ac, String exp, int id, Rarity r) {
        this.card = ac;
        this.expansion = exp;
        this.idNumber = id;
        this.rarity = r;
    }

    public AbstractCard getCard() {
        return card;
    }

    public String getExpansion() {
        return expansion;
    }

    public int getIdNumber() {
        return idNumber;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public boolean equals(ExpansionCard e) {
        return e.idNumber == this.idNumber && e.expansion.equals(this.expansion) &&
                e.rarity.equals(this.rarity) && e.card.equals(this.card);
    }

}
