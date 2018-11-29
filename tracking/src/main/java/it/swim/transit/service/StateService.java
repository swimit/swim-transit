package it.swim.transit.service;

import java.util.Iterator;

import it.swim.transit.model.Agency;
import it.swim.transit.model.Vehicle;
import recon.Record;
import recon.Value;
import swim.api.AbstractService;
import swim.api.CommandLane;
import swim.api.JoinMapLane;
import swim.api.JoinValueLane;
import swim.api.MapDownlink;
import swim.api.MapLane;
import swim.api.SwimLane;
import swim.api.ValueLane;

public class StateService extends AbstractService {

  @SwimLane("count")
  public ValueLane<Value> count = valueLane();

  @SwimLane("agencyCount")
  public MapLane<Agency, Integer> agencyCount = mapLane().keyClass(Agency.class).valueClass(Integer.class).isTransient(true);

  @SwimLane("joinAgencyCount")
  public JoinValueLane<Agency, Integer> joinAgencyCount = joinValueLane().keyClass(Agency.class).valueClass(Integer.class)
      .didUpdate((Agency key, Integer newCount, Integer prevCount) -> updateCounts(key, newCount));

  @SwimLane("vehicles")
  public MapLane<String, Vehicle> vehicles = mapLane().keyClass(String.class).valueClass(Vehicle.class).isTransient(true);

  @SwimLane("joinAgencyVehicles")
  public JoinMapLane<Agency, String, Vehicle> joinAgencyVehicles = joinMapLane().linkClass(Agency.class).keyClass(String.class)
      .valueClass(Vehicle.class)
      .didUpdate((key, newVehicle, oldVehicle) -> vehicles.put(key, newVehicle))
      .didRemove((key, vehicle) -> vehicles.remove(key));

  @SwimLane("speed")
  public ValueLane<Float> speed = valueLane().valueClass(Float.class);

  @SwimLane("agencySpeed")
  public MapLane<Agency, Float> agencySpeed = mapLane().keyClass(Agency.class).valueClass(Float.class).isTransient(true);

  @SwimLane("joinStateSpeed")
  public JoinValueLane<Agency, Float> joinAgencySpeed = joinValueLane().keyClass(Agency.class).valueClass(Float.class)
      .didUpdate((Agency key, Float newSpeed, Float oldSpeed) -> updateSpeeds(key, newSpeed));

  @SwimLane("addAgency")
  public CommandLane<Agency> agencyAdd = commandLane().valueClass(Agency.class).onCommand((Agency agency) -> {
    joinAgencyCount.downlink(agency).nodeUri(agency.getUri()).laneUri("count").open();
    joinAgencyVehicles.downlink(agency).nodeUri(agency.getUri()).laneUri("vehicles").open();
    joinAgencySpeed.downlink(agency).nodeUri(agency.getUri()).laneUri("speed").open();
    context.command("/country/" + prop("country").stringValue(), "addAgency", agency.toValue().withSlot("stateUri", nodeUri().toUri()));
  });

  public void updateCounts(Agency agency, int newCount) {
    int vCounts = 0;
    Iterator<Integer> it = joinAgencyCount.valueIterator();
    while (it.hasNext()) {
      final Integer next = it.next();
      vCounts += next;
    }

    int maxCount = Integer.max(count.get().get("max").intValue(0), vCounts);
    count.set(Record.EMPTY.withSlot("current", vCounts).withSlot("max", maxCount));
    agencyCount.put(agency, newCount);
    System.out.println(nodeUri().toUri() + " vehicles count: " + vehicles.size());
  }

  public void updateSpeeds(Agency agency, float newSpeed) {
    float vSpeeds = 0.0f;
    Iterator<Float> it = joinAgencySpeed.valueIterator();
    while (it.hasNext()) {
      final Float next = it.next();
      vSpeeds += next;
    }
    if (joinAgencyCount.size() > 0) {
      speed.set(vSpeeds / joinAgencyCount.size());
    }
    agencySpeed.put(agency, newSpeed);
  }

  public void didStart() {
    vehicles.clear();
    agencyCount.clear();
    agencySpeed.clear();
    System.out.println("Started Service" + nodeUri().toUri());
  }

}