package uk.co.downthewire.jLTE.simulator.results;

import java.util.ArrayList;
import java.util.List;

public class SimulationResultsAggregator {

    private final List<SimulationResults> results;

    public SimulationResultsAggregator() {
        results = new ArrayList<>();
    }

    public void aggregate(List<SimulationResults> resultList) {
        results.addAll(resultList);
    }

    public void aggregate(SimulationResults result) {
        results.add(result);
    }

    @SuppressWarnings("boxing")
    public AggregatedSimulationResults getResult() {
        List<Double> averageTputs = new ArrayList<>();
        List<Double> percentileTputs = new ArrayList<>();
        List<Double> maxTputs = new ArrayList<>();

        for (SimulationResults result : results) {
            averageTputs.add(result.avergeTput);
            percentileTputs.add(result.percentileTput);
            maxTputs.add(result.maxUETput);
        }

        return new AggregatedSimulationResults(mean(percentileTputs), mean(averageTputs), mean(maxTputs), results.get(0).configuration, stddev(percentileTputs), stddev(averageTputs));
    }

    private static double mean(List<Double> inputs) {
        double total = 0;
        for (double number : inputs) {
            total += number;
        }
        return total / inputs.size();
    }

    private static double stddev(List<Double> inputs) {
        double mean = mean(inputs);

        double totalSquaredDifferences = 0;
        for (double number : inputs) {
            double difference = mean - number;
            totalSquaredDifferences += difference * difference;
        }

        return Math.sqrt(totalSquaredDifferences / inputs.size());
    }
}
