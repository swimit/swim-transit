package it.swim.transit.service;

import it.swim.transit.model.Agency;
import it.swim.transit.model.Vehicle;
import it.swim.transit.model.Vehicles;
import recon.Form;
import recon.Item;
import recon.Value;
import swim.api.AbstractService;
import swim.api.CommandLane;
import swim.api.MapLane;
import swim.api.SwimLane;
import swim.api.ValueLane;

import java.util.List;

public class AgencyService extends AbstractService {

  @SwimLane("vehicles")
  public MapLane<String, Vehicle> vehicles = mapLane().keyClass(String.class).valueClass(Vehicle.class);

  @SwimLane("count")
  public ValueLane<Integer> vehiclesCount = valueLane().valueClass(Integer.class);

  @SwimLane("speed")
  public ValueLane<Float> vehiclesSpeed = valueLane().valueClass(Float.class);

  @SwimLane("input")
  public CommandLane<Vehicles> input = commandLane().valueClass(Vehicles.class).onCommand(v -> add(v));

  private void add(Vehicles vehicles) {
    if (vehicles == null ||vehicles.getVehicles().size() == 0) {
      return;
    }
    this.vehicles.clear();
    int speedSum = 0;
    for (Vehicle v : vehicles.getVehicles()) {
      final String vehicleId = prop("id").stringValue() + "_" + v.getId();
      this.vehicles.put(vehicleId, v.withAgency(prop("id").stringValue("")));
      speedSum += v.getSpeed();
    }
    vehiclesCount.set(this.vehicles.size());
    if (vehiclesCount.get() > 0) {
      vehiclesSpeed.set(((float) speedSum) / vehiclesCount.get());
    }
  }

  @SwimLane("addInfo")
  public CommandLane<Agency> addInfo = commandLane().valueClass(Agency.class).onCommand((Agency agency) -> onInfo(agency));

  private void onInfo(Agency agency) {
    Value agencyValue = Form.forClass(Agency.class).mold(agency).withSlot("agencyUri", this.nodeUri().toUri());
    context.command("/state/" + agency.getCountry() + "/" + agency.getState(), "addAgency", agencyValue);
  }

  @Override
  public void didStart() {
    vehicles.clear();
    vehiclesSpeed.set((float) 0);
    vehiclesCount.set(0);
    System.out.println("Started Service: " + nodeUri().toUri());
  }
}