package uk.co.downthewire.jLTE.simulator;

import static uk.co.downthewire.jLTE.simulator.utils.FieldNames.ENODEB_CONFIG;
import static uk.co.downthewire.jLTE.simulator.utils.FieldNames.NUM_UES;
import static uk.co.downthewire.jLTE.simulator.utils.FieldNames.RBS_PER_SECTOR;
import static uk.co.downthewire.jLTE.simulator.utils.FieldNames.RESULTS_PATH;
import static uk.co.downthewire.jLTE.simulator.utils.FieldNames.SCENARIO_PATH;
import static uk.co.downthewire.jLTE.simulator.utils.FieldNames.UE_CONFIG;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import uk.co.downthewire.jLTE.simulator.results.SimulationResults;
import uk.co.downthewire.jLTE.simulator.sectors.AbstractSector;
import uk.co.downthewire.jLTE.simulator.traffic.TrafficGenerator;
import uk.co.downthewire.jLTE.simulator.ue.UE;
import uk.co.downthewire.jLTE.simulator.utils.FieldNames;
import uk.co.downthewire.jLTE.simulator.x2.X2Router;
import flanagan.math.PsRandom;

public class Simulator extends AbstractConfiguredRunnable<SimulationResults> {

	private static final Logger LOG = LoggerFactory.getLogger(Simulator.class);

	public static void main(final String[] args) throws ConfigurationException, InterruptedException, IOException {
		Configuration configuration = new PropertiesConfiguration("system.properties").interpolatedConfiguration();
		Simulator sim = new Simulator(configuration);
		sim.doMain();
	}

	public Simulator(Configuration configuration) {
		super(configuration);
		long seed = (long) configuration.getDouble(FieldNames.SEED);
		configuration.setProperty(FieldNames.RANDOM_GENERAL, new PsRandom(seed));
		configuration.setProperty(FieldNames.RANDOM_LOCATION, new PsRandom(seed));
		configuration.setProperty(FieldNames.RANDOM_SHADOWING, new PsRandom(seed));
	}

	public void configureLogging() throws InterruptedException {
        MDC.put("userid", config.getString(SCENARIO_PATH) + config.getString(RESULTS_PATH) + logName(config));
		Thread.sleep(5000);
	}

	private static void printConfig(Configuration config) {
		Iterator<String> keyIterator = config.getKeys();
		while (keyIterator.hasNext()) {
			String key = keyIterator.next();
			LOG.error("{} - {}", key, config.getProperty(key));
		}
	}

	private static String logName(Configuration config) {
		final SimpleDateFormat formatter = new SimpleDateFormat("hhmmss_ddMMyyyy");
		String timestamp = formatter.format(new Date());

		String x2String = "";
		if (config.getBoolean(FieldNames.X2_ENABLED))
			x2String = "_x2";

		return config.getInt(FieldNames.EXPERIMENT_ID) + //
		"." + config.getInt(FieldNames.CHROMOSOME_ID) + //
		"_" + config.getString(FieldNames.ALGORITHM) + //
		x2String + //
		"_sp" + config.getInt(FieldNames.SPEED) + //
		"_ues" + config.getInt(FieldNames.NUM_UES) + //
		"_i" + config.getInt(FieldNames.ITERATIONS) + //
		"_" + config.getString(FieldNames.TRAFFIC_TYPE) + //
		"_s" + config.getDouble(FieldNames.SEED) + //
		"_" + timestamp + ".log";

	}

	@Override
	public String getId() {
		return config.getInt(FieldNames.EXPERIMENT_ID) + ":" + config.getInt(FieldNames.CHROMOSOME_ID);
	}

	@Override
	public SimulationResults call() throws InterruptedException, IOException {
		return doMain();
	}

	@SuppressWarnings("boxing")
	public SimulationResults doMain() throws InterruptedException, IOException {
		Thread.currentThread().setName(String.format("Sim_%d_%d_%d", Thread.currentThread().getId(),
                config.getInt(FieldNames.EXPERIMENT_ID), config.getInt(FieldNames.CHROMOSOME_ID)));
		configureLogging();

		final long time = System.currentTimeMillis();
		LOG.debug("Simulator started...");

		TrafficGenerator trafficGenerator = new TrafficGenerator(config);
		printConfig(config);

		List<ENodeB> enodebs = createENodeBs(config);
		List<AbstractSector> sectors = getAllSectors(enodebs);
		List<UE> ues = createUEs(config, enodebs.size(), sectors, trafficGenerator);

		int numTuples = ues.size() * sectors.size();
		FadingData fadingData = new FadingData(config, numTuples, config.getInt(RBS_PER_SECTOR));
		calcServingSectors(ues, sectors, fadingData);
		logSectors(sectors);

		X2Router x2Router = new X2Router(config, enodebs);

		final SimMain main = new SimMain(config, ues, sectors, fadingData, x2Router);
		main.run();
		SimulationResults results = new SimulationResults(main.finalStats(ues));

		LOG.debug("Time taken: {}", Double.toString((System.currentTimeMillis() - time) / 1000.0));
		return results;
	}

