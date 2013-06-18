package uk.co.downthewire.jLTE.simulator;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.downthewire.jLTE.simulator.results.SimulationResults;
import uk.co.downthewire.jLTE.simulator.utils.FieldNames;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ParallelSimulationRunner {

    List<Configuration> configsToRun;
    private final int numTasks;
    Logger LOG = LoggerFactory.getLogger(Simulator.class);

    public void addConfigToRun(Configuration config) {
        configsToRun.add(config);
    }

    public void reset() {
        this.configsToRun = new ArrayList<>();
    }

    public ParallelSimulationRunner(int numTasks, Logger LOG) {
        this.numTasks = numTasks;
        this.LOG = LOG;
        this.configsToRun = new ArrayList<>();
    }

    @SuppressWarnings("boxing")
    public List<SimulationResults> run() {
        LOG.info("ParallelSimulationRunner: " + Thread.currentThread().getName());
        LOG.info("ParallelSimulationRunner: " + Thread.currentThread().getId());
        LOG.info("ParallelSimulationRunner: " + Thread.currentThread().getThreadGroup());

        // Setup concurrency stuff
        ExecutorService executorService = Executors.newFixedThreadPool(numTasks);
        List<Future<SimulationResults>> futureResults = new ArrayList<>();
        List<SimulationResults> results = new ArrayList<>();

        final long time = System.currentTimeMillis();

        // Run the tasks
        LOG.info("Running {} total tasks, with queue size {}", configsToRun.size(), numTasks);
        for (Configuration config : configsToRun) {
            LOG.info("Running simulator for chromosome:{}, seed = {}", config.getInt(FieldNames.CHROMOSOME_ID), config.getDouble(FieldNames.SEED));
            Future<SimulationResults> submit = executorService.submit(new Simulator(config));
            futureResults.add(submit);

        }

        Iterator<Future<SimulationResults>> iterator = futureResults.iterator();
        while (iterator.hasNext()) {
            try {
                Future<SimulationResults> result = iterator.next();
                int chromosomeId = result.get().configuration.getInt(FieldNames.CHROMOSOME_ID);
                LOG.info("Got results for chromosomeId = {}", chromosomeId);
                results.add(result.get());
                iterator.remove();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        LOG.info("Time taken: {} seconds", Double.toString((System.currentTimeMillis() - time) / 1000.0));
        executorService.shutdownNow();

        return results;
    }
}
