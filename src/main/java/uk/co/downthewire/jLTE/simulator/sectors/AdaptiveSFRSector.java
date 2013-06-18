package uk.co.downthewire.jLTE.simulator.sectors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.w3c.dom.Node;

import uk.co.downthewire.jLTE.simulator.ENodeB;
import uk.co.downthewire.jLTE.simulator.Location;
import uk.co.downthewire.jLTE.simulator.rbs.ResourceBlock;
import uk.co.downthewire.jLTE.simulator.ue.UE;
import uk.co.downthewire.jLTE.simulator.utils.FieldNames;
import flanagan.math.PsRandom;

public class AdaptiveSFRSector extends SFRSector {

	public static AdaptiveSFRSector fromXML(Configuration config, Node xml, ENodeB eNodeB, Location location) {
		SectorParams params = new SectorParams(xml);
		return new AdaptiveSFRSector(config, params.getSectorId(), eNodeB, location, params.getTxPower(), params.getAzimuth(), params.getHeight(), params.getDowntilt(), params.getAntennaGain());
	}

	public AdaptiveSFRSector(Configuration config, int sectorId, final ENodeB eNodeB, final Location loc, double txPower, double azimuth, double height, double downtilt, double antennaGain) {
		super(config, sectorId, eNodeB, loc, txPower, azimuth, height, downtilt, antennaGain);
		setupRBs();
	}

	protected void setupRBs() {
		if (isRandomHighPowerRBs()) {
			LOG.info("Assigning Full-Power RBs randomly");
			assignRandomFullPowerRBs();
		} else {
			LOG.info("Assigning Full-Power RBs normally");
			assignRegularFullPowerRBs();
		}
	}

	@SuppressWarnings("boxing")
	protected void assignRandomFullPowerRBs() {
		double proportionOfFullPowerRBs = getProportionOfFullPowerRBs();
		int numFullPowerRBs = (int) (resourceBlocks.size() * proportionOfFullPowerRBs);
		Set<Integer> fullPowerRBs = new HashSet<>();

		PsRandom generalRandom = (PsRandom) config.getProperty(FieldNames.RANDOM_GENERAL);

		while (fullPowerRBs.size() < numFullPowerRBs) {
			fullPowerRBs.add(generalRandom.nextInteger(0, config.getInt(FieldNames.RBS_PER_SECTOR)));
		}

		LOG.info("fullPowerRBs = {}", fullPowerRBs);

		for (ResourceBlock RB: resourceBlocks.getResourceBlocks()) {
			if (fullPowerRBs.contains(RB.id)) {
				RB.setFullPower();
			} else {
				RB.setLowPower();
			}
		}

	}

	@SuppressWarnings("boxing")
	protected void assignRegularFullPowerRBs() {
		double proportionOfFullPowerRBs = getProportionOfFullPowerRBs();
		int numFullPowerRBs = (int) (resourceBlocks.size() * proportionOfFullPowerRBs);

		int startIndex = 0;
		int endIndex = 0;

		if (id == 0) {
			startIndex = 0;
			endIndex = startIndex + numFullPowerRBs;
		} else if (id == 1) {
			int midPoint = config.getInt(FieldNames.RBS_PER_SECTOR) / 2;
			startIndex = midPoint - (numFullPowerRBs / 2);
			endIndex = startIndex + numFullPowerRBs;
		} else if (id == 2) {
			endIndex = config.getInt(FieldNames.RBS_PER_SECTOR);
			startIndex = endIndex - numFullPowerRBs;
		} else {
			throw new IllegalStateException("Invalid sector ID");
		}

		LOG.info("id = {}, propRBs = {}, numFullPowerRBs = {}, start = {}, end = {}", id, proportionOfFullPowerRBs, numFullPowerRBs, startIndex, endIndex);

		for (ResourceBlock RB: resourceBlocks.getResourceBlocks()) {
			if (RB.id >= startIndex && RB.id < endIndex) {
				RB.setFullPower();
			} else {
				RB.setLowPower();
			}
		}
	}

	@SuppressWarnings("boxing")
	@Override
	public void determineEdgeUEs() {
		int edgeCount = 0;
		double edgeThreshold = getEdgeThreshold();
		for (UE ue: servedUEs) {
			if (ue.getAverageSinr() <= edgeThreshold) {
				ue.setEdge(true);
				edgeCount += 1;
			} else {
				ue.setEdge(false);
			}
		}
		LOG.info("Number of edge UEs = {}", edgeCount);
	}

	@Override
	protected void doDownlinkAllocation(int iteration) {
		PsRandom generalRandom = (PsRandom) config.getProperty(FieldNames.RANDOM_GENERAL);
		double randomTrigger = getRandomTrigger();
		if (generalRandom.nextDouble() < randomTrigger) {
			LOG.info("Scheduling UEs randomly");
			randomDownlinkAllocation(iteration);
		} else {
			LOG.info("Scheduling UEs using standard SFR appraoch");
			super.doDownlinkAllocation(iteration);
		}
	}

	protected void randomDownlinkAllocation(final int iteration) {
		PsRandom generalRandom = (PsRandom) config.getProperty(FieldNames.RANDOM_GENERAL);
		List<UE> toSchedule = getUEsToSchedule();

		final List<ResourceBlock> unscheduledRBs = resourceBlocks.getUnscheduledRBs(iteration);

		int scheduledRBs = 0;
		for (ResourceBlock RB: unscheduledRBs) {
			if (toSchedule.isEmpty()) {
				break;
			}

			final int index = generalRandom.nextInteger(toSchedule.size() - 1);
			final UE ue = toSchedule.get(index);

			allocateRBToUE(ue, RB);
			scheduledRBs += 1;
			toSchedule = getUEsToSchedule();
		}

		updateScheduledRBCounters(scheduledRBs);
	}

	protected double getEdgeThreshold() {
		return config.getDouble(FieldNames.ADAPTIVE_EDGE_THRESHOLD);
	}

	protected double getRandomTrigger() {
		return config.getDouble(FieldNames.ADAPTIVE_RANDOM_TRIGGER);
	}

	protected double getProportionOfFullPowerRBs() {
		return config.getDouble(FieldNames.ADAPTIVE_PROPORTION_OF_HIGH_POWER_RBS);
	}

	protected boolean isRandomHighPowerRBs() {
		return config.getBoolean(FieldNames.ADAPTIVE_RANDOM_HIGH_POWER_RBS);
	}
}