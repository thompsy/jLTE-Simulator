package uk.co.downthewire.jLTE.simulator;

import org.apache.commons.configuration.Configuration;

import uk.co.downthewire.jLTE.simulator.FadingData;

import java.util.Arrays;

public class MockFadingData extends FadingData {

    public MockFadingData(Configuration config, int numTuples, int numRBs) {
        super(config, numTuples, numRBs);
        readFading(0);
    }

    /**
     * Read in the next set of fading numbers.
     */
    @Override
    public void readFading(int iteration) {

        float[][] fadingValues = new float[numTuples][numRBs];

        for (float[] fadingValue : fadingValues) {
            Arrays.fill(fadingValue, 1.0f);
        }

        this.fading = fadingValues;
    }

}
