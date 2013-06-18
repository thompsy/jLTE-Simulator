package uk.co.downthewire.jLTE.simulator;

import flanagan.math.PsRandom;
import org.apache.commons.configuration.Configuration;

import uk.co.downthewire.jLTE.simulator.sectors.AbstractSector;
import uk.co.downthewire.jLTE.simulator.utils.FieldNames;

import java.io.Serializable;
import java.util.List;

import static uk.co.downthewire.jLTE.simulator.utils.FieldNames.CELL_RANGE;
import static uk.co.downthewire.jLTE.simulator.utils.FieldNames.MIN_DISTANCE_FROM_CELL;

public class Location implements Serializable {

    private static final long serialVersionUID = 1714606679582616477L;

    public final double x;
    public final double y;

    public Location(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "" + x + ", " + y + "";
    }

    public static Location generateRandomLocation(Configuration config, int numEnodebs, List<AbstractSector> sectors) {

        if (numEnodebs != 19) {
            throw new IllegalArgumentException("Only 19 eNodeBs are currently supported with randomly generated UE locations.");
        }
        PsRandom random = (PsRandom) config.getProperty(FieldNames.RANDOM_LOCATION);

        double maxCoord = 3.0;
        double minCoord = 0.0;

        while (true) {
            double x = random.nextDouble() * (maxCoord - minCoord);
            double y = random.nextDouble() * (maxCoord - minCoord);

            int goodLocations = 0;
            for (AbstractSector s : sectors) {
                double dist = calcDistance(new Location(x, y), s.location);
                if (dist > config.getDouble(MIN_DISTANCE_FROM_CELL) && dist < config.getDouble(CELL_RANGE)) {
                    goodLocations += 1;
                }
            }
            if (goodLocations > 0) {
                return new Location(x, y);
            }
        }
    }

    /**
     * Calculate the distance between two points
     */
    public static double calcDistance(final Location l1, final Location l2) {
        return Math.sqrt(Math.pow((l1.x - l2.x), 2) + Math.pow((l1.y - l2.y), 2));
    }
}