package it.swim.transit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import it.swim.transit.model.Agency;
import it.swim.transit.service.AgencyService;
import it.swim.transit.service.CountryService;
import it.swim.transit.service.StateService;
import recon.Form;
import recon.Recon;
import recon.Value;
import swim.api.AbstractPlane;
import swim.api.ServiceType;
import swim.api.SwimRoute;
import swim.server.ServerDef;
import swim.server.SwimPlane;
import swim.server.SwimServer;
import swim.util.Decodee;

public class TransitPlane extends AbstractPlane {

  @SwimRoute("/country/:id")
  final ServiceType<?> transitService = serviceClass(CountryService.class);

  @SwimRoute("/state/:country/:state")
  final ServiceType<?> stateService = serviceClass(StateService.class);

  @SwimRoute("/agency/:country/:state/:id")
  final ServiceType<?> agencyService = serviceClass(AgencyService.class);

  public static void main(String[] args) throws IOException {
    Value configValue = loadReconConfig(args);

    final ServerDef serverDef = ServerDef.FORM.cast(configValue);
    final SwimServer server = new SwimServer();
    server.materialize(serverDef);
    final SwimPlane planeContext = server.getPlane("transit");
    final TransitPlane plane = (TransitPlane) planeContext.getPlane();

    server.start();
    System.out.println("Running TransitPlane ...");
    server.run(); // blocks until termination
    startAgencies(planeContext);
  }

  private static void startAgencies(SwimPlane planeContext) {
    List<Agency> agencies = loadAgencies();
    for (Agency agency : agencies) {
      planeContext.command(agency.getUri(), "addInfo", Form.forClass(Agency.class).mold(agency));
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {

      }
    }
    new NextBusHttpAPI(planeContext).repeatSendVehicleInfo(agencies);
  }

  private static List<Agency> loadAgencies() {
    List<Agency> agencies = new ArrayList<>();
    InputStream is = null;
    Scanner scanner = null;
    try {
      is = FileReader.class.getResourceAsStream("/agencies.csv");
      scanner = new Scanner(is, "UTF-8");
      int index = 0;
      while (scanner.hasNextLine()) {
        final String[] line = scanner.nextLine().split(",");
        if (line.length >= 3) {
          agencies.add(new Agency(line[0], line[1], line[2], index++));
        }
      }
    } catch (Throwable t) {
    } finally {
      try {
        if (is != null) {
          is.close();
        }
      } catch (IOException ignore) {
      }
      if (scanner != null) {
        scanner.close();
      }
    }
    return agencies;
  }

  private static Value loadReconConfig(String[] args) throws IOException {
    String configPath;
    if (args.length > 0) {
      configPath = args[0];
    } else {
      configPath = System.getProperty("swim.config");
      if (configPath == null) {
        configPath = "/transit-space.recon";
      }
    }

    InputStream configInput = null;
    Value configValue;
    try {
      final File configFile = new File(configPath);
      if (configFile.exists()) {
        configInput = new FileInputStream(configFile);
      } else {
        configInput = TransitPlane.class.getResourceAsStream(configPath);
      }
      configValue = Decodee.readUtf8(Recon.FACTORY.blockParser(), configInput);
    } finally {
      try {
        if (configInput != null) {
          configInput.close();
        }
      } catch (Exception ignored) {
      }
    }
    return configValue;
  }
}