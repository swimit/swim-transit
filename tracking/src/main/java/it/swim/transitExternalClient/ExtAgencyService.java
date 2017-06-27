package it.swim.transitExternalClient;

import it.swim.api.AbstractService;
import it.swim.api.CommandLane;
import it.swim.api.MapLane;
import it.swim.api.SwimLane;
import it.swim.api.ValueLane;
import it.swim.recon.Item;
import it.swim.recon.Record;
import it.swim.recon.Slot;
import it.swim.recon.Value;

public class ExtAgencyService extends AbstractService {

  @SwimLane("agency/vehicles")
  public MapLane<String, Value> vehiclesMap = mapLane().keyClass(String.class).valueClass(Value.class);

  @SwimLane("agency/count")
  public ValueLane<Integer> vehiclesCount = valueLane().valueClass(Integer.class);

  @SwimLane("agency/speed")
  public ValueLane<Float> vehiclesSpeed = valueLane().valueClass(Float.class);

  @SwimLane("agency/input")
  public CommandLane<Value> input = commandLane().valueClass(Value.class).onCommand((Value values) -> add(values));

  private void add(Value values) {
    vehiclesMap.clear();
    int speedSum = 0;
    for (Item recon : values) {
      vehiclesMap.put(recon.get("id").stringValue(), recon.asValue());
      speedSum += Integer.parseInt(recon.get("speed").stringValue());
    }
    vehiclesCount.set(vehiclesMap.size());
    vehiclesSpeed.set(((float)speedSum)/vehiclesCount.get());
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
