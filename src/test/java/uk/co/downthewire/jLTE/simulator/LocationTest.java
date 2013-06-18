package uk.co.downthewire.jLTE.simulator;

import org.junit.Test;

import uk.co.downthewire.jLTE.simulator.Location;

import static org.junit.Assert.assertEquals;

public class LocationTest {

    private static final Location location1 = new Location(1.0, 1.0);
    private static final Location location2 = new Location(2.0, 2.0);

    @SuppressWarnings("static-method")
    @Test
    public void distanceCalc() {
        assertEquals(1.4142d, Location.calcDistance(location1, location2), 0.01);
    }

}
