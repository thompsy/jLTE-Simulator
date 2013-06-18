package uk.co.downthewire.jLTE.integ.longer;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

import uk.co.downthewire.jLTE.integ.IntegConfiguration;
import uk.co.downthewire.jLTE.simulator.results.SimulationResults;
import uk.co.downthewire.jLTE.simulator.utils.FieldNames;

public class AdaptiveSFRTest {

	@Test
	public void longIntegTest() throws ConfigurationException, IOException {
		Configuration config = IntegConfiguration.getLongIntegTestConfig();
		config.setProperty(FieldNames.ALGORITHM, FieldNames.ADAPTIVE_SFR);

		SimulationResults results = IntegConfiguration.runTest(config);
		assertEquals(0.643121, results.avergeTput, 0.000001);
		assertEquals(11.908312, results.maxUETput, 0.000001);
		assertEquals(0.106867, results.percentileTput, 0.000001);
	}

	@Test
	public void longX2IntegTest() throws ConfigurationException, IOException {
		Configuration config = IntegConfiguration.getLongX2IntegTestConfig();
		config.setProperty(FieldNames.ALGORITHM, FieldNames.ADAPTIVE_SFR);

		SimulationResults results = IntegConfiguration.runTest(config);
		assertEquals(0.638358, results.avergeTput, 0.000001);
		assertEquals(12.135785, results.maxUETput, 0.000001);
		assertEquals(0.104455, results.percentileTput, 0.000001);
	}

	@SuppressWarnings("boxing")
	@Test
	public void essentiallyRandom() throws ConfigurationException, IOException {
		Configuration config = IntegConfiguration.getLongIntegTestConfig();
		config.setProperty(FieldNames.ALGORITHM, FieldNames.ADAPTIVE_SFR);

		// setup options that should lead to the same behaviour as Random
		config.setProperty(FieldNames.ADAPTIVE_EDGE_THRESHOLD, 0.25);
		config.setProperty(FieldNames.ADAPTIVE_PROPORTION_OF_HIGH_POWER_RBS, 1.0);
		config.setProperty(FieldNames.ADAPTIVE_RANDOM_HIGH_POWER_RBS, false);
		config.setProperty(FieldNames.ADAPTIVE_RANDOM_TRIGGER, 1.0);

		SimulationResults results = IntegConfiguration.runTest(config);
		assertEquals(0.627, results.avergeTput, 0.001);
		assertEquals(4.553332, results.maxUETput, 0.000001);
		assertEquals(0.147147, results.percentileTput, 0.000001);
	}

}
