package it.swim.transit;

import recon.Form;
import recon.Item;
import recon.Value;
import swim.api.AbstractService;
import swim.api.CommandLane;
import swim.api.MapLane;
import swim.api.SwimLane;
import swim.api.ValueLane;

public class AgencyService extends AbstractService {

  @SwimLane("vehicles")
  public MapLane<String, Value> vehicles = mapLane().keyClass(String.class);

  @SwimLane("count")
  public ValueLane<Integer> vehiclesCount = valueLane().valueClass(Integer.class);

  @SwimLane("speed")
  public ValueLane<Float> vehiclesSpeed = valueLane().valueClass(Float.class);

  @SwimLane("input")
  public CommandLane<Value> input = commandLane().onCommand((Value values) -> add(values));

  private void add(Value value) {
    if (value.isAbsent() || value.length() == 0) {
      //System.out.println(nodeUri().toUri() + ": Add value " + value.toRecon());
      return;
    }
    vehicles.clear();
    int speedSum = 0;
    for (Item recon : value) {
      final String vehicleId = prop("id").stringValue() + "_" + recon.get("id").stringValue();
      vehicles.put(vehicleId, recon.asValue());
      speedSum += Integer.parseInt(recon.get("speed").stringValue());
    }
    vehiclesCount.set(vehicles.size());
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