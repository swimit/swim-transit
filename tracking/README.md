# Tracking App
Tracking app that tracks the buses in the sf-muni and ucsf agencies

## Getting Started
Compile and run the Tracking App using [Gradle](https://gradle.org/install)
```sh
Gradle Clean Build Run
```
Now open up `sf-muni.html` in a browser to track the buses in the sf-muni agency.
Open `ucsf.html` as well in a browser to track the buses in the ucsf agency.

## Code Walkthrough

### AgencyService.java
```java
@SwimLane("agency/vehicles")
	public MapLane<String, Value> vehiclesMap = mapLane().keyClass(String.class).valueClass(Value.class);
```
Creates a `MapLane`, which we will use to store the vehicles. The key will be a String that 
represents the id of the vehicle, while the value contains information about the given vehicle
stored using [`recon`](https://github.com/swimit/recon-java).

```java
@SwimLane("agency/info")
	public ValueLane<Integer> vehiclesInfo = valueLane().valueClass(Integer.class);
```
Creates a `ValueLane`, which we will use to store the number of vehicles in the agency as an Integer. 

```java
@SwimLane("agency/set")
	public CommandLane<String> agencySet = commandLane().valueClass(String.class).onCommand(info -> agency = info);
```
Creates a `CommandLane`, which when a command is sent to the Uri `agency/set` sets the agency to the String that is sent. 

```java
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
```
Here, we define two methods that allow us to get the locations of the vehicles for this agency every 10 seconds
using the NextBus API and then update the `MapLane` accordingly by first clearing everything present using `clear` and
adding all the vehicles returned by `getVehicleLocations` using `put`.

```java
@Override
	public void didStart() {
		vehiclesMap.clear();
		System.out.println("Started Service" + nodeUri());
		scheduleCheckVehicleLocations();
		Value config = Record.of(new Slot("key", this.nodeUri().toUri()), new Slot("node", this.nodeUri().toUri()));
		context.command("/transit/norcal", "agencies/add", config);
	}
```
The `didStart` callback above runs when the AgencyService first starts and lets the TransitService at node
`/transit/norcal` know that it exists and that it should have its `JoinValueLane` downlink to this AgencyService's
value lane. We will explain this further when looking at `TransitService.java`.

### TransitService.java
```java
@SwimLane("VehicleCounts")
    public JoinValueLane<Value, Integer> vehicleCounts = joinValueLane().valueClass(Integer.class)
            .didUpdate((Value key, Integer prevCount, Integer newCount) -> updateCounts());
```
Creates a `JoinValueLane`, which maintains links to the `agency/info` lane of agencies and when that lane updates
`updateCounts` is called. We will explain `updateCounts` further below.

```java
@SwimLane("agencies/add")
    public CommandLane<Value> agencyAdd = commandLane().valueClass(Value.class).onCommand(value -> {
        vehicleCounts.downlink(value.get("key")).nodeUri(Uri.parse(value.get("node").stringValue()))
                .laneUri("agency/info").open();
    });
```
Creates the `CommandLane` that we were talking about earlier that when it receives a command gets the 'key' and the
'node' from the body of the command and then has `vehicleCounts` downlink to the `agency/info` lane of the AgencyService
at the node.

```java
@SwimLane("counts")
    public ValueLane<Integer> counts = valueLane().valueClass(Integer.class);
```
Creates a `ValueLane`, which we will use to store the aggregate counts of the number of vehicles.
