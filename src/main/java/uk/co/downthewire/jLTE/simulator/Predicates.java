package uk.co.downthewire.jLTE.simulator;

import com.google.common.base.Predicate;
import fj.F;
import uk.co.downthewire.jLTE.simulator.rbs.ResourceBlock;
import uk.co.downthewire.jLTE.simulator.sectors.AbstractSector;
import uk.co.downthewire.jLTE.simulator.ue.UE;

public class Predicates {

    public static final Predicate<UE> UE_ALWAYS_TRUE = new Predicate<UE>() {
        @Override
        public boolean apply(UE ue) {
            return true;
        }
    };

    public static final Predicate<AbstractSector> SECTOR_ALWAYS_TRUE = new Predicate<AbstractSector>() {
        @Override
        public boolean apply(AbstractSector s) {
            return true;
        }
    };

    public static final Predicate<UE> IS_EDGE_UE = new Predicate<UE>() {
        @Override
        public boolean apply(UE ue) {
            return ue.isEdge();
        }
    };

    public static final Predicate<UE> IS_CENTRAL_UE = new Predicate<UE>() {
        @Override
        public boolean apply(UE ue) {
            return !ue.isEdge();
        }
    };

    public static final F<UE, Double> GET_AVERAGE_UE_TPUT = new F<UE, Double>() {
        @SuppressWarnings("boxing")
        @Override
        public Double f(UE ue) {
            return ue.calculateAverageDatarate();
        }
    };

    public static final F<UE, Double> GET_AVERAGE_UE_SINR = new F<UE, Double>() {
        @SuppressWarnings("boxing")
        @Override
        public Double f(UE ue) {
            return ue.calculateAverageSinr();
        }
    };

    public static final Predicate<UE> HAS_NEVER_BEEN_SCHEDULED = new Predicate<UE>() {
        @Override
        public boolean apply(UE ue) {
            return ue.getTotalNumDlRBsServed() < 1;
        }
    };

    public static final Predicate<UE> HAS_NEVER_BEEN_SCHEDULED_AND_HAD_DATA = new Predicate<UE>() {
        @Override
        public boolean apply(UE ue) {
            return ue.getTotalNumDlRBsServed() < 1 && ue.getCurrentRBsQueued() > 0;
        }
    };

    public static final F<UE, Double> UE_NUM_RBS_SERVED = new F<UE, Double>() {
        @SuppressWarnings("boxing")
        @Override
        public Double f(UE ue) {
            return (double) ue.getTotalNumDlRBsServed();
        }
    };

    public static final F<UE, Double> UE_NUM_RBS_QUEUED = new F<UE, Double>() {
        @SuppressWarnings("boxing")
        @Override
        public Double f(UE ue) {
            return (double) ue.getTotalNumDlRBsQueued();
        }
    };

    /**
     * Sector based predicates
     */
    public static final F<AbstractSector, Double> SECTOR_TPUT = new F<AbstractSector, Double>() {
        @SuppressWarnings("boxing")
        @Override
        public Double f(AbstractSector sector) {
            return sector.getAvgDownlinkTput();
        }
    };

    public static final F<AbstractSector, Double> SECTOR_UES_SERVED = new F<AbstractSector, Double>() {
        @SuppressWarnings("boxing")
        @Override
        public Double f(AbstractSector sector) {
            return (double) sector.servedUEs.size();
        }
    };

    public static final F<AbstractSector, Double> SECTOR_AVERAGE_LOAD = new F<AbstractSector, Double>() {
        @SuppressWarnings("boxing")
        @Override
        public Double f(AbstractSector sector) {
            return sector.calcAvgLoad();
        }
    };

    public static final F<AbstractSector, Double> SECTOR_EDGE_UES_SERVED = new F<AbstractSector, Double>() {
        @SuppressWarnings("boxing")
        @Override
        public Double f(AbstractSector sector) {
            return sector.getNumEdgeUEs();
        }
    };

    public static final F<AbstractSector, Double> SECTOR_AVERAGE_RBS_BLOCKED = new F<AbstractSector, Double>() {
        @SuppressWarnings("boxing")
        @Override
        public Double f(AbstractSector sector) {
            return sector.getAverageRBsBlocked();
        }
    };

    public static final F<AbstractSector, Double> SECTOR_TOTAL_RBS_BLOCKED = new F<AbstractSector, Double>() {
        @SuppressWarnings("boxing")
        @Override
        public Double f(AbstractSector sector) {
            return sector.getTotalRBsBlocked();
        }
    };

    public static final Predicate<ResourceBlock> FULL_POWER_RBS = new Predicate<ResourceBlock>() {
        @Override
        public boolean apply(ResourceBlock rb) {
            return rb.isFullPowerRB();
        }
    };
}
