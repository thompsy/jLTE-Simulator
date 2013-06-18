package uk.co.downthewire.jLTE.stats;

import com.google.common.base.Predicate;
import fj.F;

public class Accumulator<X> {

    private final Predicate<X> predicate;
    private final ValueDistribution values = new ValueDistribution();
    private final F<X, Double> func;

    public Accumulator(Predicate<X> predicate, F<X, Double> func) {
        this.predicate = predicate;
        this.func = func;
    }

    @SuppressWarnings("boxing")
    public void accumulate(X x) {
        if (predicate.apply(x)) {
            values.add(func.f(x));
        }
    }

    public double getAverage() {
        return values.average();
    }

    public double getMax() {
        return values.max();
    }

    public double getMin() {
        return values.min();
    }

    public double get5thPercentileAverage() {
        return values.average5thPrecentile();
    }

    public double getTotal() {
        return values.total();
    }
}
