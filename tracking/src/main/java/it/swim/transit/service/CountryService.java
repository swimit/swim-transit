package it.swim.transit.service;

import java.util.Iterator;

import it.swim.transit.model.Agency;
import it.swim.transit.model.Vehicle;
import recon.Form;
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

public class CountryService extends AbstractService {

  @SwimLane("count")
  public ValueLane<Value> count = valueLane();

  @SwimLane("states")
  public MapLane<String, String> states = mapLane().keyClass(String.class).valueClass(String.class);

  @SwimLane("stateCount")
  public MapLane<String, Integer> stateCount = mapLane().keyClass(String.class).valueClass(Integer.class);

  @SwimLane("joinStateCount")
  public JoinValueLane<Value, Value> joinStateCount = joinValueLane()
      .didUpdate((Value key, Value newCount, Value prevCount) -> updateCounts(key.stringValue(), newCount));

  @SwimLane("vehicles")
  public MapLane<String, Vehicle> vehicles = mapLane().keyClass(String.class).valueClass(Vehicle.class);

  @SwimLane("joinAgencyVehicles")
  public JoinMapLane<Value, String, Vehicle> joinAgencyVehicles = joinMapLane().keyClass(String.class)
      .valueClass(Vehicle.class).didUpdate(
          (String key, Vehicle newEntry, Vehicle oldEntry) -> vehicles.put(key, newEntry));

  @SwimLane("speed")
  public ValueLane<Float> speed = valueLane().valueClass(Float.class);

  @SwimLane("stateSpeed")
  public MapLane<String, Float> stateSpeed = mapLane().keyClass(String.class).valueClass(Float.class);

  @SwimLane("joinStateSpeed")
  public JoinValueLane<Value, Float> joinStateSpeed = joinValueLane().valueClass(Float.class)
      .didUpdate((Value key, Float newSpeed, Float oldSpeed) -> updateSpeeds(key.stringValue(), newSpeed));


  @SwimLane("addAgency")
  public CommandLane<Value> agencyAdd = commandLane().valueClass(Value.class).onCommand((Value value) -> {
    states.put(value.get("state").stringValue(), value.get("state").stringValue());
    joinStateCount.downlink(value.get("state")).nodeUri(Uri.parse(value.get("stateUri").stringValue()))
        .laneUri("count").open();
    joinStateSpeed.downlink(value.get("state")).nodeUri(Uri.parse(value.get("stateUri").stringValue()))
        .laneUri("speed").open();
    final Agency agency = (Agency) Form.forClass(Agency.class).cast(value);
    joinAgencyVehicles.downlink(value.get("id")).nodeUri(agency.getUri()).laneUri("vehicles").open();

  });

  public void updateCounts(String state, Value newCount) {
    stateCount.put(state, newCount.get("current").intValue(0));
    int vCounts = 0;
    Iterator<Integer> it = stateCount.valueIterator();
    while (it.hasNext()) {
      final Integer next = it.next();
      vCounts += next;
    }

    int maxCount = Integer.max(count.get().get("max").intValue(0), vCounts);
    count.set(Record.EMPTY.withSlot("current", vCounts).withSlot("max", maxCount));

  }

  public void updateSpeeds(String state, float newSpeed) {
    float vSpeeds = 0.0f;
    Iterator<Float> it = joinStateSpeed.valueIterator();
    while (it.hasNext()) {
      final Float next = it.next();
      vSpeeds += next;
    }
    if (states.size() > 0) {
      speed.set(vSpeeds / states.size());
    }
    stateSpeed.put(state, newSpeed);
  }

  public void didStart() {
    vehicles.clear();
    System.out.println("Started Service" + nodeUri().toUri());
  }
}