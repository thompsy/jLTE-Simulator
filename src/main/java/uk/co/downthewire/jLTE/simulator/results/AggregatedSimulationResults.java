package uk.co.downthewire.jLTE.simulator.results;

import org.apache.commons.configuration.Configuration;

public class AggregatedSimulationResults extends SimulationResults {

    public final double percentileStdDev;
    public final double averageTputStdDev;

    public AggregatedSimulationResults(double percentile, double average, double max, Configuration config, double percentileStdDev, double averageTputStdDev) {
        super(percentile, average, max, config);
        this.percentileStdDev = percentileStdDev;
        this.averageTputStdDev = averageTputStdDev;
    }

    public static String header() {
        return "averageTput,averageTput-stddev,percentileTput,percentileTput-sdtdev";
    }

    @Override
    public String toString() {
        return avergeTput + "," + averageTputStdDev + "," + percentileTput + "," + percentileStdDev;
    }
}
