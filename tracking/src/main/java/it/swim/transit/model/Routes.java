package it.swim.transit.model;

import java.util.ArrayList;
import java.util.List;
import recon.ReconName;

@ReconName("Routes")
public class Routes {

  private final List<Route> routes = new ArrayList<Route>();

  public Routes() {
  }

  public List<Route> getRoutes() {
    return routes;
  }

  public void add(Route route) {
    routes.add(route);
  }

}
