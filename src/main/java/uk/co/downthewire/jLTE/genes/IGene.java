package uk.co.downthewire.jLTE.genes;

public interface IGene<X> extends Cloneable {

    void mutate();

    X getValue();

    IGene<X> clone();
}
