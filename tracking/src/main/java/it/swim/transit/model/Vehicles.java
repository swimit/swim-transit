package it.swim.transit.model;

import java.util.HashMap;
import java.util.Map;
import recon.ReconName;

import java.util.ArrayList;
import java.util.List;

@ReconName("vehicles")
public class Vehicles {

  private final Map<String, Vehicle> vehicles = new HashMap<String, Vehicle>();

  public Vehicles() {
  }

  public Map<String, Vehicle> getVehicles() {
    return vehicles;
  }

  public void add(Vehicle vehicle) {
    vehicles.put(vehicle.getUri(), vehicle);
  }

}
