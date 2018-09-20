package it.swim.transit;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import recon.Attr;
import recon.Record;
import recon.Slot;
import swim.server.SwimPlane;

public class NextBusHttpAPI {

  private final SwimPlane plane;

  public NextBusHttpAPI(SwimPlane plane) {
    this.plane = plane;
  }

  public void repeatSendVehicleInfo(List<Agency> agencies) {
    int count = 0;
    for (Agency agency : agencies) {
      final int index = count;
      ScheduledExecutorService run = Executors.newSingleThreadScheduledExecutor();
      run.scheduleAtFixedRate(() -> sendVehicleInfo(agency.getUri(), agency, index)
          , 10 + count, 10, TimeUnit.SECONDS);
      count += 1;
    }
  }

  private Document parse(URL url) {
    HttpURLConnection urlConnection;
    try {
      urlConnection = (HttpURLConnection) url.openConnection();
      InputStream stream = urlConnection.getInputStream();
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document file = builder.parse(stream);
      file.normalizeDocument();
      return file;
    } catch (Throwable e) {
      // ignore errors for now
    }
    return null;
  }

  public Record getVehicleLocations(Agency ag, int index) {
    try {
      URL url = new URL(String.format(
          "http://webservices.nextbus.com//service/publicXMLFeed?command=vehicleLocations&a=%s&t=0", ag.getId()));
      Document file = parse(url);
      if (file == null) {
        return null;
      }
      NodeList nodes = file.getElementsByTagName("vehicle");
      Record res = Record.empty();
      for (int i = 0; i < nodes.getLength(); i++) {
        final String id = ((Element) nodes.item(i)).getAttribute("id").replace("\"", "");
        final String routeTag = ((Element) nodes.item(i)).getAttribute("routeTag").replace("\"", "");
        final String dirId = ((Element) nodes.item(i)).getAttribute("dirTag").replace("\"", "");
        final float latitude = Float.parseFloat(((Element) nodes.item(i)).getAttribute("lat").replace("\"", ""));
        final float longitude = Float.parseFloat(((Element) nodes.item(i)).getAttribute("lon").replace("\"", ""));
        final int speed = Integer.parseInt(((Element) nodes.item(i)).getAttribute("speedKmHr").replace("\"", ""));
        final String secsSinceReport = ((Element) nodes.item(i)).getAttribute("secsSinceReport").replace("\"", "");

        res = res.withItem(Record.of(Attr.of("vehicle"), Slot.of("id", id), Slot.of("routeId", routeTag),
            Slot.of("dirId", dirId), Slot.of("latitude", latitude), Slot.of("longitude", longitude),
            Slot.of("speed", speed), Slot.of("secsSinceReport", secsSinceReport),
            Slot.of("agency", ag.getId()), Slot.of("index", index)));
      }
      return res;
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return null;
  }

  private void sendVehicleInfo(String node, Agency ag, int index) {
    Record vehicles = getVehicleLocations(ag, index);
    if (vehicles != null && vehicles.size() > 0) {
      plane.command(node, "input", vehicles);
    }
  }
}
