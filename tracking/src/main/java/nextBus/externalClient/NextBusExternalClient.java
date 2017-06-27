package nextBus.externalClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.api.Session;

public class NextBusExternalClient extends WebSocketAdapter {

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
    } catch (IOException | ParserConfigurationException e) {
      e.printStackTrace();
    } catch (SAXException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static String getVehicleLocations(String ag) {
    try {
      URL url = new URL(String.format(
              "http://webservices.nextbus.com//service/publicXMLFeed?command=vehicleLocations&a=%s&t=0", ag));
      Document file = parse(url);
      if (file == null) {
        return null;
      }
      NodeList nodes = file.getElementsByTagName("vehicle");
      String vehicles = "{";
      for (int i = 0; i < nodes.getLength(); i++) {
        String id = ((Element) nodes.item(i)).getAttribute("id").replace("\"", "");
        String routeTag = ((Element) nodes.item(i)).getAttribute("routeTag").replace("\"", "");
        String dirId = ((Element) nodes.item(i)).getAttribute("dirTag").replace("\"", "");
        String latitude = ((Element) nodes.item(i)).getAttribute("lat").replace("\"", "");
        String longitude = ((Element) nodes.item(i)).getAttribute("lon").replace("\"", "");
        String speed = ((Element) nodes.item(i)).getAttribute("speedKmHr").replace("\"", "");
        String secsSinceReport = ((Element) nodes.item(i)).getAttribute("secsSinceReport").replace("\"", "");
        vehicles += String.format("@Vehicle{id:\"%s\",routeId:\"%s\",dirId:\"%s\",latitude:\"%s\",longitude:\"%s\","
                        + "speed:\"%s\",secsSinceReport:\"%s\", agency:\"%s\"},", id, routeTag, dirId, latitude,
                longitude, speed, secsSinceReport, ag);
      }
      return vehicles;
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static void sendVehicleInfo(Session session, String node, String ag) {
    String vehicles = getVehicleLocations(ag);
    String message = String.format("@command(node:\"%s\",lane:\"agency/input\")" +
            vehicles.substring(0,vehicles.length() - 1) + '}', node);
    try {
      session.getRemote().sendString(message);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void repeatSendVehicleInfo(Session session) {
    ScheduledExecutorService run = Executors.newSingleThreadScheduledExecutor();
    run.scheduleAtFixedRate(() -> {
              sendVehicleInfo(session, "/extAgency/sf-muni", "sf-muni");
              sendVehicleInfo(session, "/extAgency/actransit", "actransit");
            }
            , 0, 10, TimeUnit.SECONDS);
  }

  public static void main(String[] args) throws IOException {
    WebSocketClient client = new WebSocketClient();
    try {
      client.start();
      client.setAsyncWriteTimeout(-1000);
      NextBusExternalClient socket = new NextBusExternalClient();
      URI host = new URI("ws://localhost:9090");
      Future<Session> future = client.connect(socket, host);
      Session session = future.get();
      repeatSendVehicleInfo(session);
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

}
