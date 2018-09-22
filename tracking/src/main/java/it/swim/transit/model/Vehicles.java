package it.swim.transit.model;

import recon.ReconName;

import java.util.ArrayList;
import java.util.List;

@ReconName("vehicles")
public class Vehicles {

  private final List<Vehicle> vehicles = new ArrayList<Vehicle>();

  public Vehicles() {
  }

  public List<Vehicle> getVehicles() {
    return vehicles;
  }

  public void add(Vehicle vehicle) {
    vehicles.add(vehicle);
  }

}
