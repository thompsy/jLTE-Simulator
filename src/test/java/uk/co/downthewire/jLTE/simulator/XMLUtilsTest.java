package uk.co.downthewire.jLTE.simulator;

import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import uk.co.downthewire.jLTE.simulator.Location;
import uk.co.downthewire.jLTE.simulator.utils.XMLUtils;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class XMLUtilsTest {

    @SuppressWarnings("static-method")
    @Test
    @Ignore
    public void parseValidLocation() throws SAXException, IOException, ParserConfigurationException {
        Location expected = new Location(1.2, 2.1);
        String validLocation = "<location><x>1.2</x><y>2.1</y></location>";

        Element node = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(validLocation.getBytes())).getDocumentElement();

        Location parsedLocation = XMLUtils.parseLocation(node);
        assertEquals(expected.x, parsedLocation.x, 0.001);
    }
}
