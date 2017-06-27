package it.swim.transitExternalClient;

import it.swim.api.AbstractService;
import it.swim.api.CommandLane;
import it.swim.api.MapLane;
import it.swim.api.SwimLane;
import it.swim.api.ValueLane;
import it.swim.recon.Record;
import it.swim.recon.Slot;
import it.swim.recon.Value;;

public class ExtAgencyService extends AbstractService {

  @SwimLane("agency/vehicles")
  public MapLane<String, Value> vehiclesMap = mapLane().keyClass(String.class).valueClass(Value.class)
          .didUpdate((String key, Value newEntry, Value oldEntry) -> updateInfo(newEntry, oldEntry));

  @SwimLane("agency/count")
  public ValueLane<Integer> vehiclesCount = valueLane().valueClass(Integer.class);

  @SwimLane("agency/speed")
  public ValueLane<Float> vehiclesSpeed = valueLane().valueClass(Float.class);

  @SwimLane("agency/input")
  public CommandLane<Value> input = commandLane().valueClass(Value.class).onCommand((Value value) -> add(value));

  private void add(Value value) {
    String id = value.get("id").stringValue();
    vehiclesMap.put(id, value);
  }

  private  void updateInfo(Value newEntry, Value oldEntry) {
    float newSpeed;
    vehiclesCount.set(vehiclesMap.size());
    if (oldEntry != Value.ABSENT) {
      newSpeed = ((float)(vehiclesCount.get() * vehiclesSpeed.get() + newEntry.get("speed").intValue() -
              oldEntry.get("speed").intValue()))/vehiclesCount.get();
    }
    else {
      newSpeed = ((float)(vehiclesCount.get() * vehiclesSpeed.get() + newEntry.get("speed").intValue()))
              /vehiclesCount.get();
    }
    vehiclesSpeed.set(newSpeed);
  }


  @Override
  public void didStart() {
    vehiclesMap.clear();
    vehiclesSpeed.set((float)0);
    vehiclesCount.set(0);
    System.out.println("Started Service" + nodeUri());
    Value config = Record.of(new Slot("key", this.nodeUri().toUri()), new Slot("node", this.nodeUri().toUri()));
    context.command("/extTransit/bayarea", "agencies/add", config);
  }
}
