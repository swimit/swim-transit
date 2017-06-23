package it.swim.transit;


import it.swim.api.AbstractPlane;
import it.swim.api.ServiceType;
import it.swim.api.SwimRoute;
import it.swim.recon.Recon;
import it.swim.recon.Value;
import it.swim.server.ServerDef;
import it.swim.server.SwimPlane;
import it.swim.server.SwimServer;
import it.swim.util.Decodee;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TransitPlane extends AbstractPlane {

  @SwimRoute("/transit/:id")
  final ServiceType<?> transitService = serviceClass(TransitService.class);

  @SwimRoute("/agency/:id")
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

    planeContext.command("/agency/sf-muni", "agency/set", Value.of("sf-muni"));
    planeContext.command("/agency/actransit", "agency/set", Value.of("actransit"));
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
        if (configInput != null)
          configInput.close();
      } catch (Exception ignored) {
      }
    }
    return configValue;
  }
}