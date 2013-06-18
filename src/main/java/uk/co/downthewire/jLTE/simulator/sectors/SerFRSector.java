package uk.co.downthewire.jLTE.simulator.sectors;

import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.w3c.dom.Node;

import uk.co.downthewire.jLTE.simulator.ENodeB;
import uk.co.downthewire.jLTE.simulator.Location;
import uk.co.downthewire.jLTE.simulator.rbs.ResourceBlock;
import uk.co.downthewire.jLTE.simulator.ue.UE;
import uk.co.downthewire.jLTE.simulator.utils.FieldNames;

public class SerFRSector extends SFRSector {

	public static SerFRSector fromXML(Configuration config, Node xml, ENodeB eNodeB, Location location) {
		SectorParams params = new SectorParams(xml);
		return new SerFRSector(config, params.getSectorId(), eNodeB, location, params.getTxPower(), params.getAzimuth(), params.getHeight(), params.getDowntilt(), params.getAntennaGain());
	}

	private SerFRSector(Configuration config, int sectorId, final ENodeB eNodeB, final Location loc, double txPower, double azimuth, double height, double downtilt, double antennaGain) {
		super(config, sectorId, eNodeB, loc, txPower, azimuth, height, downtilt, antennaGain);
	}

	/**
	 * Main scheduling algorithm. Here we schedule the UE which has been scheduled least first until we've run out of UEs or RBs.
	 */
	@Override
	protected void doDownlinkAllocation(final int iteration) {
		int scheduledRBs = 0;

		List<UE> toSchedule = getUEsToSchedule();
		List<ResourceBlock> unscheduledRBs = resourceBlocks.getUnscheduledRBs(iteration);

		// schedule them based on priority
		for (ResourceBlock RB: unscheduledRBs) {
			if (toSchedule.isEmpty()) {
				break;
			}
			// Sort by signal quality
			Collections.sort(toSchedule, getPriorityComparator(RB));
			// Schedule the UE with the best signal
			final UE ue = toSchedule.get(toSchedule.size() - 1);

			allocateRBToUE(ue, RB);
			scheduledRBs += 1;
			toSchedule = getUEsToSchedule();
		}

		updateScheduledRBCounters(scheduledRBs);
	}

	@Override
	protected double calculatePriority(UE ue, ResourceBlock rb) {
		double basePriority = ue.getRelativeSignalOnRB(rb.id);
		return basePriority * getGamma(ue, rb, config);
	}

	private static double getGamma(UE ue, ResourceBlock rb, Configuration config) {
		if (!ue.isEdge() && !rb.isFullPowerRB())
			return config.getDouble(FieldNames.SERFR_GAMMA0);
		if (!ue.isEdge() && rb.isFullPowerRB())
			return config.getDouble(FieldNames.SERFR_GAMMA1);
		if (ue.isEdge() && !rb.isFullPowerRB())
			return config.getDouble(FieldNames.SERFR_GAMMA2);
		if (ue.isEdge() && rb.isFullPowerRB())
			return config.getDouble(FieldNames.SERFR_GAMMA3);

		throw new IllegalStateException("No suitable Gamma value was found.");
	}
}
