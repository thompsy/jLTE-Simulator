package uk.co.downthewire.jLTE.simulator;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.downthewire.jLTE.simulator.rbs.ResourceBlock;
import uk.co.downthewire.jLTE.simulator.sectors.AbstractSector;
import uk.co.downthewire.jLTE.simulator.ue.UE;
import uk.co.downthewire.jLTE.simulator.ue.UETypeInfo;
import uk.co.downthewire.jLTE.simulator.utils.AntennaUtils;
import uk.co.downthewire.jLTE.simulator.utils.FieldNames;

public class UESectorTuple implements Comparable<UESectorTuple> {

    private static final Logger LOG = LoggerFactory.getLogger("Sim_" + Thread.currentThread().getId());

    /**
     * Calculate the path loss component (in dB) of downlink gain
     */
    public static double pathLoss(Location ueLocation, Location sectorLocation) {
        double distance = Location.calcDistance(ueLocation, sectorLocation);
        return 128.1 + (36.7 * Math.log10(distance));
    }

    public static double gamma(Location ueLocation, Location sectorLocation, double sectorDowntilt, double sectorAzimuth, double sectorHeight) {
        // Some variables we need.
        final double xs = ueLocation.x * 1000;
        final double ys = ueLocation.y * 1000;

        final double xa = sectorLocation.x * 1000;
        final double ya = sectorLocation.y * 1000;

        final double mech_dt = Math.toRadians(sectorDowntilt);

        final double x = xs - xa;
        final double y = ys - ya;

        double alphaS;
        if (y < 0) {
            alphaS = (2 * Math.PI) - Math.acos((1.0 / Math.sqrt((x * x) + (y * y))) * x);
        } else {
            alphaS = Math.acos((1.0 / Math.sqrt((x * x) + (y * y))) * x);
        }

        final double alphaA = sectorAzimuth;
        final double za = sectorHeight;
        final double zs = UETypeInfo.ANTENNA_HEIGHT;

        final double alpha_rad = (alphaS - (alphaA * Math.PI / 180) + 2 * Math.PI) % (2 * Math.PI);

        final double phi_rad = Math.acos((zs - za) / Math.sqrt(Math.pow(xs - xa, 2) + Math.pow(ys - ya, 2) + Math.pow(zs - za, 2)));

        double temp, alpha_mod;
        alpha_mod = Math.cos(alpha_rad) * Math.cos(mech_dt) - (1.0 / Math.tan(phi_rad)) * Math.sin(mech_dt);
        temp = ((alpha_mod) * (alpha_mod)) + (Math.sin(alpha_rad) * Math.sin(alpha_rad));
        alpha_mod = Math.acos(alpha_mod / Math.sqrt(temp));

        double alpha_rad_final;
        if (0 <= alpha_rad && alpha_rad < Math.PI) {
            alpha_rad_final = alpha_mod;
        } else {
            alpha_rad_final = (2 * Math.PI) - alpha_mod;
        }

        final double phi_rad_final = Math.acos(Math.sin(mech_dt) * Math.sin(phi_rad) * Math.cos(alpha_rad) + Math.cos(mech_dt) * Math.cos(phi_rad));
        final double alpha_deg = alpha_rad_final * (180 / Math.PI) % 360;
        final double phi_deg = phi_rad_final * (180 / Math.PI);

        double dk;
        if (alpha_deg <= 90 || alpha_deg >= 270) {
            dk = 270 - phi_deg;
        } else {
            dk = (270 + phi_deg) % 360;
        }

        final int kh = (int) (Math.floor(alpha_deg));
        final int kv = (int) (Math.floor(dk));

        final double h = AntennaUtils.antenna_gain_matrix_h[kh] + ((alpha_deg - kh) * (AntennaUtils.antenna_gain_matrix_h[(int) ((kh + 1) % 360.0)] - AntennaUtils.antenna_gain_matrix_h[kh]));
        final double v = AntennaUtils.antenna_gain_matrix_v[kv] + ((dk - kv) * (AntennaUtils.antenna_gain_matrix_v[(int) ((kv + 1) % 360.0)] - AntennaUtils.antenna_gain_matrix_v[kv]));

        final double gamma = h * v;
        return gamma;
    }

    private final int id;
    private final UE ue;
    public final AbstractSector sector;
    private final Configuration config;
    private final FadingData fadingData;
    private final double txPowerPerRB;
    private final double downlinkGain;

    /**
     * Create a tuple with the provided sector and UE.
     */
    @SuppressWarnings("boxing")
    public UESectorTuple(Configuration config, int id, final AbstractSector sector, final UE ue, FadingData fadingData) {
        this.id = id;
        this.config = config;
        this.ue = ue;
        this.sector = sector;
        this.fadingData = fadingData;
        this.downlinkGain = calculateDownlinkGain(sector, ue);
        this.txPowerPerRB = (sector.txPower / config.getInt(FieldNames.RBS_PER_SECTOR)) * downlinkGain;
        LOG.trace("UE[{}] at {}, Sector[{}:{}] at {}, txPowerPerRB={}", ue.id, ue.location, sector.servingENodeBId, sector.id, sector.location, txPowerPerRB);
    }

    public static double calculateDownlinkGain(AbstractSector sector, UE ue) {
        return calculateDownlinkGain(ue.location, sector.location, sector.downtilt, sector.azimuth, sector.height, sector.antennaGain, UETypeInfo.ANTENNA_GAIN, UETypeInfo.PENETRATION_LOSS);
    }

    public static double calculateDownlinkGain(Location ueLocation, Location sectorLocation, double sectorDowntilt, double sectorAzimuth, double sectorHeight, double sectorAntennaGain, double ueAntennaGain, double uePenetrationGain) {
        double gamma = gamma(ueLocation, sectorLocation, sectorDowntilt, sectorAzimuth, sectorHeight);
        double localPathloss = pathLoss(ueLocation, sectorLocation);
        double pathloss = Math.pow(10, -0.1 * localPathloss);
        double downlinkGain = Math.pow(10, 0.1 * sectorAntennaGain) * Math.pow(10, 0.1 * ueAntennaGain) * Math.pow(10, -0.1 * uePenetrationGain) * pathloss * gamma;
        return downlinkGain;
    }

    @Override
    public int compareTo(final UESectorTuple other) {
        return Double.valueOf(downlinkGain).compareTo(other.downlinkGain);
    }

    /**
     * Return the downlink power for the given RB.
     */
    @SuppressWarnings("boxing")
    public double getDownlinkPower(ResourceBlock RB) {
        if (!config.getBoolean(FieldNames.FADING)) {
            LOG.trace("txPowerPerRB = {}", txPowerPerRB);
            return txPowerPerRB;
        }
        float[] fading = fadingData.getFadingDataForTuple(id);
        double fadingFactor = Math.pow(10, 0.1 * fading[RB.id]);
        double resultPower = txPowerPerRB * fadingFactor;
        LOG.trace("txPowerPerRB = {}, fading = {}, result = {}", txPowerPerRB, fadingFactor, resultPower);
        return resultPower;
    }

    @Override
    public String toString() {
        return "Tuple[" + ue.id + ", " + sector.servingENodeBId + ":" + sector.id + "]\t" + "downlinkGain=" + downlinkGain + "\ttxPowerPerRB=" + txPowerPerRB;
    }
}