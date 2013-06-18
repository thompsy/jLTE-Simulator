package uk.co.downthewire.jLTE.simulator;

import flanagan.math.PsRandom;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Before;
import org.junit.Test;

import uk.co.downthewire.jLTE.simulator.ENodeB;
import uk.co.downthewire.jLTE.simulator.Location;
import uk.co.downthewire.jLTE.simulator.UESectorTuple;
import uk.co.downthewire.jLTE.simulator.sectors.AbstractSector;
import uk.co.downthewire.jLTE.simulator.sectors.RandomSector;
import uk.co.downthewire.jLTE.simulator.traffic.TrafficGenerator;
import uk.co.downthewire.jLTE.simulator.ue.UE;
import uk.co.downthewire.jLTE.simulator.utils.FieldNames;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class UETest {

    private UE ue;

    @Before
    public void setUp() throws ConfigurationException {
        Configuration config = new PropertiesConfiguration("src/test/resources/integ-test.system.properties").interpolatedConfiguration();
        config.setProperty(FieldNames.SHADOWING, Boolean.FALSE);
        config.addProperty(FieldNames.RANDOM_SHADOWING, new PsRandom((long) 11111.11111));
        config.addProperty(FieldNames.RANDOM_GENERAL, new PsRandom((long) 11111.11111));

        Location ueLocation = new Location(1.4, 1.3);
        Location sectorLocation = new Location(1.0, 1.0);

        MockFadingData fadingData = new MockFadingData(config, 10, 100);

        ENodeB eNodeB = new ENodeB(config, 1, sectorLocation, new ArrayList<AbstractSector>());
        AbstractSector sector = new RandomSector(config, 1, eNodeB, sectorLocation, 20000, 60, 30, 0, 0);
        ue = new UE(config, 0, ueLocation, new TrafficGenerator(config));

        UESectorTuple tuple = new UESectorTuple(config, 1, sector, ue, fadingData);

        List<UESectorTuple> tupleList = new ArrayList<>();
        tupleList.add(tuple);

        ue.sectorTuples.addAll(tupleList);
        ue.servingTuple = tuple;
    }

    @Test
    public void generateTraffic() {
        doTraffic();
        assertEquals(64, ue.getTotalNumDlRBsQueued());
        assertEquals(64, ue.getCurrentRBsQueued());
    }

    @Test
    public void accumulateDatarate() {
        ue.accumulateDatarate();
        assertEquals(0.0, ue.calculateAverageDatarate(), 0.1);
        assertEquals(0.0, ue.calculateAverageSinr(), 0.1);
    }

    private void doTraffic() {
        ue.generateTraffic(1.0);
    }

}
