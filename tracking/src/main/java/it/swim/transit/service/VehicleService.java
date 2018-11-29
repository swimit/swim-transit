package it.swim.transit.service;

import it.swim.transit.model.Vehicle;
import swim.api.AbstractService;
import swim.api.CommandLane;
import swim.api.MapLane;
import swim.api.SwimLane;
import swim.api.ValueLane;

public class VehicleService extends AbstractService {

  private long lastReportedTime = 0L;

  @SwimLane("vehicle")
  public ValueLane<Vehicle> vehicle = valueLane().valueClass(Vehicle.class);

  @SwimLane("speeds")
  public MapLane<Long, Integer> speeds = mapLane().keyClass(Long.class).valueClass(Integer.class).isTransient(true);

  @SwimLane("accelerations")
  public MapLane<Long, Integer> accelerations = mapLane().keyClass(Long.class).valueClass(Integer.class).isTransient(true);

  @SwimLane("addVehicle")
  public CommandLane<Vehicle> addVehicle = commandLane().valueClass(Vehicle.class).onCommand(v -> onVehicle(v));

  private void onVehicle(Vehicle v) {
    final long time = System.currentTimeMillis() - (v.getSecsSinceReport() * 1000L);
    final int oldSpeed = vehicle.get() != null ? vehicle.get().getSpeed() : 0;
    this.vehicle.set(v);
    speeds.put(time, v.getSpeed());
    if (speeds.size() > 10) {
      speeds.drop(speeds.size() - 10);
    }
    if (lastReportedTime > 0) {
      final float acceleration = (v.getSpeed() - oldSpeed) / ((time - lastReportedTime) / 3600);
      accelerations.put(time, Math.round(acceleration));
      if (accelerations.size() > 10) {
        accelerations.drop(accelerations.size() - 10);
      }
    }
    lastReportedTime = time;
  }

  @Override
  public void didStart() {
    //System.out.println("Started Service: " + nodeUri().toUri());
  }
}
