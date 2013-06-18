package uk.co.downthewire.jLTE.simulator.sectors;

import com.google.common.collect.Collections2;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.downthewire.jLTE.simulator.ENodeB;
import uk.co.downthewire.jLTE.simulator.Location;
import uk.co.downthewire.jLTE.simulator.rbs.ResourceBlock;
import uk.co.downthewire.jLTE.simulator.rbs.ResourceBlocks;
import uk.co.downthewire.jLTE.simulator.ue.UE;
import uk.co.downthewire.jLTE.simulator.ue.UEComparators;
import uk.co.downthewire.jLTE.simulator.utils.FieldNames;
import uk.co.downthewire.jLTE.simulator.x2.X2Request;
import uk.co.downthewire.jLTE.stats.Accumulator;
import uk.co.downthewire.jLTE.stats.SimpleCounter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static uk.co.downthewire.jLTE.simulator.Predicates.GET_AVERAGE_UE_TPUT;
import static uk.co.downthewire.jLTE.simulator.Predicates.UE_ALWAYS_TRUE;

public abstract class AbstractSector {

    static final Logger LOG = LoggerFactory.getLogger("Sim_" + Thread.currentThread().getId());

    public final int id;
    public final Location location;
    protected final ENodeB eNodeB;
    public final int servingENodeBId;
    final Configuration config;

    // Antenna parameters
    public final double azimuth;
    public final double height;
    public final double txPower;
    public final double downtilt;
    public final double antennaGain;

    private final SimpleCounter numRBsScheduledCounter;
    private final SimpleCounter numRBsNotScheduledCounter;
    private final SimpleCounter datarateCounter;
    private final SimpleCounter loadCounter;

    public final List<UE> servedUEs;

    protected final ResourceBlocks resourceBlocks;

    public AbstractSector(Configuration config, int sectorId, ENodeB eNodeB, final Location loc, double txPower, double azimuth, double height, double downtilt, double antennaGain) {

        this.config = config;
        this.id = sectorId;
        this.eNodeB = eNodeB;
        this.servingENodeBId = eNodeB.id;
        this.location = loc;

        this.txPower = txPower;
        this.azimuth = azimuth;
        this.height = height;
        this.downtilt = downtilt;
        this.antennaGain = antennaGain;

        this.servedUEs = new ArrayList<>();

        this.loadCounter = new SimpleCounter();
        this.datarateCounter = new SimpleCounter();
        this.numRBsScheduledCounter = new SimpleCounter();
        this.numRBsNotScheduledCounter = new SimpleCounter();

        this.resourceBlocks = new ResourceBlocks(config);
    }

    public void resetScheduledStatus() {
        resourceBlocks.resetScheduledStatus();
        for (UE ue : servedUEs) {
            ue.resetScheduledStatus();
        }
    }

    public void determineEdgeUEs() {
        for (final UE ue : servedUEs) {
            ue.setEdge(false);
        }
    }

    /**
     * Assign an RB each UE as far as possible.
     */
    public void assignDownlinkRBs(final int iteration) {
        List<UE> ues = servedUEs;
        if (ues.isEmpty()) {
            updateScheduledRBCounters(0);
            return;
        }
        doDownlinkAllocation(iteration);
    }

    protected List<UE> getUEsToSchedule() {
        return new ArrayList<>(Collections2.filter(servedUEs, UEComparators.hasRBsQueued()));
    }

    public boolean isRBScheduled(ResourceBlock rb) {
        return resourceBlocks.isScheduled(rb);
    }

    public double getScheduledPowerFactor(ResourceBlock RB) {
        if (RB.isFullPowerRB())
            return 1.0;
        return getReducedPowerFactor();
    }

    protected double getReducedPowerFactor() {
        return config.getDouble(FieldNames.ADAPTIVE_REDUCED_POWER_FACTOR);
    }

    public List<ResourceBlock> getResourceBlocks() {
        return resourceBlocks.getResourceBlocks();
    }

    protected abstract void doDownlinkAllocation(final int iteration);

    public void updateScheduledRBCounters(final int numRBsScheduled) {
        numRBsScheduledCounter.accumulate(numRBsScheduled);
        numRBsNotScheduledCounter.accumulate(config.getInt(FieldNames.RBS_PER_SECTOR) - numRBsScheduled);
    }

    protected static void allocateRBToUE(UE ue, ResourceBlock RB) {
        ue.schedule(RB);
        RB.schedule();
    }

    public void accumulateDatarate() {
        double datarate = 0.0;
        for (UE ue : servedUEs) {
            datarate += ue.getLastDatarate();
        }
        datarateCounter.accumulate(datarate);
    }

    public double getAvgDownlinkTput() {
        return datarateCounter.getAverage();
    }

    public double getPercentileTput() {

        Accumulator<UE> tputAll = new Accumulator<>(UE_ALWAYS_TRUE, GET_AVERAGE_UE_TPUT);
        for (UE ue : servedUEs) {
            tputAll.accumulate(ue);
        }
        return tputAll.get5thPercentileAverage();
    }

    public double calcAvgLoad() {
        return loadCounter.getAverage();
    }

    public double getNumEdgeUEs() {
        int numEdgeUEs = 0;
        for (UE ue : servedUEs) {
            if (ue.isEdge())
                numEdgeUEs += 1;
        }
        return numEdgeUEs;
    }

    @SuppressWarnings("boxing")
    @Override
    public String toString() {
        return String.format("Sector[%d:%d]: azimuth=%f, servedUEs=%s", servingENodeBId, id, azimuth, servedUEs);
    }

    public X2Request generateX2Requests(int iteration) {
        // get worst performing N RBs
        int numRBs = config.getInt(FieldNames.X2_MAX_RBS_PER_MSG);
        List<ResourceBlock> RBs = resourceBlocks.getResourceBlocks();
        Collections.sort(RBs, ResourceBlock.RB_SINR_COMPARATOR);

        // worst RB is first
        List<ResourceBlock> worstRBs = new ArrayList<>();
        for (int index = 0; index < numRBs; index++) {
            worstRBs.add(RBs.get(index));
        }

        return new X2Request(servingENodeBId, loadCounter.getLastSample(), worstRBs, iteration);
    }

    public void reserveRBs(X2Request request) {
        resourceBlocks.reserveRBs(request);
    }

    public double getAverageRBsBlocked() {
        return resourceBlocks.getAverageRBsBlocked();
    }

    public double getTotalRBsBlocked() {
        return resourceBlocks.getTotalRBsBlocked();
    }
}
