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

import it.swim.transit.model.Agency;
import it.swim.transit.model.Vehicle;
import it.swim.transit.model.Vehicles;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import recon.Form;
import recon.Value;
import swim.server.SwimPlane;

public class NextBusHttpAPI {

  private final SwimPlane plane;

  public NextBusHttpAPI(SwimPlane plane) {
    this.plane = plane;
  }

  public void repeatSendVehicleInfo(List<Agency> agencies) {
    for (Agency agency : agencies) {
      ScheduledExecutorService run = Executors.newSingleThreadScheduledExecutor();
      run.scheduleAtFixedRate(() -> sendVehicleInfo(agency.getUri(), agency)
          , 10 + agency.getIndex(), 10, TimeUnit.SECONDS);
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

  public Vehicles getVehicleLocations(Agency ag) {
    try {
      URL url = new URL(String.format(
          "http://webservices.nextbus.com//service/publicXMLFeed?command=vehicleLocations&a=%s&t=0", ag.getId()));
      Document file = parse(url);
      if (file == null) {
        return null;
      }

      NodeList nodes = file.getElementsByTagName("vehicle");
      final Vehicles vehicles = new Vehicles();
      for (int i = 0; i < nodes.getLength(); i++) {
        final String id = ((Element) nodes.item(i)).getAttribute("id").replace("\"", "");
        final String routeTag = ((Element) nodes.item(i)).getAttribute("routeTag").replace("\"", "");
        final String dirId = ((Element) nodes.item(i)).getAttribute("dirTag").replace("\"", "");
        final float latitude = parseFloat(nodes, i, "lat");
        final float longitude = parseFloat(nodes, i, "lon");
        final int speed = parseInt(nodes, i, "speedKmHr");
        final int secsSinceReport = parseInt(nodes, i, "secsSinceReport");

        final Vehicle vehicle = new Vehicle().withId(id).withDirId(dirId).withIndex(ag.getIndex()).withLatitude(latitude)
            .withLongitude(longitude).withRouteTag(routeTag).withSecsSinceReport(secsSinceReport).withSpeed(speed);
        vehicles.add(vehicle);
      }
      return vehicles;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private int parseInt(NodeList nodes, int i, String property) {
    try {
      return Integer.parseInt(((Element) nodes.item(i)).getAttribute(property).replace("\"", ""));
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private float parseFloat(NodeList nodes, int i, String property) {
    try {
      return Float.parseFloat(((Element) nodes.item(i)).getAttribute(property).replace("\"", ""));
    } catch (NumberFormatException e) {
      return 0.0f;
    }
  }

  private void sendVehicleInfo(String node, Agency ag) {
    final Vehicles vehicles = getVehicleLocations(ag);
    if (vehicles != null && vehicles.getVehicles().size() > 0) {
      final Value value = Form.forClass(Vehicles.class).mold(vehicles);
      plane.command(node, "input", value);
    }
  }
}
