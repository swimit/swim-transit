package it.swim.transit;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import it.swim.recon.Attr;
import it.swim.recon.Record;
import it.swim.recon.Slot;
import it.swim.recon.Value;

public class NextBusHttpAPI {

  private static Document parse(URL url) {
    HttpURLConnection urlConnection;
    try {
      urlConnection = (HttpURLConnection) url.openConnection();
      InputStream stream = urlConnection.getInputStream();
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document file = builder.parse(stream);
      file.normalizeDocument();
      return file;
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    } catch (SAXException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static Value[] getVehicleLocations(String ag) {
    try {
      URL url = new URL(String.format(
              "http://webservices.nextbus.com//service/publicXMLFeed?command=vehicleLocations&a=%s&t=0", ag));
      Document file = parse(url);
      if (file == null) {
        return null;
      }
      NodeList nodes = file.getElementsByTagName("vehicle");
      Value[] vehicles = new Value[nodes.getLength()];
      for (int i = 0; i < nodes.getLength(); i++) {
        String id = ((Element) nodes.item(i)).getAttribute("id").replace("\"", "");
        String routeTag = ((Element) nodes.item(i)).getAttribute("routeTag").replace("\"", "");
        String dirId = ((Element) nodes.item(i)).getAttribute("dirTag").replace("\"", "");
        String latitude = ((Element) nodes.item(i)).getAttribute("lat").replace("\"", "");
        String longitude = ((Element) nodes.item(i)).getAttribute("lon").replace("\"", "");
        String speed = ((Element) nodes.item(i)).getAttribute("speedKmHr").replace("\"", "");
        String secsSinceReport = ((Element) nodes.item(i)).getAttribute("secsSinceReport").replace("\"", "");
        vehicles[i] = Record.of(new Attr("vehicle"), new Slot("id", id), new Slot("routeId", routeTag),
                new Slot("dirId", dirId), new Slot("latitude", latitude), new Slot("longitude", longitude),
                new Slot("speed", speed), new Slot("secsSinceReport", secsSinceReport),
                new Slot("agency", ag));
      }
      return vehicles;
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return null;
  }

	/*public Value[] getRoutes() {
        try {
			URL url = new URL(
					String.format("http://webservices.nextbus.com/service/publicXMLFeed?command=routeList&a=sf-muni"));
			Document file = parse(url);
			if (file == null) {
				return null;
			}
			NodeList nodes = file.getElementsByTagName("route");
			Value[] routes = new Value[nodes.getLength()];
			for (int i = 0; i < nodes.getLength(); i++) {
				String tag = ((Element) nodes.item(i)).getAttribute("tag").replace("\"", "");
				String title = ((Element) nodes.item(i)).getAttribute("title").replace("\"", "");
				routes[i] = Record.of(new Attr("route"), new Slot("id", tag), new Slot("title", title));
			}
			return routes;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	private Value[] getRouteConfig(String routeId) {
		try {
			URL url = new URL(String.format(
					"http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=sf-muni&r=%s",
					routeId));
			Document file = parse(url);
			if (file == null) {
				return null;
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}*/
}
