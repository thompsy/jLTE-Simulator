package uk.co.downthewire.jLTE.simulator.ue;

import flanagan.math.PsRandom;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import uk.co.downthewire.jLTE.simulator.Location;
import uk.co.downthewire.jLTE.simulator.UESectorTuple;
import uk.co.downthewire.jLTE.simulator.rbs.ResourceBlock;
import uk.co.downthewire.jLTE.simulator.sectors.AbstractSector;
import uk.co.downthewire.jLTE.simulator.traffic.TrafficGenerator;
import uk.co.downthewire.jLTE.simulator.traffic.TrafficType;
import uk.co.downthewire.jLTE.simulator.utils.FieldNames;
import uk.co.downthewire.jLTE.simulator.utils.Utils;
import uk.co.downthewire.jLTE.simulator.utils.XMLUtils;
import uk.co.downthewire.jLTE.stats.SimpleCounter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class UE {

    private final double noise;
    private static final Logger LOG = LoggerFactory.getLogger("Sim_" + Thread.currentThread().getId());

    public static UE fromXML(Configuration config, TrafficGenerator trafficGenerator, final Node xml) {
        int id = Integer.parseInt(xml.getAttributes().getNamedItem("id").getNodeValue());
        Location loc = XMLUtils.parseLocation(xml);
        return new UE(config, id, loc, trafficGenerator);
    }

    public final int id;
    private final double SHADOWING;
    public final Location location;

    // NOTE: See 3GPP 36.942 for these definitons
    private static final double MIN_SINR = -15;
    private static final double BANDWIDTH_PER_RB = 180000;
    private static final double BPS_TO_MBPS = 1024 * 1024;

    private final Configuration config;
    private final TrafficGenerator trafficGenerator;
    private final TrafficType trafficType;
    public UESectorTuple servingTuple;

    public final List<UESectorTuple> sectorTuples;

    private final List<ResourceBlock> scheduledRBs;

    private int currentRBsQueued;

    private final SimpleCounter totalDatarates;
    private final SimpleCounter totalSinr;
    private final SimpleCounter totalRBsServed;
    //private final SimpleCounter totalRBsQueued;
    private final SimpleCounter ulRBsQueued;
    private final SimpleCounter dlRBsQueued;
    private final SimpleCounter[] signalPerRB;

    private boolean isEdge;

    /**
     * Create a UE at a random location.
     */
    public UE(Configuration configuration, final int Id, int numeNodeBs, List<AbstractSector> sectors, TrafficGenerator trafficGenerator) {
        this(configuration, Id, Location.generateRandomLocation(configuration, numeNodeBs, sectors), trafficGenerator);
    }

    public UE(Configuration configuration, final int id, Location location, TrafficGenerator trafficGenerator) {
        this.config = configuration;
        this.trafficGenerator = trafficGenerator;
        this.id = id;
        this.location = location;
        this.noise = Math.pow(10, 0.1 * config.getDouble(FieldNames.NIQUIST_NOISE_PER_RB)) / 1000;

        this.sectorTuples = new ArrayList<>();

        this.totalRBsServed = new SimpleCounter();
        //this.totalRBsQueued = new SimpleCounter();
        this.ulRBsQueued = new SimpleCounter();
        this.dlRBsQueued = new SimpleCounter();
        this.totalDatarates = new SimpleCounter();
        this.totalSinr = new SimpleCounter();

        signalPerRB = new SimpleCounter[config.getInt(FieldNames.RBS_PER_SECTOR)];
        for (int RB = 0; RB < config.getInt(FieldNames.RBS_PER_SECTOR); RB++) {
            signalPerRB[RB] = new SimpleCounter();
        }

        this.scheduledRBs = new ArrayList<>();

        PsRandom shadowRandom = (PsRandom) config.getProperty(FieldNames.RANDOM_SHADOWING);
        this.SHADOWING = shadowRandom.nextGaussian(0, 8);

        PsRandom generalRandom = (PsRandom) config.getProperty(FieldNames.RANDOM_GENERAL);
        this.trafficType = trafficGenerator.getTrafficType(generalRandom.nextDouble());

        this.isEdge = false;
    }

    public void resetScheduledStatus() {
        scheduledRBs.clear();

    }

    /**
     * Update the DL/UL queue for the UE.
     */
    public void generateTraffic(double random) {
        int numRbs = trafficGenerator.generateTraffic(trafficType, random);
        // decide if demand traffic is UL or DL
        if (random > config.getDouble(FieldNames.TRAFFIC_UPLINK_PROB)) {
            // accumulate UL queue
            ulRBsQueued.accumulate(numRbs);
        }
        else {
            // accumulate DL queue
            dlRBsQueued.accumulate(numRbs);
        }
        currentRBsQueued += numRbs;
    }

    public void schedule(ResourceBlock RB, boolean isDL) {
        scheduledRBs.add(RB);
        if (isDL) {
            dlRBsQueued.accumulate(-1.0);
        }
        else {
            ulRBsQueued.accumulate(-1.0);
        }
        currentRBsQueued -= 1;
        totalRBsServed.accumulate(1.0);
    }

    @SuppressWarnings("boxing")
    // calculate the signal first
    public void calculateSignalAcrossAllRBs() {
        for (ResourceBlock RB : servingTuple.sector.getResourceBlocks()) {
            double signal = calculateSignal(RB);
            LOG.trace("calculateSignalAcrossAllRBs: UE[{}], RB[{}], signal={}", id, RB.id, signal);
            signalPerRB[RB.id].accumulate(signal);
        }
    }

    public double getSignalOnRB(int RB) {
        return signalPerRB[RB].getLastSample();
    }

    public double getRelativeSignalOnRB(int RB) {
        return signalPerRB[RB].getLastSample() / signalPerRB[RB].getAverage();
    }

    public double getAverageSinr() {
        return totalSinr.getAverage();
    }

    @SuppressWarnings("boxing")
    public void accumulateDatarate() {
        double accumulatedDatarate = 0.0;
        double accumulatedSinr = 0.0;
        // calculate the signal first
        for (ResourceBlock RB : scheduledRBs) {
            double signal = signalPerRB[RB.id].getLastSample();
            double interference = calculateInterference(RB);
            double sinr = calcSinr(signal, interference);
            double datarate = calculateMbpsFromSinr(sinr);

            accumulatedDatarate += datarate;
            accumulatedSinr += sinr / config.getInt(FieldNames.RBS_PER_SECTOR);

            RB.accumulateDataRate(datarate);
            RB.accumulateSinr(sinr);

            LOG.trace("calculateDatarate: UE[{}], RB[{}], signal={}, interference={}, sinr={}, datarate={}", id, RB.id, signal, interference, sinr, datarate);
        }
        totalDatarates.accumulate(accumulatedDatarate);
        totalSinr.accumulate(accumulatedSinr);
    }

    private double calculateSignal(ResourceBlock RB) {
        double downlinkPower = servingTuple.getDownlinkPower(RB);
        double scheduledPowerFactor = servingTuple.sector.getScheduledPowerFactor(RB);
        double rxSignal = downlinkPower * scheduledPowerFactor;
        signalPerRB[RB.id].accumulate(rxSignal);
        return rxSignal;
    }

    private double calculateInterference(ResourceBlock RB) {
        double totalLinearInterference = 0.0;
        for (final UESectorTuple tuple : sectorTuples) {
            if (tuple.sector.id == servingTuple.sector.id && tuple.sector.servingENodeBId == servingTuple.sector.servingENodeBId) {
                continue;
            }
            if (tuple.sector.isRBScheduled(RB)) {
                double interference = tuple.getDownlinkPower(RB) * tuple.sector.getScheduledPowerFactor(RB);
                totalLinearInterference += interference;
            }
        }
        return totalLinearInterference;
    }

    private double calcSinr(double signal, double interference) {
        return signal / (noise + interference);
    }

    /**
     * Given the SINR (in dB) what would the datarate be (in Mbps). This estimates the maximum datarate based on the most applicable Modulation and Coding Scheme (MCS). See 3GPP 36.942: section A.1
     */
    private static double calculateMbpsFromSinr(double sinr) {
        double bps_per_hz = sinr > MIN_SINR ? Utils.log2(1 + sinr) * UETypeInfo.ATTENUATION_FACTOR : 0;
        if (bps_per_hz > 4.4) {
            bps_per_hz = 4.4;
        }
        return bps_per_hz * BANDWIDTH_PER_RB / BPS_TO_MBPS;
    }

    public double calculateAverageDatarate() {
        return totalDatarates.getAverage();
    }

    public double calculateAverageSinr() {
        return totalSinr.getAverage();
    }

    public void setEdge(boolean edge) {
        isEdge = edge;
    }

    public boolean isEdge() {
        return isEdge;
    }

    public int getCurrentRBsQueued() {
        return currentRBsQueued;
    }

    public int getTotalNumDlRBsServed() {
        return (int) totalRBsServed.getCount();
    }

    public int getTotalNumDlRBsQueued() {
        return (int) (ulRBsQueued.getCount() + dlRBsQueued.getCount());
    }

    public double getLastDatarate() {
        return totalDatarates.getLastSample();
    }

    @SuppressWarnings("boxing")
    public void logLine() {
        final DecimalFormat df = new DecimalFormat("00.000E00");
        final DecimalFormat dd = new DecimalFormat("00.00");
        LOG.trace("UEs|\t{}\ts{}:{}\t({},{})\t\t{}\t\t{}\t{}\t{}\t{}\t{}\t{}\t\t{}\t\t\t{}", id, //
                servingTuple.sector.servingENodeBId, //
                servingTuple.sector.id, //
                dd.format(location.x), //
                dd.format(location.y), //
                dd.format(SHADOWING), //
                scheduledRBs.size(), //
                df.format(totalDatarates.getLastSample()), //
                totalRBsServed.getCount(), //
                scheduledRBs);
    }

    @SuppressWarnings("boxing")
    @Override
    public String toString() {
        return String.format("UE[%d]", id);
    }
}
