package uk.co.downthewire.jLTE.simulator.sectors;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.w3c.dom.Node;

import uk.co.downthewire.jLTE.simulator.AdaptiveGenes;
import uk.co.downthewire.jLTE.simulator.ENodeB;
import uk.co.downthewire.jLTE.simulator.Location;
import uk.co.downthewire.jLTE.simulator.ue.UE;
import uk.co.downthewire.jLTE.simulator.utils.FieldNames;

public class DistributedSFRSector extends AdaptiveSFRSector {

	private AdaptiveGenes genes;
	private AdaptiveGenes oldGenes = null;

	private double priorDatarate;
	private boolean neighbourMutating = false;

	public static DistributedSFRSector fromXML(Configuration config, Node xml, ENodeB eNodeB, Location location) {
		SectorParams params = new SectorParams(xml);
		return new DistributedSFRSector(config, params.getSectorId(), eNodeB, location, params.getTxPower(), params.getAzimuth(), params.getHeight(), params.getDowntilt(), params.getAntennaGain());
	}

	private DistributedSFRSector(Configuration config, int sectorId, final ENodeB eNodeB, final Location loc, double txPower, double azimuth, double height, double downtilt, double antennaGain) {
		super(config, sectorId, eNodeB, loc, txPower, azimuth, height, downtilt, antennaGain);
	}

	@Override
	protected void setupRBs() {
		genes = AdaptiveGenes.create(config);
		if (isRandomHighPowerRBs()) {
			assignRandomFullPowerRBs();
		} else {
			assignRegularFullPowerRBs();
		}
	}

	@SuppressWarnings({ "unused", "boxing" })
	public void mutate(int iteration) {
		priorDatarate = getPercentileTput();

		for (DistributedSFRSector sector: getNeighbouringSectors()) {
			sector.notifyNeighbour();
		}

		LOG.error("Mutating sector:{}", id);
		oldGenes = genes;
		LOG.error("Genes prior to mutation: {}", genes);
		genes = genes.mutate();
		LOG.error("Genes after to mutation: {}", genes);
	}

	public void notifyNeighbour() {
		neighbourMutating = true;
		priorDatarate = getPercentileTput();
	}

	public List<DistributedSFRSector> getNeighbouringSectors() {
		List<DistributedSFRSector> sectors = new ArrayList<>();
		for (ENodeB neighbour: eNodeB.getNeighbours()) {
			for (AbstractSector sector: neighbour.getSectors()) {
				sectors.add((DistributedSFRSector) sector);
			}
		}
		return sectors;
	}

	@Override
	public void resetScheduledStatus() {
		resourceBlocks.resetScheduledStatus();
		for (UE ue: servedUEs) {
			ue.resetScheduledStatus();
		}

		if (isRandomHighPowerRBs()) {
			assignRandomFullPowerRBs();
		} else {
			assignRegularFullPowerRBs();
		}
	}

	@SuppressWarnings("boxing")
	public void evaluateMutation(int numberHappyNeighbours) {

		boolean acceptMutation = false;
		if (config.getBoolean(FieldNames.DISTRIBUTED_SFR_CONSENSUS)) {
			double proportionOfHappyNeighbours = ((double) numberHappyNeighbours) / ((double) getNeighbouringSectors().size());
			LOG.error("numberOfHappyNeighbours: {}, proportionOfHappyNeighbours = {}", numberHappyNeighbours, proportionOfHappyNeighbours);
			acceptMutation = hasTputIncreasedDuringMutationWindow() && proportionOfHappyNeighbours > config.getDouble(FieldNames.DISTRIBUTED_SFR_CONSENSUS_PROPORTION);
		} else {
			acceptMutation = hasTputIncreasedDuringMutationWindow();
		}

		if (acceptMutation) {
			LOG.error("Keeping mutation");
		} else {
			LOG.error("Discarding mutation");
			genes = oldGenes;
		}
		LOG.error("Final genes: {}", genes);
		oldGenes = null;
	}

	@SuppressWarnings("boxing")
	public boolean hasTputIncreasedDuringMutationWindow() {
		double percentileTput = getPercentileTput();
		LOG.error("sector[{}]: priorPercentileTput = {}, postPercentileTput = {}", id, priorDatarate, percentileTput);
		return priorDatarate < percentileTput;
	}

	@Override
	public double getReducedPowerFactor() {
		return genes.getReducedPowerFactor();
	}

	@Override
	protected double getEdgeThreshold() {
		return genes.getEdgeThreshold();
	}

	@Override
	protected double getRandomTrigger() {
		return genes.getRandomTrigger();
	}

	@Override
	protected double getProportionOfFullPowerRBs() {
		return genes.getProportionOfFullPowerRBs();
	}

	@Override
	protected boolean isRandomHighPowerRBs() {
		return genes.isRandomHighPowerRBs();
	}

	public void logHeader() {
		genes.logGenesHeader();
	}

	public void logGenes() {
		genes.logGenes();
	}

	public boolean isNeighbourMutating() {
		return neighbourMutating;
	}

	public void resetNeighbourMutating() {
		neighbourMutating = false;
	}

}