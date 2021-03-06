package uk.co.downthewire.jLTE.integ;

import static org.junit.Assert.assertEquals;
import static uk.co.downthewire.jLTE.simulator.utils.FieldNames.SFR_ALGO;

import java.io.IOException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

import uk.co.downthewire.jLTE.simulator.results.SimulationResults;
import uk.co.downthewire.jLTE.simulator.utils.FieldNames;

public class SFRAlgoIntegTest {

	@Test
	public void singleUE() throws ConfigurationException, IOException {
		Configuration config = IntegConfiguration.getDefaultIntegTestConfig();
		config.setProperty(FieldNames.SCENARIO_PATH, "src/test/resources/01-1eNodeB-1UE/");
		config.setProperty(FieldNames.ALGORITHM, SFR_ALGO);

		SimulationResults results = IntegConfiguration.runTest(config);
		assertEquals(50.605774, results.avergeTput, 0.000001);
		assertEquals(50.605774, results.percentileTput, 0.000001);
	}

	@Test
	public void threeUEs() throws ConfigurationException, IOException {
		Configuration config = IntegConfiguration.getDefaultIntegTestConfig();
		config.setProperty(FieldNames.SCENARIO_PATH, "src/test/resources/02-1eNodeB-3UEs/");
		config.setProperty(FieldNames.ALGORITHM, SFR_ALGO);

		SimulationResults results = IntegConfiguration.runTest(config);
		assertEquals(50.605774, results.avergeTput, 0.000001);
		assertEquals(50.605774, results.percentileTput, 0.000001);
	}

	@Test
	public void withInterference() throws ConfigurationException, IOException {
		Configuration config = IntegConfiguration.getDefaultIntegTestConfig();
		config.setProperty(FieldNames.SCENARIO_PATH, "src/test/resources/04-2eNodeBs-2UEs/");
		config.setProperty(FieldNames.ALGORITHM, SFR_ALGO);

		SimulationResults results = IntegConfiguration.runTest(config);
		assertEquals(39.959359, results.avergeTput, 0.000001);
		assertEquals(39.465394, results.percentileTput, 0.000001);
	}
}
