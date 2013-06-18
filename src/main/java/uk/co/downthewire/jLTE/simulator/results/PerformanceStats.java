package uk.co.downthewire.jLTE.simulator.results;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.downthewire.jLTE.simulator.Simulator;
import uk.co.downthewire.jLTE.simulator.sectors.AbstractSector;
import uk.co.downthewire.jLTE.simulator.ue.UE;
import uk.co.downthewire.jLTE.simulator.utils.FieldNames;
import uk.co.downthewire.jLTE.stats.Accumulator;
import uk.co.downthewire.jLTE.stats.Counter;

import java.text.DecimalFormat;
import java.util.List;

import static uk.co.downthewire.jLTE.simulator.Predicates.*;

public class PerformanceStats {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.######");

    private final List<UE> ues;
    private final List<AbstractSector> sectors;
    private final Configuration config;

    private Accumulator<UE> tputAll;
    private Accumulator<UE> tputEdge;
    private Accumulator<UE> tputCentral;
    private Accumulator<UE> sinrAll;
    private Accumulator<UE> sinrEdge;
    private Accumulator<UE> sinrCentral;
    private Counter<UE> uesNeverScheduled;
    private Counter<UE> uesNeverScheduldeButHadData;
    private Counter<UE> numEdgeUEs;
    private Counter<UE> numCentralUEs;
    private Accumulator<UE> numRBsServed;
    private Accumulator<UE> numRBsQueued;

    private Accumulator<AbstractSector> sectorTput;
    private Accumulator<AbstractSector> sectorServedUEs;
    private Accumulator<AbstractSector> sectorAvgLoad;
    private Accumulator<AbstractSector> sectorEdgeUEs;
    private Accumulator<AbstractSector> sectorRBsBlocked;
    private Accumulator<AbstractSector> sectorTotalRBsBlocked;

    private static final Logger LOG = LoggerFactory.getLogger(Simulator.class);

    public PerformanceStats(List<UE> ues, List<AbstractSector> sectors, Configuration config) {
        this.ues = ues;
        this.sectors = sectors;
        this.config = config;
    }

    public void calculateStats() {

        tputAll = new Accumulator<>(UE_ALWAYS_TRUE, GET_AVERAGE_UE_TPUT);
        tputEdge = new Accumulator<>(IS_EDGE_UE, GET_AVERAGE_UE_TPUT);
        tputCentral = new Accumulator<>(IS_CENTRAL_UE, GET_AVERAGE_UE_TPUT);

        sinrAll = new Accumulator<>(UE_ALWAYS_TRUE, GET_AVERAGE_UE_SINR);
        sinrEdge = new Accumulator<>(IS_EDGE_UE, GET_AVERAGE_UE_SINR);
        sinrCentral = new Accumulator<>(IS_CENTRAL_UE, GET_AVERAGE_UE_SINR);

        uesNeverScheduled = new Counter<>(HAS_NEVER_BEEN_SCHEDULED);
        uesNeverScheduldeButHadData = new Counter<>(HAS_NEVER_BEEN_SCHEDULED_AND_HAD_DATA);

        numEdgeUEs = new Counter<>(IS_EDGE_UE);
        numCentralUEs = new Counter<>(IS_CENTRAL_UE);

        numRBsServed = new Accumulator<>(UE_ALWAYS_TRUE, UE_NUM_RBS_SERVED);
        numRBsQueued = new Accumulator<>(UE_ALWAYS_TRUE, UE_NUM_RBS_QUEUED);

        for (final UE ue : ues) {
            tputAll.accumulate(ue);
            tputEdge.accumulate(ue);
            tputCentral.accumulate(ue);

            sinrAll.accumulate(ue);
            sinrEdge.accumulate(ue);
            sinrCentral.accumulate(ue);

            uesNeverScheduled.accumulate(ue);
            uesNeverScheduldeButHadData.accumulate(ue);

            numEdgeUEs.accumulate(ue);
            numCentralUEs.accumulate(ue);

            numRBsQueued.accumulate(ue);
            numRBsServed.accumulate(ue);
        }

        sectorTput = new Accumulator<>(SECTOR_ALWAYS_TRUE, SECTOR_TPUT);
        sectorServedUEs = new Accumulator<>(SECTOR_ALWAYS_TRUE, SECTOR_UES_SERVED);
        sectorAvgLoad = new Accumulator<>(SECTOR_ALWAYS_TRUE, SECTOR_AVERAGE_LOAD);
        sectorEdgeUEs = new Accumulator<>(SECTOR_ALWAYS_TRUE, SECTOR_EDGE_UES_SERVED);
        sectorRBsBlocked = new Accumulator<>(SECTOR_ALWAYS_TRUE, SECTOR_AVERAGE_RBS_BLOCKED);
        sectorTotalRBsBlocked = new Accumulator<>(SECTOR_ALWAYS_TRUE, SECTOR_TOTAL_RBS_BLOCKED);

        for (AbstractSector sector : sectors) {
            sectorTput.accumulate(sector);
            sectorServedUEs.accumulate(sector);
            sectorAvgLoad.accumulate(sector);
            sectorEdgeUEs.accumulate(sector);
            sectorRBsBlocked.accumulate(sector);
            sectorTotalRBsBlocked.accumulate(sector);
        }

    }

