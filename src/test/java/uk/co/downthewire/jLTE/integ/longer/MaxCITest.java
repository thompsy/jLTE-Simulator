package uk.co.downthewire.jLTE.integ.longer;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

import uk.co.downthewire.jLTE.integ.IntegConfiguration;
import uk.co.downthewire.jLTE.simulator.results.SimulationResults;
import uk.co.downthewire.jLTE.simulator.utils.FieldNames;

public class MaxCITest {

	@Test
	public void longIntegTest() throws ConfigurationException, IOException {
		Configuration config = IntegConfiguration.getLongIntegTestConfig();
		config.setProperty(FieldNames.ALGORITHM, FieldNames.MAXCI_ALGO);

		SimulationResults results = IntegConfiguration.runTest(config);

		assertEquals(1.567864, results.avergeTput, 0.000001);
		assertEquals(51.734616, results.maxUETput, 0.000001);
		assertEquals(0, results.percentileTput, 0.000001);
	}

	@Test
	public void longX2IntegTest() throws ConfigurationException, IOException {
		Configuration config = IntegConfiguration.getLongX2IntegTestConfig();
		config.setProperty(FieldNames.ALGORITHM, FieldNames.MAXCI_ALGO);

		SimulationResults results = IntegConfiguration.runTest(config);

		assertEquals(1.551352, results.avergeTput, 0.000001);
		assertEquals(51.173321, results.maxUETput, 0.000001);
		assertEquals(0, results.percentileTput, 0.000001);
	}

}
