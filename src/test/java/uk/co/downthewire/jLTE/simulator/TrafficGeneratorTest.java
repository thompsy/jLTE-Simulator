package uk.co.downthewire.jLTE.simulator;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Before;
import org.junit.Test;

import uk.co.downthewire.jLTE.simulator.traffic.TrafficGenerator;
import uk.co.downthewire.jLTE.simulator.traffic.TrafficType;

import static org.junit.Assert.assertEquals;
import static uk.co.downthewire.jLTE.simulator.utils.FieldNames.TRAFFIC_TYPE;

public class TrafficGeneratorTest {

    private Configuration config;

    @Before
    public void setUp() throws ConfigurationException {
        config = new PropertiesConfiguration("src/test/resources/integ-test.system.properties").interpolatedConfiguration();
    }

    @Test
    public void lightTraffic() {
        config.setProperty(TRAFFIC_TYPE, "light");
        TrafficGenerator trafficGenerator = new TrafficGenerator(config);

        assertEquals(TrafficType.LIGHT, trafficGenerator.getTrafficType(0.7));
        assertEquals(TrafficType.MIXED, trafficGenerator.getTrafficType(0.8));
        assertEquals(TrafficType.HEAVY, trafficGenerator.getTrafficType(0.9));
    }

    @Test
    public void mixedTraffic() {
        config.setProperty(TRAFFIC_TYPE, "mixed");
        TrafficGenerator trafficGenerator = new TrafficGenerator(config);

        assertEquals(TrafficType.LIGHT, trafficGenerator.getTrafficType(0.2));
        assertEquals(TrafficType.MIXED, trafficGenerator.getTrafficType(0.8));
        assertEquals(TrafficType.HEAVY, trafficGenerator.getTrafficType(0.9));
    }

    @Test
    public void heavyTraffic() {
        config.setProperty(TRAFFIC_TYPE, "heavy");
        TrafficGenerator trafficGenerator = new TrafficGenerator(config);

        assertEquals(TrafficType.HEAVY, trafficGenerator.getTrafficType(0.1));
        assertEquals(TrafficType.HEAVY, trafficGenerator.getTrafficType(0.5));
        assertEquals(TrafficType.HEAVY, trafficGenerator.getTrafficType(0.9));
    }

    @Test
    public void fullTraffic() {
        config.setProperty(TRAFFIC_TYPE, "full");
        TrafficGenerator trafficGenerator = new TrafficGenerator(config);

        assertEquals(TrafficType.FULL, trafficGenerator.getTrafficType(0.1));
        assertEquals(TrafficType.FULL, trafficGenerator.getTrafficType(0.5));
        assertEquals(TrafficType.FULL, trafficGenerator.getTrafficType(0.9));
    }

    @Test
    public void rbsToSend() {
        config.setProperty(TRAFFIC_TYPE, "heavy");
        TrafficGenerator trafficGenerator = new TrafficGenerator(config);

        assertEquals(5, trafficGenerator.getRBsToSend(TrafficType.LIGHT));
        assertEquals(15, trafficGenerator.getRBsToSend(TrafficType.MIXED));
        assertEquals(100, trafficGenerator.getRBsToSend(TrafficType.HEAVY));
    }

    @Test
    public void downlinkProbability() {
        config.setProperty(TRAFFIC_TYPE, "heavy");
        TrafficGenerator trafficGenerator = new TrafficGenerator(config);

        assertEquals(0.2, trafficGenerator.getDownlinkProb(TrafficType.LIGHT), 0.1);
        assertEquals(0.5, trafficGenerator.getDownlinkProb(TrafficType.MIXED), 0.1);
        assertEquals(0.8, trafficGenerator.getDownlinkProb(TrafficType.HEAVY), 0.1);
    }

}
