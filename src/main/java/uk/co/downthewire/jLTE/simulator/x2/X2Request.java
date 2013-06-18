package uk.co.downthewire.jLTE.simulator.x2;

import uk.co.downthewire.jLTE.simulator.rbs.ResourceBlock;

import java.util.List;

public class X2Request {

    public final int eNodeBId;
    private final double load;
    private final List<ResourceBlock> RBs;
    private final int time;

    public X2Request(final int eNB, final double load, final List<ResourceBlock> rbs, final int time) {
        this.eNodeBId = eNB;
        this.load = load;
        this.RBs = rbs;
        this.time = time;
    }

    @Override
    public String toString() {
        return "{eNB-Id: " + eNodeBId + ", load: " + load + ", RBs: " + getRBs() + ", t: " + time + "}";
    }

    public int startTime() {
        return time;
    }

    public List<ResourceBlock> getRBs() {
        return RBs;
    }

}