package it.swim.transitExternalClient;


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

public class ExtTransitPlane extends AbstractPlane {

  @SwimRoute("/extTransit/:id")
  final ServiceType<?> extTransitService = serviceClass(ExtTransitService.class);

  @SwimRoute("/extAgency/:id")
  final ServiceType<?> extAgencyService = serviceClass(ExtAgencyService.class);

  public static void main(String[] args) throws IOException {
    Value configValue = loadReconConfig(args);

    final ServerDef serverDef = ServerDef.FORM.cast(configValue);
    final SwimServer server = new SwimServer();
    server.materialize(serverDef);
    final SwimPlane planeContext = server.getPlane("extTransit");
    final ExtTransitPlane plane = (ExtTransitPlane) planeContext.getPlane();

    server.start();
    System.out.println("Running ExtTransitPlane ...");
    server.run(); // blocks until termination
  }

  private static Value loadReconConfig(String[] args) throws IOException {
    String configPath;
    if (args.length > 0) {
      configPath = args[0];
    } else {
      configPath = System.getProperty("swim.config");
      if (configPath == null) {
        configPath = "/transit-space-external.recon";
      }
    }

    InputStream configInput = null;
    Value configValue;
    try {
      final File configFile = new File(configPath);
      if (configFile.exists()) {
        configInput = new FileInputStream(configFile);
      } else {
        configInput = ExtTransitPlane.class.getResourceAsStream(configPath);
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