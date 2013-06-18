package uk.co.downthewire.jLTE.stats;

import org.junit.Before;
import org.junit.Test;

import uk.co.downthewire.jLTE.stats.ValueDistribution;

import static org.junit.Assert.assertEquals;

public class TestValueDistribution {

    private ValueDistribution values;

    @Before
    public void setUp() {
        values = new ValueDistribution();
        for (double d = 0.0; d < 100.0; d++) {
            values.add(d);
        }
    }

    @Test
    public void total() {
        assertEquals(4950.0, values.total(), 0.1);
    }

    @SuppressWarnings("static-method")
    @Test
    public void empty() {
        ValueDistribution empty = new ValueDistribution();
        assertEquals(0.0, empty.average(), 0.1);
        assertEquals(0.0, empty.max(), 0.1);
        assertEquals(0.0, empty.min(), 0.1);
        assertEquals(0.0, empty.average5thPrecentile(), 0.1);
        assertEquals(0.0, empty.total(), 0.1);
    }

    @Test
    public void average() {
        assertEquals(49.5, values.average(), 0.1);
    }

    @Test
    public void max() {
        assertEquals(99.0, values.max(), 0.1);
    }

    @Test
    public void min() {
        assertEquals(0.0, values.min(), 0.1);
    }

    @Test
    public void precentile() {
        assertEquals(2.0, values.average5thPrecentile(), 0.1);
    }

    @SuppressWarnings("static-method")
    @Test
    public void precentileVeryFewValues() {
        ValueDistribution smallValues = new ValueDistribution();
        for (double d = 1.0; d <= 5.0; d++) {
            smallValues.add(d);
        }

        assertEquals(1.0, smallValues.average5thPrecentile(), 0.1);
    }

    @SuppressWarnings("static-method")
    @Test
    public void precentileFewValues() {
        ValueDistribution smallValues = new ValueDistribution();
        for (double d = 1.0; d <= 20.0; d++) {
            smallValues.add(d);
        }

        assertEquals(1.0, smallValues.average5thPrecentile(), 0.1);
    }
}
