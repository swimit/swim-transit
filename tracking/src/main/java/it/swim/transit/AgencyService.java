package it.swim.transit;

import it.swim.api.AbstractService;
import it.swim.api.CommandLane;
import it.swim.api.MapLane;
import it.swim.api.SwimLane;
import it.swim.api.ValueLane;
import it.swim.recon.Record;
import it.swim.recon.Slot;
import it.swim.recon.Value;;

public class AgencyService extends AbstractService {

	private String agency = "";

	@SwimLane("agency/vehicles")
	public MapLane<String, Value> vehiclesMap = mapLane().keyClass(String.class).valueClass(Value.class);

	@SwimLane("agency/info")
	public ValueLane<Integer> vehiclesInfo = valueLane().valueClass(Integer.class);

	@SwimLane("agency/set")
	public CommandLane<String> agencySet = commandLane().valueClass(String.class).onCommand((String info) -> agency = info);

	private void checkVehicleLocations() {
		Value[] vehicles = NextBusHttpAPI.getVehicleLocations(agency);
		vehiclesMap.clear();
		for (int i = 0; i < vehicles.length; i++) {
			vehiclesMap.put(vehicles[i].get("id").stringValue(), vehicles[i]);
		}
		vehiclesInfo.set(vehiclesMap.size());
		scheduleCheckVehicleLocations();
	}

	private void scheduleCheckVehicleLocations() {
		setTimer(10000, () -> checkVehicleLocations());
	}

	@Override
	public void didStart() {
		vehiclesMap.clear();
		System.out.println("Started Service" + nodeUri());
		scheduleCheckVehicleLocations();
		Value config = Record.of(new Slot("key", this.nodeUri().toUri()), new Slot("node", this.nodeUri().toUri()));
		context.command("/transit/norcal", "agencies/add", config);
	}
}
