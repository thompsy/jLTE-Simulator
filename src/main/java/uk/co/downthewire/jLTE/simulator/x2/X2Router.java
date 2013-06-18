package uk.co.downthewire.jLTE.simulator.x2;

import flanagan.math.PsRandom;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.downthewire.jLTE.simulator.ENodeB;
import uk.co.downthewire.jLTE.simulator.utils.FieldNames;

import java.util.ArrayList;
import java.util.List;

public class X2Router {

    private static final Logger LOG = LoggerFactory.getLogger("Sim_" + Thread.currentThread().getId());

    private final List<ENodeB> eNodeBs;
    private final Configuration config;

    public X2Router(Configuration config, List<ENodeB> eNBs) {
        this.config = config;
        this.eNodeBs = eNBs;
    }

    @SuppressWarnings("boxing")
    public void routeRequests(int iteration, List<X2Request> requests) {
        LOG.info("Routing {} X2 requests", requests.size());
        PsRandom generalRandom = (PsRandom) config.getProperty(FieldNames.RANDOM_GENERAL);
        boolean sendToAllNeighbours = config.getBoolean(FieldNames.X2_MSG_ALL_NEIGHBOURS);

        for (X2Request request : requests) {
            ENodeB sendingENodeB = eNodeBs.get(request.eNodeBId);

            List<ENodeB> neigbours = sendingENodeB.getNeighbours();
            List<ENodeB> recipients;
            if (sendToAllNeighbours) {
                recipients = neigbours;
            } else {
                int randomIndex = generalRandom.nextInteger(neigbours.size() - 1);
                recipients = new ArrayList<>();
                recipients.add(neigbours.get(randomIndex));
            }

            for (ENodeB enb : recipients) {
                enb.reserveRBs(iteration, request);
            }
        }
    }
}
