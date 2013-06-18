package uk.co.downthewire.jLTE.simulator.rbs;

import uk.co.downthewire.jLTE.stats.SimpleCounter;

import java.util.Comparator;

public class ResourceBlock {

    public final int id;
    private boolean isFullPower;
    private boolean isScheduled;
    private final SimpleCounter datarate;
    private final SimpleCounter sinr;

    public ResourceBlock(int id) {
        this.id = id;
        this.isFullPower = true;
        this.isScheduled = false;
        this.datarate = new SimpleCounter();
        this.sinr = new SimpleCounter();
    }

    public void resetScheduledStatus() {
        isScheduled = false;
    }

    public boolean isFullPowerRB() {
        return isFullPower;
    }

    public void setFullPower() {
        isFullPower = true;
    }

    public void setLowPower() {
        isFullPower = false;
    }

    public void schedule() {
        isScheduled = true;
    }

    public boolean isScheduled() {
        return isScheduled;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ResourceBlock other = (ResourceBlock) obj;
        return id == other.id;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }

    @SuppressWarnings("hiding")
    public void accumulateDataRate(double datarate) {
        this.datarate.accumulate(datarate);
    }

    @SuppressWarnings("hiding")
    public void accumulateSinr(double sinr) {
        this.sinr.accumulate(sinr);
    }

    public double getAverageSinr() {
        return sinr.getAverage();
    }

    public static final Comparator<ResourceBlock> RB_SINR_COMPARATOR = new Comparator<ResourceBlock>() {
        @Override
        public int compare(final ResourceBlock rb1, final ResourceBlock rb2) {
            double avgSinr1 = rb1.getAverageSinr();
            double avgSinr2 = rb2.getAverageSinr();

            return Double.valueOf(avgSinr1).compareTo(avgSinr2);
        }
    };

}