	private static void logSectors(List<AbstractSector> sectors) {
		for (AbstractSector sector: sectors) {
			LOG.debug(sector.toString());
		}
	}

	private static List<UE> createUEs(Configuration config, int numeNodeBs, List<AbstractSector> sectors,
                                      TrafficGenerator trafficGenerator) {

		List<UE> createdUEs = new ArrayList<>();
		if (config.getInt(NUM_UES) < 1) {

			// Create the UEs from XML.
			try {
				final File file = new File(config.getString(SCENARIO_PATH) + config.getString(UE_CONFIG));
				final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				final DocumentBuilder db = dbf.newDocumentBuilder();
				final Document doc = db.parse(file);
				doc.getDocumentElement().normalize();
				final NodeList nodes = doc.getElementsByTagName("UE");
				for (int s = 0; s < nodes.getLength(); s++) {
					final UE e = UE.fromXML(config, trafficGenerator, nodes.item(s));
					createdUEs.add(e);
				}
			} catch (final Exception e) {
				LOG.error("Error creating UEs: {}", e);
			}
		} else {
			// create the UEs randomly
			for (int i = 0; i < config.getInt(NUM_UES); i++) {
				final UE e = new UE(config, i, numeNodeBs, sectors, trafficGenerator);
				createdUEs.add(e);
			}

		}
		return createdUEs;
	}

	@SuppressWarnings("boxing")
	private static List<ENodeB> createENodeBs(Configuration config) {

		List<ENodeB> createdeNodeBs = new ArrayList<>();
		try {

			final File file = new File(config.getString(SCENARIO_PATH) + config.getString(ENODEB_CONFIG));
			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			final DocumentBuilder db = dbf.newDocumentBuilder();
			final Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			final NodeList nodes = doc.getElementsByTagName("eNodeB");
			for (int s = 0; s < nodes.getLength(); s++) {
				final ENodeB e = ENodeB.fromXML(config, nodes.item(s));
				LOG.info(e.toString());
				createdeNodeBs.add(e);
			}
		} catch (final Exception e) {
			LOG.error("Error creating eNodeBs: {}", e);
			throw new IllegalStateException(e);
		}

		// Neighbours are determined by their distance.
		for (ENodeB eNB1: createdeNodeBs) {
			for (ENodeB eNB2: createdeNodeBs) {
				// Cells are 500m apart, so the immediate neighbours will all be within 600m
				if (eNB1.id != eNB2.id && Location.calcDistance(eNB1.location, eNB2.location) < 0.6) {
					eNB1.addNeighbour(eNB2);
				}
			}
		}

		for (ENodeB enb: createdeNodeBs) {
			LOG.debug("eNB[{}]: num neighbours = {}", enb.id, enb.getNeighbours().size());
		}

		return createdeNodeBs;
	}

	private static List<AbstractSector> getAllSectors(List<ENodeB> eNodeBs) {
		List<AbstractSector> allSectors = new ArrayList<>();
		for (ENodeB nb: eNodeBs) {
			allSectors.addAll(nb.getSectors());
		}
		return allSectors;
	}

	@SuppressWarnings("boxing")
	public void calcServingSectors(List<UE> ues, List<AbstractSector> sectors, FadingData fadingData) {

		for (final UE ue: ues) {

			// Now create a list to hold sectors which could serve this UE
			final List<UESectorTuple> sectorList = new ArrayList<>();

			int id = 0;
			for (final AbstractSector sector: sectors) {
				final UESectorTuple tuple = new UESectorTuple(config, id++, sector, ue, fadingData);
				sectorList.add(tuple);
			}

			java.util.Collections.sort(sectorList);
			ue.sectorTuples.addAll(sectorList);
			ue.servingTuple = sectorList.get(sectorList.size() - 1);

			final AbstractSector servingSector = sectorList.get(sectorList.size() - 1).sector;
			servingSector.servedUEs.add(ue);

			for (UESectorTuple tuple: sectorList) {
				LOG.info(tuple.toString());
			}

			LOG.info("UE[{}] served by Sector[{}:{}]", ue.id, servingSector.servingENodeBId, servingSector.id);
		}
	}

}
