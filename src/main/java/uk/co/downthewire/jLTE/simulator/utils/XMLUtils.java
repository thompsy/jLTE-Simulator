package uk.co.downthewire.jLTE.simulator.utils;

import org.apache.commons.configuration.Configuration;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.co.downthewire.jLTE.simulator.ENodeB;
import uk.co.downthewire.jLTE.simulator.Location;
import uk.co.downthewire.jLTE.simulator.sectors.*;

import java.util.ArrayList;
import java.util.List;

import static uk.co.downthewire.jLTE.simulator.utils.FieldNames.ADAPTIVE_SFR;
import static uk.co.downthewire.jLTE.simulator.utils.FieldNames.ALGORITHM;

public final class XMLUtils {

    public static List<AbstractSector> parseSectors(Configuration config, Node xml, ENodeB eNodeB, Location location) {
        List<AbstractSector> sectors = new ArrayList<>();

        final NodeList list = xml.getChildNodes();
        for (int s = 0; s < list.getLength(); s++) {
            final Node n = list.item(s);

            // Parse the sectors.
            if (n.getNodeName().equals("sectors")) {
                final NodeList l = n.getChildNodes();
                for (int i = 0; i < l.getLength(); i++) {
                    final Node sNode = l.item(i);
                    if (sNode.getNodeName().equals("sectorInfo")) {
                        AbstractSector sector;
                        final String algorithm = config.getString(ALGORITHM);

                        switch (algorithm) {
                            case FieldNames.RANDOM_ALGO:
                                sector = RandomSector.fromXML(config, sNode, eNodeB, location);
                                break;
                            case FieldNames.MAXCI_ALGO:
                                sector = MaxCISector.fromXML(config, sNode, eNodeB, location);
                                break;
                            case FieldNames.PROPORTIONATE_FAIR_ALGO:
                                sector = ProportionateFairSector.fromXML(config, sNode, eNodeB, location);
                                break;
                            case FieldNames.SFR_ALGO:
                                sector = SFRSector.fromXML(config, sNode, eNodeB, location);
                                break;
                            case FieldNames.SERFR_ALGO:
                                sector = SerFRSector.fromXML(config, sNode, eNodeB, location);
                                break;
                            case ADAPTIVE_SFR:
                                sector = AdaptiveSFRSector.fromXML(config, sNode, eNodeB, location);
                                break;
                            case FieldNames.DISTRIBUTED_SFR:
                                sector = DistributedSFRSector.fromXML(config, sNode, eNodeB, location);
                                break;
                            default:
                                throw new IllegalArgumentException("Unknown algorithm, cannot choose sector.");
                        }

                        sectors.add(sector);
                    }
                }
            }
        }
        return sectors;
    }

    public static Location parseLocation(Node xml) {
        final NodeList list = xml.getChildNodes();
        for (int s = 0; s < list.getLength(); s++) {

            final Node n = list.item(s);

            // Parse the location
            if (n.getNodeName().equals("location")) {
                final NodeList l = n.getChildNodes();
                double x = -99.99;
                double y = -99.99;
                for (int i = 0; i < l.getLength(); i++) {
                    final Node locNode = l.item(i);
                    if (locNode.getNodeName().equals("x")) {
                        x = Double.parseDouble(locNode.getChildNodes().item(0).getNodeValue());
                    }
                    if (locNode.getNodeName().equals("y")) {
                        y = Double.parseDouble(locNode.getChildNodes().item(0).getNodeValue());
                    }
                }
                return new Location(x, y);
            }
        }
        return null;
    }

    private XMLUtils() {
    }
}
