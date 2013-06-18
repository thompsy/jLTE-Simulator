package uk.co.downthewire.jLTE.simulator;

import org.junit.Test;

import uk.co.downthewire.jLTE.simulator.Location;
import uk.co.downthewire.jLTE.simulator.UESectorTuple;

import static org.junit.Assert.assertEquals;

public class UESectorTupleTest {

    @SuppressWarnings("static-method")
    @Test
    public void pathLoss() {
        Location location1 = new Location(1, 1);
        Location location2 = new Location(1.4, 1.3);
        assertEquals(117.052, UESectorTuple.pathLoss(location1, location2), 0.3);
    }

    @SuppressWarnings("static-method")
    @Test
    public void gamma() {
        Location location1 = new Location(1.4, 1.3);
        Location location2 = new Location(1, 1);
        double downtilt = 0;
        double azimuth = 30;
        double height = 30;

        assertEquals(0.445, UESectorTuple.gamma(location1, location2, downtilt, azimuth, height), 0.001);
    }

    @SuppressWarnings("static-method")
    @Test
    public void calculateDownlinkGain() {
        Location location1 = new Location(1.4, 1.3);
        Location location2 = new Location(1, 1);
        double downtilt = 0;
        double azimuth = 30;
        double height = 30;

        double sectorAntennaGain = 15;
        double ueAntennaGain = 0;
        double uePrenetrationGain = 10;
        assertEquals(2.777E-12, UESectorTuple.calculateDownlinkGain(location1, location2, downtilt, azimuth, height, sectorAntennaGain, ueAntennaGain, uePrenetrationGain), 1.0E-12);

    }
}
