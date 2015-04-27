package uk.co.downthewire.jLTE.simulator.sectors;

import flanagan.math.PsRandom;
import org.apache.commons.configuration.Configuration;
import org.w3c.dom.Node;

import uk.co.downthewire.jLTE.simulator.ENodeB;
import uk.co.downthewire.jLTE.simulator.Location;
import uk.co.downthewire.jLTE.simulator.rbs.ResourceBlock;
import uk.co.downthewire.jLTE.simulator.ue.UE;
import uk.co.downthewire.jLTE.simulator.utils.FieldNames;

import java.util.List;

public class RandomSector extends AbstractSector {

    public static RandomSector fromXML(Configuration config, Node xml, ENodeB eNodeB, Location location) {
        SectorParams params = new SectorParams(xml);
        return new RandomSector(config, params.getSectorId(), eNodeB, location, params.getTxPower(), params.getAzimuth(), params.getHeight(), params.getDowntilt(), params.getAntennaGain());
    }

    public RandomSector(Configuration config, int sectorId, final ENodeB eNodeB, final Location loc, double txPower, double azimuth, double height, double downtilt, double antennaGain) {
        super(config, sectorId, eNodeB, loc, txPower, azimuth, height, downtilt, antennaGain);
    }

    /**
     * Main scheduling algorithm. Here we schedule the UE which has been scheduled least first until we've run out of UEs or RBs.
     */
    @Override
    protected void doDownlinkAllocation(final int iteration, boolean isDL) {
        PsRandom generalRandom = (PsRandom) config.getProperty(FieldNames.RANDOM_GENERAL);
        List<UE> toSchedule = getUEsToSchedule();

        final List<ResourceBlock> unscheduledRBs = resourceBlocks.getUnscheduledRBs(iteration);

        int scheduledRBs = 0;
        for (ResourceBlock RB : unscheduledRBs) {
            if (toSchedule.isEmpty()) {
                break;
            }

            final int index = generalRandom.nextInteger(toSchedule.size() - 1);
            final UE ue = toSchedule.get(index);

            allocateRBToUE(ue, RB, isDL);
            scheduledRBs += 1;
            toSchedule = getUEsToSchedule();
        }

        updateScheduledRBCounters(scheduledRBs);
    }
}
