package uk.co.downthewire.jLTE.integ;

import static uk.co.downthewire.jLTE.simulator.utils.FieldNames.ITERATIONS;
import static uk.co.downthewire.jLTE.simulator.utils.FieldNames.NUM_UES;
import static uk.co.downthewire.jLTE.simulator.utils.FieldNames.TRAFFIC_TYPE;

import java.io.IOException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import uk.co.downthewire.jLTE.simulator.Simulator;
import uk.co.downthewire.jLTE.simulator.results.SimulationResults;
import uk.co.downthewire.jLTE.simulator.utils.FieldNames;

public final class IntegConfiguration {

	private final static int DEFAULT_ITERATIONS = 1;
	private final static String DEFUALT_UES = "0";
	private final static String DEFAULT_TRAFFIC = "full";

	private final static int EXTENDED_ITERATIONS = 120;
	private final static String EXTENDED_UES = "1150";

	@SuppressWarnings("boxing")
	public static Configuration getDefaultIntegTestConfig() throws ConfigurationException {
		Configuration config = new PropertiesConfiguration("src/test/resources/integ-test.system.properties").interpolatedConfiguration();
		config.setProperty(ITERATIONS, DEFAULT_ITERATIONS);
		config.setProperty(NUM_UES, DEFUALT_UES);
		config.setProperty(TRAFFIC_TYPE, DEFAULT_TRAFFIC);

		return config;
	}

	@SuppressWarnings("boxing")
	public static Configuration getDefaultX2IntegTestConfig() throws ConfigurationException {
		Configuration config = getDefaultIntegTestConfig();
		config.setProperty(NUM_UES, EXTENDED_UES);
		config.setProperty(FieldNames.X2_ENABLED, true);
		config.setProperty(FieldNames.NUM_UES, "0");
		config.setProperty(FieldNames.ITERATIONS, 10);

		return config;
	}

	@SuppressWarnings("boxing")
	public static Configuration getLongIntegTestConfig() throws ConfigurationException {
		Configuration config = new PropertiesConfiguration("src/test/resources/integ-test.system.properties").interpolatedConfiguration();
		config.setProperty(ITERATIONS, EXTENDED_ITERATIONS);
		config.setProperty(NUM_UES, EXTENDED_UES);
		config.setProperty(TRAFFIC_TYPE, DEFAULT_TRAFFIC);
		config.setProperty(FieldNames.SCENARIO_PATH, "src/test/resources/03-19eNodeBs/");

		return config;
	}

	@SuppressWarnings("boxing")
	public static Configuration getLongX2IntegTestConfig() throws ConfigurationException {
		Configuration config = getLongIntegTestConfig();
		config.setProperty(FieldNames.X2_ENABLED, true);

		return config;
	}

	public static SimulationResults runTest(Configuration options) throws IOException {

		Simulator sim = new Simulator(options);
		SimulationResults results = null;
		try {
			results = sim.doMain();
		} catch (InterruptedException e) {
			// Do nothing
		}
		return results;
	}

	private IntegConfiguration() {
	}
}
