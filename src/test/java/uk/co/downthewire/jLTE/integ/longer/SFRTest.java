package uk.co.downthewire.jLTE.integ.longer;

import static org.junit.Assert.assertEquals;
import static uk.co.downthewire.jLTE.simulator.utils.FieldNames.SFR_ALGO;

import java.io.IOException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

import uk.co.downthewire.jLTE.integ.IntegConfiguration;
import uk.co.downthewire.jLTE.simulator.results.SimulationResults;
import uk.co.downthewire.jLTE.simulator.utils.FieldNames;

public class SFRTest {

	@Test
	public void longIntegTest() throws ConfigurationException, IOException {
		Configuration config = IntegConfiguration.getLongIntegTestConfig();
		config.setProperty(FieldNames.ALGORITHM, SFR_ALGO);

		SimulationResults results = IntegConfiguration.runTest(config);
		assertEquals(0.651168, results.avergeTput, 0.000001);
		assertEquals(5.717894, results.maxUETput, 0.000001);
		assertEquals(0.135034, results.percentileTput, 0.000001);
	}

	@Test
	public void longX2IntegTest() throws ConfigurationException, IOException {
		Configuration config = IntegConfiguration.getLongX2IntegTestConfig();
		config.setProperty(FieldNames.ALGORITHM, SFR_ALGO);

		SimulationResults results = IntegConfiguration.runTest(config);
		assertEquals(0.646409, results.avergeTput, 0.000001);
		assertEquals(5.573835, results.maxUETput, 0.000001);
		assertEquals(0.136248, results.percentileTput, 0.000001);
	}

}
