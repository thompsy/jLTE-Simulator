package uk.co.downthewire.jLTE.simulator.traffic;

import flanagan.math.PsRandom;
import org.apache.commons.configuration.Configuration;

import uk.co.downthewire.jLTE.simulator.utils.FieldNames;

import static uk.co.downthewire.jLTE.simulator.utils.FieldNames.*;

public class TrafficGenerator {

    private final Configuration configuration;

    public TrafficGenerator(Configuration configuration) {
        this.configuration = configuration;
    }

    public double getDownlinkProb(TrafficType trafficType) {
        if (trafficType == TrafficType.LIGHT) {
            return configuration.getDouble(TRAFFIC_LIGHT_PROBABILITY);
        } else if (trafficType == TrafficType.MIXED) {
            return configuration.getDouble(TRAFFIC_MIXED_PROBABILITY);
        } else if (trafficType == TrafficType.HEAVY) {
            return configuration.getDouble(TRAFFIC_HEAVY_PROBABILITY);
        } else if (trafficType == TrafficType.FULL) {
            return configuration.getDouble(TRAFFIC_FULL_PROBABILITY);
        }

        throw new IllegalArgumentException("Unknown traffic type.");
    }

    public int getRBsToSend(TrafficType trafficType) {
        if (trafficType == TrafficType.LIGHT) {
            return configuration.getInt(TRAFFIC_LIGHT_RBS);
        } else if (trafficType == TrafficType.MIXED) {
            return configuration.getInt(TRAFFIC_MIXED_RBS);
        } else {
            return configuration.getInt(TRAFFIC_HEAVY_RBS);
        }
    }

    // TODO: this is still pretty nasty, but at least is a bit more tidy.
    public TrafficType getTrafficType(double rr) {

        if (configuration.getString(TRAFFIC_TYPE).equalsIgnoreCase(LIGHT)) {
            if (rr <= configuration.getDouble(TRAFFIC_LIGHT_PROPORTION_LIGHT)) {
                return TrafficType.LIGHT;
            } else if (rr <= configuration.getDouble(TRAFFIC_LIGHT_PROPORTION_LIGHT) + configuration.getDouble(TRAFFIC_LIGHT_PROPORTION_MIXED)) {
                return TrafficType.MIXED;
            } else {
                return TrafficType.HEAVY;
            }

        } else if (configuration.getString(TRAFFIC_TYPE).equalsIgnoreCase(MIXED)) {

            if (rr <= configuration.getDouble(TRAFFIC_MIXED_PROPORTION_LIGHT)) {
                return TrafficType.LIGHT;
            } else if (rr <= configuration.getDouble(TRAFFIC_MIXED_PROPORTION_LIGHT) + configuration.getDouble(TRAFFIC_MIXED_PROPORTION_MIXED)) {
                return TrafficType.MIXED;
            } else {
                return TrafficType.HEAVY;
            }

        } else if (configuration.getString(TRAFFIC_TYPE).equalsIgnoreCase(HEAVY)) {

            if (rr <= configuration.getDouble(TRAFFIC_HEAVY_PROPORTION_LIGHT)) {
                return TrafficType.LIGHT;
            } else if (rr <= configuration.getDouble(TRAFFIC_HEAVY_PROPORTION_LIGHT) + configuration.getDouble(TRAFFIC_HEAVY_PROPORTION_MIXED)) {
                return TrafficType.MIXED;
            } else {
                return TrafficType.HEAVY;
            }
        } else if (configuration.getString(TRAFFIC_TYPE).equalsIgnoreCase(FieldNames.FULL)) {
            return TrafficType.FULL;
        } else {
            throw new IllegalArgumentException("Invalid traffic type");
        }
    }

    public int generateTraffic(TrafficType trafficType, double random) {
        PsRandom generalRandom = (PsRandom) configuration.getProperty(FieldNames.RANDOM_GENERAL);
        if (trafficType == TrafficType.FULL) {
            return getRBsToSend(trafficType);
        }
        int numRBsToGenerate = 0;
        if (random > 1 - getDownlinkProb(trafficType)) {
            numRBsToGenerate = generalRandom.nextInteger(getRBsToSend(trafficType)) + 1;
        }
        return numRBsToGenerate;
    }
}
