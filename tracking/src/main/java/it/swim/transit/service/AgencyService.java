package it.swim.transit.service;

import it.swim.transit.model.Agency;
import it.swim.transit.model.Route;
import it.swim.transit.model.Routes;
import it.swim.transit.model.Vehicle;
import it.swim.transit.model.Vehicles;
import recon.Value;
import swim.api.AbstractService;
import swim.api.CommandLane;
import swim.api.MapLane;
import swim.api.SwimLane;
import swim.api.ValueLane;
import swim.util.Uri;

public class AgencyService extends AbstractService {

  @SwimLane("vehicles")
  public MapLane<String, Vehicle> vehicles = mapLane().keyClass(String.class).valueClass(Vehicle.class).isTransient(true);

  @SwimLane("count")
  public ValueLane<Integer> vehiclesCount = valueLane().valueClass(Integer.class);

  @SwimLane("speed")
  public ValueLane<Float> vehiclesSpeed = valueLane().valueClass(Float.class);

  @SwimLane("addVehicles")
  public CommandLane<Vehicles> addVehicles = commandLane().valueClass(Vehicles.class).onCommand(v -> onVehicles(v));

  private void onVehicles(Vehicles vehicles) {
    if (vehicles == null ||vehicles.getVehicles().size() == 0) {
      return;
    }
    this.vehicles.clear();
    int speedSum = 0;
    for (Vehicle v : vehicles.getVehicles()) {
      final String vehicleUri = vehicleUri(v.getId());
      if (vehicleUri != null) {
        context.command(vehicleUri(v.getId()), "addVehicle", v.toValue());
        addVehicle(vehicleUri, v);
        speedSum += v.getSpeed();
      }
    }
    vehiclesCount.set(this.vehicles.size());
    if (vehiclesCount.get() > 0) {
      vehiclesSpeed.set(((float) speedSum) / vehiclesCount.get());
    }
  }

  private void addVehicle(String vehicleUri, Vehicle v) {
    final Route r = routes.get(v.getRouteTag());
    if (r != null) {
      this.vehicles.put(vehicleUri, v.withAgency(prop("id").stringValue("")).withRouteTitle(r.getTitle()));
    }
  }

  private String vehicleUri(String id) {
    final Agency agency = info.get();
    if (agency != null) {
      return "/vehicle/" + agency.getCountry() + "/" + agency.getState() + "/" + agency.getId() + "/" + id;
    } else {
      return null;
    }
  }

  @SwimLane("addInfo")
  public CommandLane<Agency> addInfo = commandLane().valueClass(Agency.class).onCommand((Agency agency) -> onInfo(agency));

  @SwimLane("info")
  public ValueLane<Agency> info = valueLane().valueClass(Agency.class);

  private void onInfo(Agency agency) {
    Value agencyValue = agency.toValue().withSlot("agencyUri", this.nodeUri().toUri());
    context.command("/state/" + agency.getCountry() + "/" + agency.getState(), "addAgency", agencyValue);
    info.set(agency);
  }

  @SwimLane("addRoutes")
  public CommandLane<Routes> addRoutes = commandLane().valueClass(Routes.class).onCommand(r -> onRoutes(r));

  @SwimLane("routes")
  public MapLane<String, Route> routes = mapLane().keyClass(String.class).valueClass(Route.class);

  private void onRoutes(Routes r) {
    for (Route route : r.getRoutes()) {
      routes.put(route.getTag(), route);
    }
  }

  @Override
  public void didStart() {
    vehicles.clear();
    vehiclesSpeed.set((float) 0);
    vehiclesCount.set(0);
    System.out.println("Started Service: " + nodeUri().toUri());
  }
}