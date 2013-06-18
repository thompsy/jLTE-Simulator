package uk.co.downthewire.jLTE.simulator.sectors;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SectorParams {

    private int id;
    private double antennaGain;
    private double centralFreq;
    private double txPower;
    private double azimuth;
    private double height;
    private double downtilt;

    public SectorParams(Node xml) {
        final NodeList list = xml.getChildNodes();
        for (int s = 0; s < list.getLength(); s++) {

            final Node n = list.item(s);

            // Parse basic attributes
            if (n.getNodeName().equals("sectorId")) {
                this.id = Integer.parseInt(n.getChildNodes().item(0).getNodeValue());
            }
            if (n.getNodeName().equals("antGain")) {
                this.antennaGain = Double.parseDouble(n.getChildNodes().item(0).getNodeValue());
            }
            if (n.getNodeName().equals("centralFreq")) {
                this.centralFreq = Double.parseDouble(n.getChildNodes().item(0).getNodeValue());
            }

            // Parse the more complex attributes.
            if (n.getNodeName().equals("attribute")) {

                String name = "";
                double value = -99.99;

                for (int i = 0; i < n.getChildNodes().getLength(); i++) {

                    if (n.getChildNodes().item(i).getNodeName().equals("name")) {
                        name = n.getChildNodes().item(i).getChildNodes().item(0).getNodeValue();
                    }
                    if (n.getChildNodes().item(i).getNodeName().equals("value")) {
                        value = Double.parseDouble(n.getChildNodes().item(i).getChildNodes().item(0).getNodeValue());
                    }
                    switch (name) {
                        case "txPower":
                            this.txPower = value;
                            break;
                        case "azimuth":
                            this.azimuth = value;
                            break;
                        case "height":
                            this.height = value;
                            break;
                        case "downtilt":
                            this.downtilt = value;
                            break;
                    }
                }
            }
        }
    }

    public int getSectorId() {
        return id;
    }

    public double getCentralFrequency() {
        return centralFreq;
    }

    public double getTxPower() {
        return txPower;
    }

    public double getAzimuth() {
        return azimuth;
    }

    public double getHeight() {
        return height;
    }

    public double getDowntilt() {
        return downtilt;
    }

    public double getAntennaGain() {
        return antennaGain;
    }
}
