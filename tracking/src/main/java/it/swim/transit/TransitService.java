package it.swim.transit;

import java.util.Iterator;

import it.swim.api.AbstractService;
import it.swim.api.CommandLane;
import it.swim.api.JoinValueLane;
import it.swim.api.SwimLane;
import it.swim.api.ValueLane;
import it.swim.recon.Value;
import it.swim.util.Uri;

public class TransitService extends AbstractService {

	@SwimLane("counts")
	public ValueLane<Integer> counts = valueLane().valueClass(Integer.class);

	@SwimLane("VehicleCounts")
	public JoinValueLane<Value, Integer> vehicleCounts = joinValueLane().valueClass(Integer.class)
			.didUpdate((Value key, Integer prevCount, Integer newCount) -> updateCounts());

	@SwimLane("agencies/add")
	public CommandLane<Value> agencyAdd = commandLane().valueClass(Value.class).onCommand(value -> {
		vehicleCounts.downlink(value.get("key")).nodeUri(Uri.parse(value.get("node").stringValue()))
				.laneUri("agency/info").open();
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
		System.out.println("Started Service" + nodeUri());
	}
}