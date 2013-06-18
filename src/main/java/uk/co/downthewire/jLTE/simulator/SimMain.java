package uk.co.downthewire.jLTE.simulator;

import flanagan.math.PsRandom;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.downthewire.jLTE.simulator.results.PerformanceStats;
import uk.co.downthewire.jLTE.simulator.sectors.AbstractSector;
import uk.co.downthewire.jLTE.simulator.sectors.DistributedSFRSector;
import uk.co.downthewire.jLTE.simulator.ue.UE;
import uk.co.downthewire.jLTE.simulator.ue.UEComparators;
import uk.co.downthewire.jLTE.simulator.utils.FieldNames;
import uk.co.downthewire.jLTE.simulator.x2.X2Request;
import uk.co.downthewire.jLTE.simulator.x2.X2Router;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static uk.co.downthewire.jLTE.simulator.utils.FieldNames.*;

public class SimMain {

    private static final Logger LOG = LoggerFactory.getLogger("Sim_" + Thread.currentThread().getId());

    private final List<UE> ues;
    private final List<AbstractSector> sectors;

    private final Configuration config;
    private final FadingData fadingData;
    private final X2Router x2Router;

    public SimMain(Configuration config, List<UE> ues, List<AbstractSector> sectors, FadingData fadingData, X2Router x2Router) {
        this.config = config;
        this.sectors = sectors;
        this.fadingData = fadingData;
        this.ues = removeUEsWithNoService(ues);
        this.x2Router = x2Router;
    }

    private static List<UE> removeUEsWithNoService(List<UE> ues) {
        List<UE> noService = new ArrayList<>();
        for (UE ue : ues) {
            if (ue.sectorTuples.isEmpty()) {
                noService.add(ue);
            }
        }
        ues.removeAll(noService);
        return ues;
    }

    @SuppressWarnings("boxing")
    public void run() throws IOException {

        PsRandom generalRandom = (PsRandom) config.getProperty(FieldNames.RANDOM_GENERAL);
        List<DistributedSFRSector> currentlyMutatingSectors = null;

        // For each time increment (1ms) work out who to schedule.
        for (int iteration = 0; iteration < config.getInt(ITERATIONS); iteration++) {

            LOG.debug("---- t = {} ms ----", iteration);

            if (config.getString(ALGORITHM).equals(DISTRIBUTED_SFR)) {
                currentlyMutatingSectors = doDistributedAlgorithm(config, iteration, sectors, currentlyMutatingSectors);
            }

            fadingData.readFading(iteration);

            for (AbstractSector sector : sectors) {
                sector.resetScheduledStatus();
            }

            for (final UE ue : ues) {
                ue.generateTraffic(generalRandom.nextDouble());
                ue.calculateSignalAcrossAllRBs();
            }

            for (final AbstractSector sector : sectors) {
                sector.determineEdgeUEs();
            }

            for (final AbstractSector s : sectors) {
                s.assignDownlinkRBs(iteration);
            }

            for (final UE ue : ues) {
                ue.accumulateDatarate();
            }

            for (final AbstractSector sector : sectors) {
                sector.accumulateDatarate();
            }

            if (config.getBoolean(FieldNames.X2_ENABLED)) {
                List<X2Request> x2Requests = new ArrayList<>();
                for (final AbstractSector sector : sectors) {
                    x2Requests.add(sector.generateX2Requests(iteration));
                }
                x2Router.routeRequests(iteration, x2Requests);
            }

            stats(iteration, ues, sectors, config);
            logUEs(iteration, ues);

        }
        if (config.getString(ALGORITHM).equals(DISTRIBUTED_SFR)) {
            LOG.error("--- Distributed Genes ---");
            ((DistributedSFRSector) sectors.get(0)).logHeader();
            for (AbstractSector sector : sectors) {
                ((DistributedSFRSector) sector).logGenes();
            }
        }
    }

    @SuppressWarnings("boxing")
    private static List<DistributedSFRSector> chooseAndMutateSector(List<AbstractSector> sectors, int iteration) {

        for (AbstractSector basicSector : sectors) {
            DistributedSFRSector sector = (DistributedSFRSector) basicSector;
            sector.resetNeighbourMutating();
        }

        List<DistributedSFRSector> mutatingSectors = new ArrayList<>();
        List<AbstractSector> shuffledSectors = new ArrayList<>(sectors);

        Collections.shuffle(shuffledSectors);

        for (AbstractSector basicSector : shuffledSectors) {
            DistributedSFRSector sector = (DistributedSFRSector) basicSector;
            if (sector.isNeighbourMutating()) {
                continue;
            }
            sector.mutate(iteration);
            mutatingSectors.add(sector);
            LOG.error("Mutating sector: {}", sector.id);
        }
        LOG.error("Mutating: {}", mutatingSectors);
        return mutatingSectors;
    }

    private static List<DistributedSFRSector> doDistributedAlgorithm(Configuration config, int iteration, List<AbstractSector> sectors, List<DistributedSFRSector> currentlyMutatingSectors) {

        if (iteration == 0 || iteration % config.getInt(FieldNames.DISTRIBUTED_SFR_WINDOW) != 0) {
            return currentlyMutatingSectors;
        }

        // start the first mutation
        if (currentlyMutatingSectors == null) {
            return chooseAndMutateSector(sectors, 0);
        }

        // evaluate the previous mutations
        for (AbstractSector basicSector : currentlyMutatingSectors) {
            DistributedSFRSector sector = (DistributedSFRSector) basicSector;
            int numberHappyNeighbours = 0;
            for (DistributedSFRSector neighbour : sector.getNeighbouringSectors()) {
                if (neighbour.hasTputIncreasedDuringMutationWindow()) {
                    numberHappyNeighbours += 1;
                }
            }
            sector.evaluateMutation(numberHappyNeighbours);
        }

        return chooseAndMutateSector(sectors, iteration);
    }

    /**
     * Log details from each UE for debugging.
     */
    @SuppressWarnings("boxing")
    private static void logUEs(final int iteration, List<UE> uesToLog) {

        final String line = "UEs|\tID\tsSector\tlocation\t\tshadowing\t\tSINR\tRBs\tdatarate\tedge\ttSch\tRBS Q'd\t\tdlRBs+\t\t\tdlPowerFactors";
        LOG.info("");
        LOG.info("--------------| UEs when t={} |-------------- ", iteration);
        LOG.info(line);

        Collections.sort(uesToLog, UEComparators.SECTOR_ORDER);
        for (final UE ue : uesToLog) {
            ue.logLine();
        }
    }

    /**
     * Some iteration statistics.
     */
    private static void stats(int iteration, List<UE> statsUEs, List<AbstractSector> statsSectors, Configuration config) {
        PerformanceStats stats = new PerformanceStats(statsUEs, statsSectors, config);
        stats.calculateStats();
        stats.logReducedStats(iteration);
    }

    /**
     * Calculate some final statistics.
     */
    public PerformanceStats finalStats(List<UE> statsUEs) {
        PerformanceStats stats = new PerformanceStats(statsUEs, sectors, config);
        stats.calculateStats();
        stats.logStats();

        return stats;
    }
}