    @SuppressWarnings("boxing")
    public void logStats() {

        LOG.error("General user stats");
        LOG.error("\tNum Edge UEs: {}", numEdgeUEs.getCount());
        LOG.error("\tNum Center UEs: {}", numCentralUEs.getCount());

        LOG.error("\tTotal RBs served: {}", numRBsServed.getTotal());
        LOG.error("\tTotal RBs queued: {}", numRBsQueued.getTotal());

        LOG.error("Scheduling stats");
        LOG.error("\tAverage times UEs scheduled: {}", numRBsServed.getAverage());
        LOG.error("\tMax times scheduled: {}", numRBsServed.getMax());
        LOG.error("\tMin times scheduled: {}", numRBsServed.getMin());
        LOG.error("\tNum UEs never scheduled: {}", uesNeverScheduled.getCount());
        LOG.error("\tNum UEs never scheduled but had data: {}", uesNeverScheduldeButHadData.getCount());

        LOG.error("SINR stats for All UEs");
        LOG.error("\tAverage   (SINR-all) = {} ({} dB)", DECIMAL_FORMAT.format(sinrAll.getAverage()), DECIMAL_FORMAT.format(10 * Math.log10(sinrAll.getAverage())));
        LOG.error("\tMax       (SINR-all) = {} ({} dB)", DECIMAL_FORMAT.format(sinrAll.getMax()), DECIMAL_FORMAT.format(10 * Math.log10(sinrAll.getMax())));
        LOG.error("\tMin       (SINR-all) = {} ({} dB)", DECIMAL_FORMAT.format(sinrAll.getMin()), DECIMAL_FORMAT.format(10 * Math.log10(sinrAll.getMin())));

        LOG.error("SINR stats for Edge UEs:");
        LOG.error("\tAverage  (SINR-edge) = {} ({} dB)", DECIMAL_FORMAT.format(sinrEdge.getAverage()), DECIMAL_FORMAT.format(10 * Math.log10(sinrEdge.getAverage())));
        LOG.error("\tMax      (SINR-edge) = {} ({} dB)", DECIMAL_FORMAT.format(sinrEdge.getMax()), DECIMAL_FORMAT.format(10 * Math.log10(sinrEdge.getMax())));
        LOG.error("\tMin      (SINR-edge) = {} ({} dB)", DECIMAL_FORMAT.format(sinrEdge.getMin()), DECIMAL_FORMAT.format(10 * Math.log10(sinrEdge.getMin())));

        LOG.error("SINR stats for Center UEs:");
        LOG.error("\tAverage (SINR-center)= {} ({} dB)", DECIMAL_FORMAT.format(sinrCentral.getAverage()), DECIMAL_FORMAT.format(10 * Math.log10(sinrCentral.getAverage())));
        LOG.error("\tMax     (SINR-center)= {} ({} dB)", DECIMAL_FORMAT.format(sinrCentral.getMax()), DECIMAL_FORMAT.format(10 * Math.log10(sinrCentral.getMax())));
        LOG.error("\tMin     (SINR-center)= {} ({} dB)", DECIMAL_FORMAT.format(sinrCentral.getMin()), DECIMAL_FORMAT.format(10 * Math.log10(sinrCentral.getMin())));

        LOG.error("Tput stats for All UEs");
        LOG.error("\tAverage   (all) = {} Mbps", DECIMAL_FORMAT.format(tputAll.getAverage()));
        LOG.error("\tMax       (all) = {} Mbps", DECIMAL_FORMAT.format(tputAll.getMax()));
        LOG.error("\tMin       (all) = {} Mbps", DECIMAL_FORMAT.format(tputAll.getMin()));

        LOG.error("Tput stats for Edge UEs");
        LOG.error("\tAverage  (edge) = {} Mbps", DECIMAL_FORMAT.format(tputEdge.getAverage()));
        LOG.error("\tMax      (edge) = {} Mbps", DECIMAL_FORMAT.format(tputEdge.getMax()));
        LOG.error("\tMin      (edge) = {} Mbps", DECIMAL_FORMAT.format(tputEdge.getMin()));

        LOG.error("Tput stats for Center UEs");
        LOG.error("\tAverage (center)= {} Mbps", DECIMAL_FORMAT.format(tputCentral.getAverage()));
        LOG.error("\tMax     (center)= {} Mbps", DECIMAL_FORMAT.format(tputCentral.getMax()));
        LOG.error("\tMin     (center)= {} Mbps", DECIMAL_FORMAT.format(tputCentral.getMin()));

        LOG.error("5th percentile Tput = {} Mbps", DECIMAL_FORMAT.format(tputAll.get5thPercentileAverage()));

        LOG.error("Sector stats:");
        LOG.error("\tAvg Sector Tput = {} Mbps", DECIMAL_FORMAT.format(sectorTput.getAverage()));
        LOG.error("\tAvg UEs per sector = {}", sectorServedUEs.getAverage());
        LOG.error("\tAvg Edge UEs per sector = {}", sectorEdgeUEs.getAverage());
        LOG.error("\tAvg sector Load = {}", sectorAvgLoad.getAverage());
        if (config.getBoolean(FieldNames.X2_ENABLED)) {
            LOG.error("\tAvg RBs blocked per iteration with X2 = {}", sectorRBsBlocked.getAverage());
            LOG.error("\tTotal RBs blocked with X2 = {}", sectorTotalRBsBlocked.getAverage());
        }
        LOG.error("");
        LOG.error("");
    }

    @SuppressWarnings("boxing")
    public void logReducedStats(int iteration) {
        LOG.error("t = " + iteration);
        LOG.error("Stats: Average Sector Tput = {} Mbps", sectorTput.getAverage());
        LOG.error("Stats: Average UE Tput = {} Mbps", tputAll.getAverage());
        LOG.error("Stats: Worst UE Tput = {} Mbps", tputAll.getMin());
        LOG.error("Stats: Best UE Tput = {} Mbps", tputAll.getMax());
        LOG.error("Stats: 5th Percentile = {} Mbps", tputAll.get5thPercentileAverage());
    }

    public double getMaxUETput() {
        return tputAll.getMax();
    }

    public double getAverageUETput() {
        return tputAll.getAverage();
    }

    public double get5thPercentileTput() {
        return tputAll.get5thPercentileAverage();
    }

    public Configuration getConfiguration() {
        return config;
    }

}
