package uk.co.downthewire.jLTE.simulator.sectors;

import org.apache.commons.configuration.Configuration;
import org.w3c.dom.Node;

import uk.co.downthewire.jLTE.simulator.ENodeB;
import uk.co.downthewire.jLTE.simulator.Location;
import uk.co.downthewire.jLTE.simulator.rbs.ResourceBlock;
import uk.co.downthewire.jLTE.simulator.ue.UE;
import uk.co.downthewire.jLTE.simulator.ue.UEComparators;

import java.util.Collections;
import java.util.List;

public class ProportionateFairSector extends AbstractSector {

    public static ProportionateFairSector fromXML(Configuration config, Node xml, ENodeB eNodeB, Location location) {
        SectorParams params = new SectorParams(xml);
        return new ProportionateFairSector(config, params.getSectorId(), eNodeB, location, params.getTxPower(), params.getAzimuth(), params.getHeight(), params.getDowntilt(), params.getAntennaGain());
    }

    private ProportionateFairSector(Configuration config, int sectorId, final ENodeB eNodeB, final Location loc, double txPower, double azimuth, double height, double downtilt, double antennaGain) {
        super(config, sectorId, eNodeB, loc, txPower, azimuth, height, downtilt, antennaGain);
    }

    /**
     * Main scheduling algorithm. Here we schedule the UE which has been scheduled least first until we've run out of UEs or RBs.
     */
    @Override
    protected void doDownlinkAllocation(final int iteration, final int subframe) {
        boolean isDL = isDownlinkSubframe(subframe);
        List<UE> toSchedule = getUEsToSchedule(isDL);

        final List<ResourceBlock> unscheduledRBs = resourceBlocks.getUnscheduledRBs(iteration);

        int scheduledRBs = 0;
        for (ResourceBlock RB : unscheduledRBs) {
            if (toSchedule.isEmpty()) {
                break;
            }

            // Sort by signal quality
            Collections.sort(toSchedule, UEComparators.getRelativeSignalComparator(RB.id));
            // Schedule the UE with the best signal
            final UE ue = toSchedule.get(toSchedule.size() - 1);

            allocateRBToUE(ue, RB, isDL);
            scheduledRBs += 1;
            toSchedule = getUEsToSchedule(isDL);
        }

        updateScheduledRBCounters(scheduledRBs);
    }
}
