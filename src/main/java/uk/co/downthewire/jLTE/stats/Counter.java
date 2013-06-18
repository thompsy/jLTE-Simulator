package uk.co.downthewire.jLTE.stats;

import com.google.common.base.Predicate;

public class Counter<X> {

    private final Predicate<X> predicate;
    private int numSamples;
    private int count;

    public Counter(Predicate<X> predicate) {
        this.predicate = predicate;
    }

    public void accumulate(X x) {
        numSamples += 1;
        if (predicate.apply(x)) {
            count += 1;
        }
    }

    public int getCount() {
        return count;
    }

}
