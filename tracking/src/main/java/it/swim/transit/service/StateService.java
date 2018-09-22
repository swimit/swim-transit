package it.swim.transit.service;

import java.util.Iterator;

import it.swim.transit.model.Vehicle;
import recon.Record;
import recon.Value;
import swim.api.AbstractService;
import swim.api.CommandLane;
import swim.api.JoinMapLane;
import swim.api.JoinValueLane;
import swim.api.MapLane;
import swim.api.SwimLane;
import swim.api.ValueLane;
import swim.util.Uri;

public class StateService extends AbstractService {

  @SwimLane("count")
  public ValueLane<Value> count = valueLane();

  @SwimLane("agencyCount")
  public MapLane<String, Integer> agencyCount = mapLane().keyClass(String.class).valueClass(Integer.class);

  @SwimLane("joinAgencyCount")
  public JoinValueLane<Value, Integer> joinAgencyCount = joinValueLane().valueClass(Integer.class)
      .didUpdate((Value key, Integer newCount, Integer prevCount) -> updateCounts(key.stringValue(), newCount));

  @SwimLane("vehicles")
  public MapLane<String, Vehicle> vehicles = mapLane().keyClass(String.class).valueClass(Vehicle.class);

  @SwimLane("joinAgencyVehicles")
  public JoinMapLane<Value, String, Vehicle> joinAgencyVehicles = joinMapLane().keyClass(String.class)
      .valueClass(Vehicle.class).didUpdate(
          (String key, Vehicle newEntry, Vehicle oldEntry) -> vehicles.put(key, newEntry));

  @SwimLane("speed")
  public ValueLane<Float> speed = valueLane().valueClass(Float.class);

  @SwimLane("agencySpeed")
  public MapLane<String, Float> agencySpeed = mapLane().keyClass(String.class).valueClass(Float.class);

  @SwimLane("joinStateSpeed")
  public JoinValueLane<Value, Float> joinAgencySpeed = joinValueLane().valueClass(Float.class)
      .didUpdate((Value key, Float newSpeed, Float oldSpeed) -> updateSpeeds(key.stringValue(), newSpeed));

  @SwimLane("addAgency")
  public CommandLane<Value> agencyAdd = commandLane().onCommand((Value value) -> {
    joinAgencyCount.downlink(value.get("id")).nodeUri(Uri.parse(value.get("agencyUri").stringValue()))
        .laneUri("count").open();
    joinAgencyVehicles.downlink(value.get("id")).nodeUri(Uri.parse(value.get("agencyUri").stringValue()))
        .laneUri("vehicles").open();
    joinAgencySpeed.downlink(value.get("id")).nodeUri(Uri.parse(value.get("agencyUri").stringValue()))
        .laneUri("speed").open();
    context.command("/country/" + prop("country").stringValue(), "addAgency", value.withSlot("stateUri", nodeUri().toUri()));
  });

  public void updateCounts(String agency, int newCount) {
    int vCounts = 0;
    Iterator<Integer> it = joinAgencyCount.valueIterator();
    while (it.hasNext()) {
      final Integer next = it.next();
      vCounts += next;
    }

    int maxCount = Integer.max(count.get().get("max").intValue(0), vCounts);
    count.set(Record.EMPTY.withSlot("current", vCounts).withSlot("max", maxCount));
    agencyCount.put(agency, newCount);

  }

  public void updateSpeeds(String agency, float newSpeed) {
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