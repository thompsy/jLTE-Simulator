package uk.co.downthewire.jLTE.simulator.sectors;

import com.google.common.collect.Collections2;
import org.apache.commons.configuration.Configuration;
import org.w3c.dom.Node;

import uk.co.downthewire.jLTE.simulator.ENodeB;
import uk.co.downthewire.jLTE.simulator.Location;
import uk.co.downthewire.jLTE.simulator.Predicates;
import uk.co.downthewire.jLTE.simulator.rbs.ResourceBlock;
import uk.co.downthewire.jLTE.simulator.ue.UE;
import uk.co.downthewire.jLTE.simulator.ue.UEComparators;
import uk.co.downthewire.jLTE.simulator.utils.FieldNames;

import java.util.*;

public class SFRSector extends AbstractSector {

    public static SFRSector fromXML(Configuration config, Node xml, ENodeB eNodeB, Location location) {
        SectorParams params = new SectorParams(xml);
        return new SFRSector(config, params.getSectorId(), eNodeB, location, params.getTxPower(), params.getAzimuth(), params.getHeight(), params.getDowntilt(), params.getAntennaGain());
    }

    public SFRSector(Configuration config, int sectorId, final ENodeB eNodeB, final Location loc, double txPower, double azimuth, double height, double downtilt, double antennaGain) {
        super(config, sectorId, eNodeB, loc, txPower, azimuth, height, downtilt, antennaGain);

        int startFullPowerRBs = id * 33;
        int endFullPowerRBs = startFullPowerRBs + config.getInt(FieldNames.SFR_NUM_HIGH_POWER_RBS);

        for (ResourceBlock RB : resourceBlocks.getResourceBlocks()) {
            if (RB.id >= startFullPowerRBs && RB.id < endFullPowerRBs) {
                RB.setFullPower();
            } else {
                RB.setLowPower();
            }
        }
    }

    @Override
    public void determineEdgeUEs() {
        super.determineEdgeUEs();

        Collections.sort(servedUEs, UEComparators.SINR_ORDER);
        for (int i = 0; i < calculateNumEdgeUEs(); i++) {
            servedUEs.get(i).setEdge(true);
        }
    }

    private int calculateNumEdgeUEs() {
        double proportionOfEdgeUes = config.getDouble(FieldNames.SFR_EDGE_USERS);
        int numberOfUEs = servedUEs.size();
        return (int) (numberOfUEs * proportionOfEdgeUes);
    }

    /**
     * Main scheduling algorithm. Here we schedule the UE which has been scheduled least first until we've run out of UEs or RBs.
     */
    @Override
    protected void doDownlinkAllocation(final int iteration, final int subframe) {
        int scheduledRBs = 0;
        boolean isDL = isDownlinkSubframe(subframe);
        // get all edge UEs
        List<UE> toSchedule = getUEsToSchedule(isDL);
        List<UE> edgeUEs = new ArrayList<>(Collections2.filter(toSchedule, Predicates.IS_EDGE_UE));

        // get all full power RBs
        List<ResourceBlock> unscheduledRBs = resourceBlocks.getUnscheduledRBs(iteration);
        Collection<ResourceBlock> fullPowerRBs = Collections2.filter(unscheduledRBs, Predicates.FULL_POWER_RBS);

        // schedule them based on priority
        for (ResourceBlock RB : fullPowerRBs) {
            if (edgeUEs.isEmpty()) {
                break;
            }
            // Sort by signal quality
            Collections.sort(edgeUEs, getPriorityComparator(RB));
            // Schedule the UE with the best signal
            final UE ue = edgeUEs.get(edgeUEs.size() - 1);

            allocateRBToUE(ue, RB, isDL);
            scheduledRBs += 1;
            edgeUEs = new ArrayList<>(Collections2.filter(toSchedule, Predicates.IS_EDGE_UE));
        }

        // get all remaining UEs
        toSchedule = getUEsToSchedule(isDL);
        // get all remaining RBs
        unscheduledRBs = resourceBlocks.getUnscheduledRBs(iteration);
        unscheduledRBs.removeAll(fullPowerRBs);

        // schedule them base on priority
        for (ResourceBlock RB : unscheduledRBs) {
            if (toSchedule.isEmpty()) {
                break;
            }
            // Sort by signal quality
            Collections.sort(toSchedule, getPriorityComparator(RB));
            // Schedule the UE with the best signal
            final UE ue = toSchedule.get(toSchedule.size() - 1);

            allocateRBToUE(ue, RB, isDL);
            scheduledRBs += 1;
            toSchedule = getUEsToSchedule(isDL);
        }

        updateScheduledRBCounters(scheduledRBs);
    }

    @SuppressWarnings("static-method")
    protected double calculatePriority(UE ue, ResourceBlock rb) {
        double priority = ue.getRelativeSignalOnRB(rb.id);
        if (!ue.isEdge() && rb.isFullPowerRB())
            priority = 0;
        if (ue.isEdge() && !rb.isFullPowerRB())
            priority = 0;

        return priority;
    }

    protected Comparator<UE> getPriorityComparator(final ResourceBlock RB) {
        return new Comparator<UE>() {
            @Override
            public int compare(final UE u1, final UE u2) {
                double priority1 = calculatePriority(u1, RB);
                double priority2 = calculatePriority(u2, RB);
                return Double.valueOf(priority1).compareTo(priority2);
            }
        };
    }
}
