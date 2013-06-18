package uk.co.downthewire.jLTE.stats;

public class SimpleCounter {

    private int numSamples;
    private double count;
    private double lastSample;

    public void accumulate(double number) {
        numSamples += 1;
        count += number;
        lastSample = number;
    }

    public double getCount() {
        return count;
    }

    public double getAverage() {
        if (numSamples == 0)
            return 0.0;
        return count / numSamples;
    }

    public double getLastSample() {
        return lastSample;
    }

}
