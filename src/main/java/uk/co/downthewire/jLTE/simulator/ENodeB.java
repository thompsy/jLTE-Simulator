package uk.co.downthewire.jLTE.simulator;

import org.apache.commons.configuration.Configuration;
import org.w3c.dom.Node;

import uk.co.downthewire.jLTE.simulator.sectors.AbstractSector;
import uk.co.downthewire.jLTE.simulator.utils.FieldNames;
import uk.co.downthewire.jLTE.simulator.utils.XMLUtils;
import uk.co.downthewire.jLTE.simulator.x2.X2Request;

import java.util.ArrayList;
import java.util.List;

public class ENodeB {

    public final int id;
    public final Location location;

    private List<AbstractSector> sectors;
    private final List<ENodeB> neighbours;
    private int lastX2Sent;
    private final Configuration config;

    public static ENodeB fromXML(Configuration configuration, final Node xml) {
        int id = Integer.parseInt(xml.getAttributes().getNamedItem("id").getNodeValue());
        Location location = XMLUtils.parseLocation(xml);

        List<AbstractSector> sectors = new ArrayList<>(3);
        ENodeB eNodeB = new ENodeB(configuration, id, location, sectors);
        sectors = XMLUtils.parseSectors(configuration, xml, eNodeB, location);
        eNodeB.sectors = sectors;
        return eNodeB;
    }

    public ENodeB(Configuration config, int id, Location location, List<AbstractSector> sectors) {
        this.config = config;
        this.id = id;
        this.location = location;
        this.sectors = sectors;
        this.neighbours = new ArrayList<>(); // A list of neighbouring eNodeBs
        this.lastX2Sent = -10; // Record when the last X2 msg was sent
    }

    @SuppressWarnings("boxing")
    @Override
    public String toString() {
        return String.format("eNB[%d]: location=%s, sectors=%s", id, location, sectors);
    }

    public List<ENodeB> getNeighbours() {
        return neighbours;
    }

    public void addNeighbour(ENodeB eNB2) {
        neighbours.add(eNB2);
    }

    public List<AbstractSector> getSectors() {
        return sectors;
    }

    public void reserveRBs(int iteration, X2Request request) {
        if (iteration - lastX2Sent <= config.getInt(FieldNames.X2_MSG_WAIT_TIME))
            return;

        for (AbstractSector s : sectors) {
            s.reserveRBs(request);
            lastX2Sent = iteration;
        }
    }
}
