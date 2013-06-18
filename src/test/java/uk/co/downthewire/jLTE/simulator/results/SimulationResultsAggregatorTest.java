package uk.co.downthewire.jLTE.simulator.results;

import org.junit.Test;

import uk.co.downthewire.jLTE.simulator.results.AggregatedSimulationResults;
import uk.co.downthewire.jLTE.simulator.results.SimulationResults;
import uk.co.downthewire.jLTE.simulator.results.SimulationResultsAggregator;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SimulationResultsAggregatorTest {

    private static List<SimulationResults> setupResults() {
        List<SimulationResults> results = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            results.add(SimulationResults.forTesting(i, i, i));
        }
        return results;
    }

    @SuppressWarnings("static-method")
    @Test
    public void test() {
        List<SimulationResults> results = setupResults();

        SimulationResultsAggregator aggregator = new SimulationResultsAggregator();
        for (SimulationResults result : results) {
            aggregator.aggregate(result);
        }

        AggregatedSimulationResults result = aggregator.getResult();

        assertEquals(4.5, result.avergeTput, 0.1);
        assertEquals(4.5, result.maxUETput, 0.1);
        assertEquals(4.5, result.percentileTput, 0.1);
        assertEquals(2.8, result.percentileStdDev, 0.1);
        assertEquals(2.8, result.averageTputStdDev, 0.1);
    }
}
