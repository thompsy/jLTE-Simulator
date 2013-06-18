package uk.co.downthewire.jLTE.simulator.rbs;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.downthewire.jLTE.simulator.utils.FieldNames;
import uk.co.downthewire.jLTE.simulator.x2.X2Request;
import uk.co.downthewire.jLTE.stats.SimpleCounter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResourceBlocks {

    private static final Logger LOG = LoggerFactory.getLogger("Sim_" + Thread.currentThread().getId());

    private final List<ResourceBlock> resourceBlocks;
    private final Configuration config;
    private final SimpleCounter numRBsBlocked;
    private final List<X2Request> x2Requests;

    public ResourceBlocks(Configuration config) {

        this.config = config;
        this.numRBsBlocked = new SimpleCounter();
        this.x2Requests = new ArrayList<>();

        this.resourceBlocks = new ArrayList<>();
        for (int id = 0; id < config.getInt(FieldNames.RBS_PER_SECTOR); id++) {
            resourceBlocks.add(new ResourceBlock(id));
        }
    }

    public void resetScheduledStatus() {
        for (ResourceBlock rb : resourceBlocks) {
            rb.resetScheduledStatus();
        }
    }

    public int size() {
        return resourceBlocks.size();
    }

    public List<ResourceBlock> getUnscheduledRBs(int iteration) {
        List<ResourceBlock> unscheduledRBs = new ArrayList<>(resourceBlocks);
        unscheduledRBs.removeAll(getReservedRBs(iteration));

        return unscheduledRBs;
    }

    private Set<ResourceBlock> getReservedRBs(int iteration) {
        Set<ResourceBlock> reservedRBs = new HashSet<>();
        for (X2Request request : x2Requests) {
            if (iteration < config.getInt(FieldNames.X2_MSG_LIFE_TIME) + request.startTime()) {
                reservedRBs.addAll(request.getRBs());
            }
        }
        LOG.info("Blocked the following RBs: {} ", reservedRBs);
        numRBsBlocked.accumulate(reservedRBs.size());
        return reservedRBs;
    }

    public List<ResourceBlock> getResourceBlocks() {
        return new ArrayList<>(resourceBlocks);
    }

    public void reserveRBs(X2Request request) {
        x2Requests.add(request);
    }

    public double getAverageRBsBlocked() {
        return numRBsBlocked.getAverage();
    }

    public double getTotalRBsBlocked() {
        return numRBsBlocked.getCount();
    }

    public boolean isScheduled(ResourceBlock rb) {
        ResourceBlock resourceBlock = resourceBlocks.get(rb.id);
        if (resourceBlock.id != rb.id) {
            throw new IllegalStateException("RBs are not correctly sorted.");
        }
        return resourceBlock.isScheduled();
    }

}
