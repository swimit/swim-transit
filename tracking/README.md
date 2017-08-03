# Tracking App
Tracking app that tracks the buses in the sf-muni and actransit agencies

## Overview
We set out to develop an application that can provide real time tracking of buses in San Francisco and the East Bay.
The goal was to have two different html pages that can display the buses in each of the agencies along with some
statistics and to also have a third html page that displays the buses from both agencies and aggregates the statistics.
The general outline of how we were going to approach developing this application was to write an `AgencyService`
for the two agencies and to also write a higher level swim service, which we called the `TransitService`, that
linked to both `AgencyServices`. The bus data was obtained from the [NextBus API](http://webservices.nextbus.com/).

## Getting Started
If you do not have Gradle installed you can find an installation guide [here](https://gradle.org/install).
Compile and run the basic Tracking App using Gradle
```
% gradle -PmainClassName='it.swim.transit.TransitPlane' runMain
```
Now open up `sf-muni.html` in a browser to track the buses in the sf-muni agency.
Open `actransit.html` as well in a browser to track the buses in the transit agency.
Open up `region.html` in order to see the aggregation of the two agencies.

In order to run the Tracking App with the external pulling client. First run the `ExtTransitPlane`
```sh
% gradle -PmainClassName='it.swim.transitExternalClient.ExtTransitPlane' runMain
```
Then, run the external pulling client
```sh
% gradle -PmainClassName='nextBus.externalClient.NextBusExternalClient' runMain
```
Now open up `sf-muni-external.html` in a browser to track the buses in the sf-muni agency.
Open `actransit-external.html` as well in a browser to track the buses in the transit agency.
Open up `region-external.html` in order to see the aggregation of the two agencies.
## Code Walkthrough

### AgencyService.java

In the `AgencyService`, the goal is to store the information from all the vehicles and keep track of some statistics
such as the average speed of the vehicles in the agency and the total number of vehicles in the agency.

```java
@SwimLane("agency/vehicles")
public MapLane<String, Value> vehiclesMap = mapLane().keyClass(String.class).valueClass(Value.class);
```
Creates a `MapLane` `vehiclesMap`, which we will use to store the vehicles. The key will be a String that
represents the id of the vehicle, while the value contains information about the given vehicle
stored using [`recon`](https://github.com/swimit/recon-java). We also assign the `MapLane` to the
relative Uri `agency/vehicles`.

```java
@SwimLane("agency/count")
public ValueLane<Integer> vehiclesCount = valueLane().valueClass(Integer.class);
```
Creates a `ValueLane` `vehiclesCount`, which we will use to store the number of vehicles in the agency as an Integer.
We then assign the `ValueLane` to the relative Uri `agency/vehicles`.

```java
@SwimLane("agency/speed")
public ValueLane<Float> vehiclesSpeed = valueLane().valueClass(Float.class);
```
Creates a `ValueLane` `vehiclesSpeed`, which we will use to store the average speed of vehicles in the agency as a
Float.

```java
@SwimLane("agency/set")
public CommandLane<String> agencySet = commandLane().valueClass(String.class).onCommand(info -> agency = info);
```
Creates a `CommandLane`, which when a command is sent to the Uri `agency/set` sets the agency to the String that is
sent. This allows us to determine which agency a particular `AgencyService` will be looking at.

```java
private void checkVehicleLocations() {
    Value[] vehicles = NextBusHttpAPI.getVehicleLocations(agency);
    vehiclesMap.clear();
    int speedSum = 0;
    for (int i = 0; i < vehicles.length; i++) {
        vehiclesMap.put(vehicles[i].get("id").stringValue(), vehicles[i]);
        speedSum += Integer.parseInt(vehicles[i].get("speed").stringValue());
    }
    vehiclesCount.set(vehiclesMap.size());
    vehiclesSpeed.set(((float) speedSum) / vehiclesMap.size());
    scheduleCheckVehicleLocations();
    }

private void scheduleCheckVehicleLocations() {
	setTimer(10000, () -> checkVehicleLocations());
}

```
Here, we define two methods that allow us to get the locations of the vehicles for this agency every 10 seconds
using the NextBus API and then update the `MapLane` accordingly by first clearing everything present using `clear` and
adding all the vehicles returned by `getVehicleLocations` using `put`. We also update the total number of vehicles and
the average speed of the vehicles by using `set`. This method makes use of `Timers` in Swim. `setTimer` only sets a
timer once, so in order to make the method `checkVehicleLocations` run every 10 seconds we call
`scheduleCheckVehicleLocations` at the end of it.

```java
@Override
public void didStart() {
    System.out.println("Started Service" + nodeUri());
    scheduleCheckVehicleLocations();
    Value config = Record.of(new Slot("key", this.nodeUri().toUri()), new Slot("node", this.nodeUri().toUri()));
    context.command("/transit/bayarea", "agencies/add", config);
}
```
The `didStart` callback above runs when the `AgencyService` first starts. This method calls
`scheduleCheckVehicleLocations` to start populating the lanes and lets the `TransitService` at node `/transit/bayarea`
know that it exists and that it should have its `JoinValueLane` downlink to this `AgencyService's` `ValueLane`.
Furthermore, this creates the `TransitService` if it has not already been instantiated. We will explain this command
further when looking at `TransitService.java`.

### TransitService.java

In the `TransitService`, the goal is to be able connect to a certain set of `AgencyServices`, display the vehicles
from all the different agencies on one map, and also keep track of some of statistics like the number of vehicles
across all the agencies.

```java
@SwimLane("counts")
public ValueLane<Integer> counts = valueLane().valueClass(Integer.class);

@SwimLane("locations")
public MapLane<String, Value> locations = mapLane().keyClass(String.class).valueClass(Value.class);
```
Here we create a `ValueLane` `counts`, in which we will store the aggregation of the counts from the agencies. We also
create a `MapLane` `locations`, in which we will store the vehicles from all the agencies. We assign them the
relative Uris of `counts` and `locations` respectively.

```java
@SwimLane("VehicleCounts")
public JoinValueLane<Value, Integer> vehicleCounts = joinValueLane().valueClass(Integer.class)
            .didUpdate((Value key, Integer prevCount, Integer newCount) -> updateCounts());
```
Creates a `JoinValueLane`, which we will use to maintain links to the `agency/count` lanes of agencies and when those
lanes update `updateCounts` will be called. We will explain `updateCounts` further below.

```java
@SwimLane("VehicleLocations")
public JoinMapLane<Value, String, Value> vehicleLocations = joinMapLane().keyClass(String.class)
          .valueClass(Value.class).didUpdate(
                  (String key, Value newEntry, Value oldEntry) -> locations.put(key, newEntry));
```
Creates a `JoinMapLane`, which we will use to maintain links to the `agency/vehicles` lanes of agencies and
when those lanes update `locations` will update as well using `put`. If the `key` is already associated with a value
that value will be replaced by `newEntry`; otherwise, there will be a new key value pair.

```java
@SwimLane("agencies/add")
public CommandLane<Value> agencyAdd = commandLane().valueClass(Value.class).onCommand((Value value) -> {
    vehicleCounts.downlink(value.get("key")).nodeUri(Uri.parse(value.get("node").stringValue()))
            .laneUri("agency/count").open();
    vehicleLocations.downlink(value.get("key")).nodeUri(Uri.parse(value.get("node").stringValue()))
            .laneUri("agency/vehicles").open();
});
```
Creates the `CommandLane` that we were talking about earlier that on command gets the `key` and the
`node` from the body of the command and then has `vehicleCounts` downlink to the `agency/count` lane of the
`AgencyService` at the node and `vehicleLocations` downlink to the `agency/vehicles` lane of the `AgencyService`.
The command that is received will have a `recon` body so in order to get the `key` as a `Value` we use `get`. Likewise,
we use `get` to get the `node` as a `Value` as well. We then convert it to a Uri by first converting the Value to a
String.

```java
public void updateCounts() {
  int vCounts = 0;
  Iterator<Integer> it = vehicleCounts.valueIterator();
  while (it.hasNext()) {
    final Integer next = it.next();
    vCounts += next;
  }
  counts.set(vCounts);
}
```
`updateCounts` is called when `vehicleCounts` receives an update to the counts of one of the agencies it is linked to.
`updateCounts` just recalculates the sum of all the `vehicleCounts` when this occurs and sets `count` to that value.

### TransitPlane.java
`TransitPlane.java` contains the `main` method that will create everything needed for the application and actually run
 it.

 ```java
@SwimRoute("/transit/:id")
final ServiceType<?> transitService = serviceClass(TransitService.class);

@SwimRoute("/agency/:id")
final ServiceType<?> agencyService = serviceClass(AgencyService.class);
 ```
In `TransitPlane.java`, we define the services that will be used. The `TransitServices` will have Uris, which start
with `/transit/`, while the `AgencyServices` will have Uris, which start with `/agency/`.

```java
public static void main(String[] args) throws IOException {
    Value configValue = loadReconConfig(args);

    final ServerDef serverDef = ServerDef.FORM.cast(configValue);
    final SwimServer server = new SwimServer();
    server.materialize(serverDef);
    final SwimPlane planeContext = server.getPlane("transit");
    final TransitPlane plane = (TransitPlane) planeContext.getPlane();

    server.start();
    System.out.println("Running TransitPlane ...");
    server.run(); // blocks until termination

    planeContext.command("/agency/sf-muni", "agency/set", Value.of("sf-muni"));
    planeContext.command("/agency/actransit", "agency/set", Value.of("actransit"));
}
```
Most of the code in the `main` method is fairly boilerplatey. We first load a `Recon Configuration`, which we will
explain in further detail later. We then create an instance of the `Plane` and start up a server.

```java
planeContext.command("/agency/sf-muni", "agency/set", Value.of("sf-muni"));
planeContext.command("/agency/actransit", "agency/set", Value.of("actransit"));
```
These lines are fairly important because here we instantiate two `AgencyServices` by sending a command to them that sets
the agencies that they will display. Note: services get instantiated lazily.

```java
private static Value loadReconConfig(String[] args) throws IOException {
  String configPath;
  if (args.length > 0) {
    configPath = args[0];
  } else {
    configPath = System.getProperty("swim.config");
    if (configPath == null) {
      configPath = "/transit-space.recon";
    }
  }

  InputStream configInput = null;
  Value configValue;
  try {
    final File configFile = new File(configPath);
    if (configFile.exists()) {
      configInput = new FileInputStream(configFile);
    } else {
      configInput = TransitPlane.class.getResourceAsStream(configPath);
    }
    configValue = Decodee.readUtf8(Recon.FACTORY.blockParser(), configInput);
  } finally {
    try {
      if (configInput != null)
        configInput.close();
    } catch (Exception ignored) {
    }
  }
  return configValue;
}
```
The `loadReconConfig` method is fairly boilerplatey. The important things to note here are that it loads the
configuration from the file `/transit-space.recon`, which we will look at next.

### transit-space.recon
```
@server {
  @plane("transit") {
    class: "it.swim.transit.TransitPlane"
  }
  @store {
    path: "/tmp/swim/transit"
  }
  @http(port: 8090) {
    plane: "transit"
    @websocket {
      serverCompressionLevel: 0# -1 = default; 0 = off; 1-9 = deflate level
      clientCompressionLevel: 0# -1 = default; 0 = off; 1-9 = deflate level
    }
  }
}
```
This recon configuration file can be configured to whatever you want. Here, our plane is in
`it.swim.transit.TransitPlane` and we store data in the repository `/tmp/swim/transit`. We also tell the application
to run on port `8090`.

Thats all the java swim code.

### sf-muni.hml
Now, we will look at `sf-muni.html` to get a sense of the `Swim Client`.
Most of the code in the html file deals with displaying the data on a map. The swim part is actually fairly short and
simple.

```html
<script src="https://repo.swim.it/swim/recon-0.3.9.js"></script>
<script src="https://repo.swim.it/swim/swim-client-0.4.6.js"></script>
var host = Swim.host('ws://localhost:8090');
var organization = host.node('agency/sf-muni');
```
Here we define the host and node so we can get information from the `sf-muni` agency.

Now, we can `Downlink` and `sync` to the lanes we are interested in and do something whenever we get an `event`.

```javascript
var counts = organization.downlinkValue()
        	.lane('agency/count')
        	.onEvent(function (event) {
        		//code to update counts in UI
        		})
        	.open();
var speed = organization.downlinkValue()
        	.lane('agency/speed')
        	.onEvent(function (event) {
        		//code to update speed in UI
        		})
        	.open();
var devices = organization.downlinkMap()
            .lane('agency/vehicles')
            .onEvent(function (event) {
                //code to update UI to display vehicles on the map
                })
            .open();
```
