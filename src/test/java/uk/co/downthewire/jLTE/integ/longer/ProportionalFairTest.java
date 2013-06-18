package uk.co.downthewire.jLTE.integ.longer;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

import uk.co.downthewire.jLTE.integ.IntegConfiguration;
import uk.co.downthewire.jLTE.simulator.results.SimulationResults;
import uk.co.downthewire.jLTE.simulator.utils.FieldNames;

public class ProportionalFairTest {

	@Test
	public void longIntegTest() throws ConfigurationException, IOException {
		Configuration config = IntegConfiguration.getLongIntegTestConfig();
		config.setProperty(FieldNames.ALGORITHM, FieldNames.PROPORTIONATE_FAIR_ALGO);

		SimulationResults results = IntegConfiguration.runTest(config);

		assertEquals(0.63346, results.avergeTput, 0.000001);
		assertEquals(5.299016, results.maxUETput, 0.000001);
		assertEquals(0.106895, results.percentileTput, 0.000001);
	}

	@Test
	public void longX2IntegTest() throws ConfigurationException, IOException {
		Configuration config = IntegConfiguration.getLongX2IntegTestConfig();
		config.setProperty(FieldNames.ALGORITHM, FieldNames.PROPORTIONATE_FAIR_ALGO);

		SimulationResults results = IntegConfiguration.runTest(config);

		assertEquals(0.62924, results.avergeTput, 0.000001);
		assertEquals(0.106569, results.percentileTput, 0.000001);
	}

}
