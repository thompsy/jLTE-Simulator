package uk.co.downthewire.jLTE.simulator.utils;

public final class FieldNames {

    public final static String TESTING = "system.testing";

    public final static String SEED = "scenario.seed";
    public final static String ITERATIONS = "scenario.iterations";
    public final static String SCENARIO_PATH = "scenario.path";
    public final static String SPEED = "scenario.speed";
    public final static String NUM_UES = "scenario.numRandomUEs";
    public final static String ALGORITHM = "scenario.algorithm";

    public final static String UE_CONFIG = "scenario.path.ues";
    public final static String ENODEB_CONFIG = "scenario.path.enodebs";
    public final static String RESULTS_PATH = "scenario.path.results";

    public final static String EXPERIMENT_ID = "experiment.id";
    public final static String CHROMOSOME_ID = "chromosome.id";

    public final static String FADING_PATH = "lte.fadingPath";
    public final static String FADING_PATH_TESTING = "lte.fadingPath.testing";
    public final static String RBS_PER_SECTOR = "lte.RBsPerSector";
    public final static String FADING = "lte.fading";
    public final static String SHADOWING = "lte.shadowing";
    public final static String CELL_RANGE = "lte.cellRange";
    public final static String MIN_DISTANCE_FROM_CELL = "lte.minDistanceFromCell";
    public final static String NIQUIST_NOISE_PER_RB = "lte.nyquistNoisePerRB";

    public final static String TRAFFIC_TYPE = "traffic.type";

    public final static String TRAFFIC_LIGHT_RBS = "traffic.light.rbs";
    public final static String TRAFFIC_LIGHT_PROBABILITY = "traffic.light.ueProbability";
    public final static String TRAFFIC_LIGHT_PROPORTION_LIGHT = "traffic.light.proportion.light";
    public final static String TRAFFIC_LIGHT_PROPORTION_MIXED = "traffic.light.proportion.mixed";

    public final static String TRAFFIC_MIXED_RBS = "traffic.mixed.rbs";
    public final static String TRAFFIC_MIXED_PROBABILITY = "traffic.mixed.ueProbability";
    public final static String TRAFFIC_MIXED_PROPORTION_LIGHT = "traffic.mixed.proportion.light";
    public final static String TRAFFIC_MIXED_PROPORTION_MIXED = "traffic.mixed.proportion.mixed";

    public final static String TRAFFIC_HEAVY_RBS = "traffic.heavy.rbs";
    public final static String TRAFFIC_HEAVY_PROBABILITY = "traffic.heavy.ueProbability";
    public final static String TRAFFIC_HEAVY_PROPORTION_LIGHT = "traffic.heavy.proportion.light";
    public final static String TRAFFIC_HEAVY_PROPORTION_MIXED = "traffic.heavy.proportion.mixed";

    public final static String TRAFFIC_FULL_PROBABILITY = "traffic.full.ueProbability";

    public final static String SFR_EDGE_USERS = "algorithm.sfr.edgeUsers";
    public final static String SFR_NUM_HIGH_POWER_RBS = "algorithm.sfr.numHighPowerRBs";

    public final static String SERFR_GAMMA0 = "algorithm.serfr.gamma0";
    public final static String SERFR_GAMMA1 = "algorithm.serfr.gamma1";
    public final static String SERFR_GAMMA2 = "algorithm.serfr.gamma2";
    public final static String SERFR_GAMMA3 = "algorithm.serfr.gamma3";

    public final static String ADAPTIVE_EDGE_THRESHOLD = "algorithm.adaptive.edgeThreshold";
    public final static String ADAPTIVE_REDUCED_POWER_FACTOR = "algorithm.adaptive.reducedPowerFactor";
    public final static String ADAPTIVE_PROPORTION_OF_HIGH_POWER_RBS = "algorithm.adaptive.proportionOfHighPowerRBs";
    public final static String ADAPTIVE_RANDOM_TRIGGER = "algorithm.adaptive.randomTrigger";
    public final static String ADAPTIVE_RANDOM_HIGH_POWER_RBS = "algorithm.adaptive.randomHighPowerRBs";

    public final static String X2_ENABLED = "algorithm.x2.enabled";
    public final static String X2_MSG_WAIT_TIME = "algorithm.x2.msgWaitTime";
    public final static String X2_MSG_LIFE_TIME = "algorithm.x2.msgLifeTime";
    public final static String X2_MAX_RBS_PER_MSG = "algorithm.x2.maxRBsPerMsg";
    public final static String X2_MSG_ALL_NEIGHBOURS = "algorithm.x2.msgAllNeighbours";

    public final static String DISTRIBUTED_SFR_WINDOW = "algorithm.peer2peer.window";
    public final static String DISTRIBUTED_SFR_RANDOM_START = "algorithm.peer2peer.randomStart";
    public final static String DISTRIBUTED_SFR_CONSENSUS = "algorithm.peer2peer.consensus";
    public final static String DISTRIBUTED_SFR_CONSENSUS_PROPORTION = "algorithm.peer2peer.consensusProportion";

    public final static String RANDOM_ALGO = "Random";
    public final static String PROPORTIONATE_FAIR_ALGO = "ProportionalFair";
    public final static String MAXCI_ALGO = "MaxCI";
    public final static String SFR_ALGO = "SFR";
    public final static String SERFR_ALGO = "SerFR";
    public final static String ADAPTIVE_SFR = "AdaptiveSFR";
    public final static String DISTRIBUTED_SFR = "DistributedSFR";

    public final static String LIGHT = "LIGHT";
    public final static String MIXED = "MIXED";
    public final static String HEAVY = "HEAVY";
    public final static String FULL = "FULL";

    public final static String RANDOM_SHADOWING = "random.shadowing";
    public final static String RANDOM_GENERAL = "random.general";
    public final static String RANDOM_LOCATION = "random.location";

    public final static String TRAFFIC_UPLINK_PROB = "traffic.uplink.probability";

    private FieldNames() {
    }

}
