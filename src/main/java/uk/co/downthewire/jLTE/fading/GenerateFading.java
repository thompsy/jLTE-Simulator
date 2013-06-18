package uk.co.downthewire.jLTE.fading;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import uk.co.downthewire.jLTE.simulator.utils.FieldNames;
import flanagan.complex.Complex;
import flanagan.math.FourierTransform;
import flanagan.math.PsRandom;

/*
 * This class is a stand-alone utility that is used to generate 
 * the values used to model the fast-fading. These values are stored
 * in a single file per iteration.
 */
public final class GenerateFading {

	private static final Logger LOG = LoggerFactory.getLogger(GenerateFading.class);

	// For Guassian random variables
	private static final double VARIANCE = 0.5;
	private static final double STD_DEV = Math.sqrt(VARIANCE);

	public static final double SPEED_OF_LIGHT = 299792458; // in m/s
	public static final double SAMPLING_FREQUENCY = 1000;
	public static final double MHZ_20 = 2000 * 1000; // 2000 MHz in Hz

	@SuppressWarnings("boxing")
	public static void main(String[] args) throws IOException, ConfigurationException {
		Configuration configuration = new PropertiesConfiguration("system.properties").interpolatedConfiguration();

		String fadingDirectory = configuration.getString(FieldNames.FADING_PATH);

		int numUEs = 1150;
		int numSectors = 57;
		int numRBs = 100;
		int numFadingChannels = numUEs * numSectors * numRBs;
		double[] seeds = { 11111.11111 };
		double[] speeds = { 3.0 };

		int iterations = 128;

		for (double seed: seeds) {
			for (double speed: speeds) {
				PsRandom random = new PsRandom((long) seed);

				long startTime = System.currentTimeMillis();
				float[][] data = generateFadingData(numFadingChannels, iterations, speed, random);
				long dataCreationTime = System.currentTimeMillis() - startTime;

				LOG.info("Data Generated for seed: {}, {} km/h in: {} seconds", seed, speed, (dataCreationTime / 1000));

				writeFilePerIteration(fadingDirectory, data, speed, seed);
				long timeTaken = System.currentTimeMillis() - startTime - dataCreationTime;

				LOG.info("Data written in: {} seconds", timeTaken / 1000);
			}
		}
	}

	private static void writeFilePerIteration(String directory, float[][] fadingData, double speedInKmh, double seed) throws IOException {

		int numFadingChannels = fadingData.length;
		int numIterations = fadingData[0].length;

		for (int iteration = 0; iteration < numIterations; iteration++) {
			String filename = generateFileName(directory, numFadingChannels, speedInKmh, iteration, seed);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(new File(filename)));
			float[] dataForIteration = new float[numFadingChannels];
			for (int channel = 0; channel < numFadingChannels; channel++) {
				dataForIteration[channel] = fadingData[channel][iteration];
			}
			objectOutputStream.writeObject(dataForIteration);
			objectOutputStream.close();
		}
	}

	@SuppressWarnings("boxing")
	public static String generateFileName(String directory, int numFadingChannels, double speedInKMH, int iteration, double seed) {
		return String.format("%sfading-iteration_%d-channels_%d-seed_%s-speed_%d.0kmh.bin", directory, iteration, numFadingChannels, String.valueOf(seed), (int) speedInKMH);
	}

	private static float[][] generateFadingData(int numFadingChannels, int numIterations, double speedInKMH, PsRandom random) {
		int iterationsToPowerOfTwo = FourierTransform.nextPowerOfTwo(numIterations);
		double speedInMS = 1000 * (speedInKMH / 60 / 60);

		double maxDopplerShift = MHZ_20 * (1000 * speedInMS / SPEED_OF_LIGHT); // max doppler shift

		float[][] data = new float[numFadingChannels + 1][iterationsToPowerOfTwo];

		for (int channel = 0; channel <= numFadingChannels; channel++) {
			data[channel] = rayleighFading(iterationsToPowerOfTwo, maxDopplerShift, SAMPLING_FREQUENCY, random);
		}
		return data;
	}

	protected static Complex[] dopplerFilter(double Fm, double Fs, int N) {
		Complex[] F = new Complex[N];

		double doppler_ratio = Fm / Fs;
		double km = doppler_ratio * 256;

		for (int i = 1; i <= N; i++) {
			if (i == 1)
				F[i - 1] = new Complex();
			else if (i >= 2 && i <= km) {
				Complex x = new Complex(1 - (i) / Math.pow(N * doppler_ratio, 2.0));
				Complex y = new Complex(x.sqrt().times(2.0));
				Complex z = new Complex(1.0);
				Complex w = z.over(y).sqrt();

				F[i - 1] = w;
			} else if (i == km + 1) {
				throw new NotImplementedException();
			} else if (i >= km + 2 && i <= N - km + 2) {
				F[i - 1] = new Complex();
			} else if (i == N * km) {
				throw new NotImplementedException();
			} else {
				Complex x = new Complex(1 - (N - i) / Math.pow(N * doppler_ratio, 2.0));
				Complex y = new Complex(x.sqrt().times(2.0));
				Complex z = new Complex(1.0);
				Complex w = z.over(y).sqrt();
				F[i - 1] = w;
			}
		}
		return F;
	}

	/*
	 * Returns a double array whose length is the number of iterations
	 */
	@SuppressWarnings("null")
	protected static float[] rayleighFading(int N, double Fm, double Fs, PsRandom rand) {

		Complex[] C = new Complex[N];
		for (int i = 0; i < N; i++) {
			C[i] = new Complex(STD_DEV * rand.nextGaussian(), STD_DEV * rand.nextGaussian());
		}

		Complex[] Fk = null;
		try {
			Fk = dopplerFilter(Fm, Fs, N);
		} catch (Exception e) {
			LOG.error("Error generating rayleighFading: {}", e);
		}

		Complex[] U = new Complex[N];
		for (int i = 0; i < N; i++) {
			assert Fk != null;
			U[i] = C[i].times(Fk[i]);
		}

		FourierTransform t = new FourierTransform(U);
		t.inverse();
		Complex[] tr = t.getTransformedDataAsComplex();

		float[] u = new float[N];

		double max = 0;
		for (int i = 0; i < N; i++) {
			u[i] = (float) tr[i].abs();
			if (u[i] > max)
				max = u[i];
		}

		for (int i = 0; i < N; i++) {
			u[i] = (float) (10 * Math.log10(u[i] / max));
		}

		return u;
	}

	@SuppressWarnings("unused")
	private static void writeToSingleFile(String directory, float[][] fadingData, double speedInKmh, double seed) throws IOException {
		int numFadingChannels = fadingData.length;
		int numIterations = fadingData[0].length;

		String filename = generateFileName(directory, numFadingChannels, speedInKmh, numIterations, seed);
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename)));
		for (int iteration = 0; iteration < numIterations; iteration++) {
			for (float[] aFadingData: fadingData) {
				writer.write(aFadingData[iteration] + ",");
			}
			writer.write("\n");
		}
		writer.close();
	}

	private GenerateFading() {
	}
}