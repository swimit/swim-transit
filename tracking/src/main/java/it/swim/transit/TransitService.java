package it.swim.transit;

import it.swim.api.*;
import it.swim.recon.Value;
import it.swim.util.Uri;

import java.util.Iterator;

public class TransitService extends AbstractService {

    @SwimLane("counts")
    public ValueLane<Integer> counts = valueLane().valueClass(Integer.class);

    @SwimLane("VehicleCounts")
    public JoinValueLane<Value, Integer> vehicleCounts = joinValueLane().valueClass(Integer.class)
            .didUpdate((Value key, Integer prevCount, Integer newCount) -> updateCounts());

    /*@SwimLane("speed")
    public ValueLane<Float> counts = valueLane().valueClass(Float.class);

    @SwimLane("VehicleSpeed")
    public JoinValueLane<Value, Float> vehicleSpeed = joinValueLane().valueClass(Float.class)
            .didUpdate((Value key, Float prevCount, Float newCount) -> updateSpeeds());*/

    @SwimLane("locations")
    public MapLane<String, Value> locations = mapLane().keyClass(String.class).valueClass(Value.class);

    @SwimLane("VehicleLocations")
    public JoinMapLane<Value, String, Value> vehicleLocations = joinMapLane().keyClass(String.class)
            .valueClass(Value.class).didUpdate(
                    (String key, Value newEntry, Value oldEntry) -> locations.put(key, newEntry));

    @SwimLane("agencies/add")
    public CommandLane<Value> agencyAdd = commandLane().valueClass(Value.class).onCommand((Value value) -> {
        vehicleCounts.downlink(value.get("key")).nodeUri(Uri.parse(value.get("node").stringValue()))
                .laneUri("agency/count").open();
        vehicleLocations.downlink(value.get("key")).nodeUri(Uri.parse(value.get("node").stringValue()))
                .laneUri("agency/vehicles").open();
    });

    public void updateCounts() {
        int vCounts = 0;
        Iterator<Integer> it = vehicleCounts.valueIterator();
        while (it.hasNext()) {
            final Integer next = it.next();
            vCounts += next;
        }
        counts.set(vCounts);
    }



    public void didStart() {
        locations.clear();
        System.out.println("Started Service" + nodeUri());
    }
}