package uk.co.downthewire.jLTE.simulator;

import flanagan.math.PsRandom;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.downthewire.jLTE.genes.BooleanGene;
import uk.co.downthewire.jLTE.genes.DoubleGene;
import uk.co.downthewire.jLTE.genes.IGene;
import uk.co.downthewire.jLTE.simulator.utils.FieldNames;

import java.util.SortedMap;
import java.util.TreeMap;

import static uk.co.downthewire.jLTE.simulator.utils.FieldNames.*;

public final class AdaptiveGenes implements Cloneable {

    private static final Logger LOG = LoggerFactory.getLogger("Sim_" + Thread.currentThread().getId());

    @SuppressWarnings("rawtypes")
    private final SortedMap<String, IGene> genes;
    private final Configuration config;

    @SuppressWarnings("rawtypes")
    private AdaptiveGenes(Configuration config, SortedMap<String, IGene> genes) {
        this.config = config;
        this.genes = genes;
    }

    @SuppressWarnings("rawtypes")
    public static AdaptiveGenes create(Configuration config) {
        SortedMap<String, IGene> genes = new TreeMap<>();
        PsRandom generalRandom = (PsRandom) config.getProperty(FieldNames.RANDOM_GENERAL);

        if (config.getBoolean(FieldNames.DISTRIBUTED_SFR_RANDOM_START)) {
            genes.put(ADAPTIVE_EDGE_THRESHOLD, new DoubleGene(generalRandom, 0.0, 1.0));
            genes.put(ADAPTIVE_REDUCED_POWER_FACTOR, new DoubleGene(generalRandom, 0.0, 1.0));
            genes.put(ADAPTIVE_PROPORTION_OF_HIGH_POWER_RBS, new DoubleGene(generalRandom, 0.0, 1.0));
            genes.put(ADAPTIVE_RANDOM_TRIGGER, new DoubleGene(generalRandom, 0.0, 1.0));
            genes.put(ADAPTIVE_RANDOM_HIGH_POWER_RBS, new BooleanGene(generalRandom.nextInteger(0, 1) == 1));

        } else {
            genes.put(ADAPTIVE_EDGE_THRESHOLD, new DoubleGene(generalRandom, 0.0, 1.0, config.getDouble(ADAPTIVE_EDGE_THRESHOLD)));
            genes.put(ADAPTIVE_REDUCED_POWER_FACTOR, new DoubleGene(generalRandom, 0.0, 1.0, config.getDouble(ADAPTIVE_REDUCED_POWER_FACTOR)));
            genes.put(ADAPTIVE_PROPORTION_OF_HIGH_POWER_RBS, new DoubleGene(generalRandom, 0.0, 1.0, config.getDouble(ADAPTIVE_PROPORTION_OF_HIGH_POWER_RBS)));
            genes.put(ADAPTIVE_RANDOM_TRIGGER, new DoubleGene(generalRandom, 0.0, 1.0, config.getDouble(ADAPTIVE_RANDOM_TRIGGER)));
            genes.put(ADAPTIVE_RANDOM_HIGH_POWER_RBS, new BooleanGene(config.getBoolean(ADAPTIVE_RANDOM_HIGH_POWER_RBS)));
        }

        if (config.getBoolean(FieldNames.X2_ENABLED)) {
            genes.put(FieldNames.X2_MAX_RBS_PER_MSG, new DoubleGene(generalRandom, 0.0, 100.0, config.getDouble(FieldNames.X2_MAX_RBS_PER_MSG)));
            genes.put(FieldNames.X2_MSG_ALL_NEIGHBOURS, new BooleanGene(config.getBoolean(FieldNames.X2_MSG_ALL_NEIGHBOURS)));
            genes.put(FieldNames.X2_MSG_LIFE_TIME, new DoubleGene(generalRandom, 0.0, 100.0, config.getDouble(FieldNames.X2_MSG_LIFE_TIME)));
            genes.put(FieldNames.X2_MSG_WAIT_TIME, new DoubleGene(generalRandom, 0.0, 100.0, config.getDouble(FieldNames.X2_MSG_WAIT_TIME)));
        }

        return new AdaptiveGenes(config, genes);
    }

    public void logGenesHeader() {
        StringBuilder sb = new StringBuilder();
        for (String key : genes.keySet()) {
            sb.append(key).append(',');
        }
        LOG.error(sb.toString());
    }

    public void logGenes() {
        StringBuilder sb = new StringBuilder();
        for (String key : genes.keySet()) {
            sb.append(genes.get(key)).append(',');
        }
        LOG.error(sb.toString());
    }

    @SuppressWarnings({"rawtypes", "null"})
    public AdaptiveGenes mutate() {
        AdaptiveGenes clonedGenes = this.clone();
        PsRandom generalRandom = (PsRandom) config.getProperty(FieldNames.RANDOM_GENERAL);

        int randomIndex = generalRandom.nextInteger(clonedGenes.genes.keySet().size() - 1);
        int index = 0;
        IGene gene = null;
        String geneName = null;
        for (String key : clonedGenes.genes.keySet()) {
            if (index++ == randomIndex) {
                geneName = key;
                gene = clonedGenes.genes.get(key);
                break;
            }
        }

        LOG.error("Mutating gene: {} = {}", geneName, gene);
        assert gene != null;
        gene.mutate();
        LOG.error("Mutated gene: {} = {}", geneName, gene);
        return clonedGenes;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public AdaptiveGenes clone() {
        SortedMap<String, IGene> clonedGenes = new TreeMap<>();
        for (String key : genes.keySet()) {
            clonedGenes.put(key, genes.get(key).clone());
        }
        return new AdaptiveGenes(config, clonedGenes);
    }

    @SuppressWarnings("boxing")
    public double getReducedPowerFactor() {
        return (double) genes.get(FieldNames.ADAPTIVE_REDUCED_POWER_FACTOR).getValue();
    }

    @SuppressWarnings("boxing")
    public double getEdgeThreshold() {
        return (double) genes.get(FieldNames.ADAPTIVE_EDGE_THRESHOLD).getValue();
    }

    @SuppressWarnings("boxing")
    public double getRandomTrigger() {
        return (double) genes.get(FieldNames.ADAPTIVE_RANDOM_TRIGGER).getValue();
    }

    @SuppressWarnings("boxing")
    public double getProportionOfFullPowerRBs() {
        return (double) genes.get(FieldNames.ADAPTIVE_PROPORTION_OF_HIGH_POWER_RBS).getValue();
    }

    @SuppressWarnings("boxing")
    public boolean isRandomHighPowerRBs() {
        return (boolean) genes.get(FieldNames.ADAPTIVE_RANDOM_HIGH_POWER_RBS).getValue();
    }

    @Override
    public String toString() {
        return genes.toString();
    }
}
