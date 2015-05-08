package uk.co.downthewire.jLTE.fading;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.downthewire.jLTE.simulator.utils.FieldNames;

public final class ReadFading {

	private static final Logger LOG = LoggerFactory.getLogger(ReadFading.class);

	public static void main(String[] args) throws ClassNotFoundException, IOException, ConfigurationException {
		printFadingChannel(0);
	}

	@SuppressWarnings("boxing")
	private static void printFadingChannel(int channelId) throws IOException, ClassNotFoundException, ConfigurationException {

		Configuration configuration = new PropertiesConfiguration("system.properties").interpolatedConfiguration();
		String fadingDirectory = configuration.getString(FieldNames.FADING_PATH);

		double seed = configuration.getDouble(FieldNames.SEED);
        int numUEs = configuration.getInt(FieldNames.NUM_UES);
        int numSectors = 57;
        int numRBs = configuration.getInt(FieldNames.RBS_PER_SECTOR);
        int numFadingChannels = numUEs * numSectors * numRBs + 1;
        double speed = configuration.getDouble(FieldNames.SPEED);
		int numIterations = 128;

		for (int iteration = 0; iteration < numIterations; iteration++) {
            String filename = GenerateFading.generateFileName(fadingDirectory, numFadingChannels, speed, iteration, seed);
			ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(new File(filename)));
			float[] data = (float[]) inputStream.readObject();
			LOG.info("{}", data[channelId]);
			inputStream.close();
		}
	}

	private ReadFading() {
	}
}
