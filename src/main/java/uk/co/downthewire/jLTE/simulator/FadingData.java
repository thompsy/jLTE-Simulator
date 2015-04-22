package uk.co.downthewire.jLTE.simulator;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.downthewire.jLTE.simulator.utils.FieldNames;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import static uk.co.downthewire.jLTE.simulator.utils.FieldNames.TESTING;

public class FadingData {

    private static final Logger LOG = LoggerFactory.getLogger("Sim_" + Thread.currentThread().getId());

    protected float[][] fading;
    protected final int numTuples;
    protected final int numRBs;
    private final Configuration config;
    private final int numFadingChannels;

    public FadingData(Configuration config, int numTuples, int numRBs) {
        this.config = config;
        this.numRBs = numRBs;
        this.numTuples = numTuples;
        int numUEs = config.getInt(FieldNames.NUM_UES);
        int numSectors = 57;
        this.numFadingChannels = numUEs * numSectors * numRBs + 1;
    }

    @SuppressWarnings("boxing")
    public float[] getFadingDataForTuple(int tupleId) {
        LOG.trace("getFadingDataForTuple - {}", tupleId);
        return fading[tupleId];
    }

    @SuppressWarnings("boxing")
    private static String generateFileName(String path, int numFadingChannels, int iteration, int speed, double seed) {
        return String.format("%sfading-iteration_%d-channels_%d-seed_%s-speed_%d.0kmh.bin", path, iteration, numFadingChannels, String.valueOf(seed), speed);
    }

    /**
     * Read in the next set of fading numbers.
     *
     * @throws IOException
     */
    public void readFading(int iteration) throws IOException {
        int modifiedIteration = iteration % 120;

        float[][] fadingValues = new float[numTuples][numRBs];

        String file = null;
        if (config.getString(TESTING).equals("${env:lte.testing}"))
            file = generateFileName(config.getString(FieldNames.FADING_PATH), numFadingChannels, modifiedIteration,
                    config.getInt(FieldNames.SPEED), config.getDouble(FieldNames.SEED));

        else
            file = generateFileName(config.getString(FieldNames.FADING_PATH_TESTING), numFadingChannels,
                    modifiedIteration, config.getInt(FieldNames.SPEED), config.getDouble(FieldNames.SEED));


        LOG.trace("Reading fading file: {}", file);
        try {
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
            float[] data = (float[]) inputStream.readObject();
            inputStream.close();
            for (int tupleId = 1; tupleId <= numTuples; tupleId++) {
                for (int RB = 1; RB < numRBs; RB++) {
                    fadingValues[tupleId - 1][RB - 1] = data[tupleId * RB - 1];
                }
            }

        } catch (Exception e) {
            LOG.error("Error reading fading data from {} - {}", file, e);
            throw new IOException("Error reading fading data from " + file + " - " + e);
        }

        this.fading = fadingValues;
    }
}
