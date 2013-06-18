package uk.co.downthewire.jLTE.simulator.results;

import org.apache.commons.configuration.Configuration;

public class SimulationResults {

    public final double percentileTput;
    public final double avergeTput;
    public final double maxUETput;
    public final Configuration configuration;

    public static SimulationResults forTesting(double percentile, double average, double max) {
        return new SimulationResults(percentile, average, max, null);
    }

    public SimulationResults(double percentile, double average, double max, Configuration config) {
        this.percentileTput = percentile;
        this.avergeTput = average;
        this.maxUETput = max;
        this.configuration = config;
    }

    public SimulationResults(PerformanceStats stats) {
        this.percentileTput = stats.get5thPercentileTput();
        this.avergeTput = stats.getAverageUETput();
        this.maxUETput = stats.getMaxUETput();
        this.configuration = stats.getConfiguration();
    }

    @Override
    public String toString() {
        return avergeTput + "," + percentileTput;
    }
}
