package uk.co.downthewire.jLTE.genes;

public class BooleanGene implements IGene<Boolean> {

    private boolean value;

    public BooleanGene(boolean value) {
        this.value = value;
    }

    @Override
    public void mutate() {
        this.value = !this.value;
    }

    @Override
    public IGene<Boolean> clone() {
        return new BooleanGene(value);
    }

    @Override
    public String toString() {
        return "" + value;
    }

    @SuppressWarnings("boxing")
    @Override
    public Boolean getValue() {
        return value;
    }
}
