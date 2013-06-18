package uk.co.downthewire.jLTE.fading;

import flanagan.complex.Complex;
import flanagan.math.PsRandom;
import org.junit.Test;

import uk.co.downthewire.jLTE.fading.GenerateFading;

import static org.junit.Assert.assertEquals;

public class GenerateFadingTest {

    private final double speedInKmh = 3.0;
    private final int iterations = 128;

    private final double speedInMs = 1000 * (speedInKmh / 60 / 60); // in m/s
    private final double fm = GenerateFading.MHZ_20 * (1000 * speedInMs / GenerateFading.SPEED_OF_LIGHT); // max doppler shift
    private final PsRandom rand = new PsRandom(111);

    @Test
    public void dopplerFilterTest() {
        Complex[] output = GenerateFading.dopplerFilter(fm, GenerateFading.SAMPLING_FREQUENCY, iterations);
        // System.err.println(Arrays.toString(output));
        assertEquals(0.0, output[0].abs(), 0.1);
        assertEquals(0.178, output[1].abs(), 0.001);
    }

    @Test
    public void rayleighTest() {
        // System.err.println("Fm = " + Fm);
        // System.err.println("Fs = " + GenerateFading.SAMPLING_FREQUENCY);
        // System.err.println("Fm/Fs = " + Fm / GenerateFading.SAMPLING_FREQUENCY);
        float[] output = GenerateFading.rayleighFading(iterations, fm, GenerateFading.SAMPLING_FREQUENCY, rand);
        // for (float f: output) {
        // System.err.println(f);
        // }

        assertEquals(-1.612, output[0], 0.001);
        assertEquals(-1.726, output[1], 0.001);
    }
}
