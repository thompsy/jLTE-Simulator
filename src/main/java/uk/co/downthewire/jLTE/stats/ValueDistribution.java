package uk.co.downthewire.jLTE.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ValueDistribution {

    private final List<Double> values;
    private double runningTotal;
    private int sampleCount;

    public ValueDistribution() {
        values = new ArrayList<>();
        runningTotal = 0.0;
        sampleCount = 0;
    }

    @SuppressWarnings("boxing")
    public void add(double sample) {
        sampleCount += 1;
        runningTotal += sample;
        values.add(sample);
    }

    public double average() {
        if (runningTotal == 0.0)
            return 0.0;

        return runningTotal / sampleCount;
    }

    @SuppressWarnings("boxing")
    public double min() {
        if (values.isEmpty())
            return 0.0;

        Collections.sort(values);
        return values.get(0);
    }

    @SuppressWarnings("boxing")
    public double max() {
        if (values.isEmpty())
            return 0.0;

        Collections.sort(values);
        return values.get(values.size() - 1);
    }

    @SuppressWarnings("boxing")
    public double average5thPrecentile() {
        if (values.isEmpty())
            return 0.0;

        Collections.sort(values);

        double percentileIndex = (sampleCount / 100.0) * 5.0;

        if (percentileIndex < 1.0)
            percentileIndex = 1.0;

        List<Double> percentileValues = values.subList(0, (int) percentileIndex);
        double percentileTotal = 0.0;
        for (Double d : percentileValues) {
            percentileTotal += d;
        }

        return percentileTotal / percentileValues.size();
    }

    public double total() {
        return runningTotal;
    }
}
