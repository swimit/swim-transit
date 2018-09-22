package it.swim.transit.model;

import recon.ReconName;

import java.util.Objects;

@ReconName("vehicle")
public class Vehicle {

  private String id = "";
  private String routeTag = "";
  private String dirId = "";
  private float latitude = 0.0f;
  private float longitude = 0.0f;
  private int speed = 0;
  private int secsSinceReport = -1;
  private int index = 0;

  public Vehicle() {
  }

  public Vehicle(String id, String routeTag, String dirId, float latitude, float longitude, int speed, int secsSinceReport, int index) {
    this.id = id;
    this.routeTag = routeTag;
    this.dirId = dirId;
    this.latitude = latitude;
    this.longitude = longitude;
    this.speed = speed;
    this.secsSinceReport = secsSinceReport;
    this.index = index;
  }

  public String getId() {
    return id;
  }

  public Vehicle withId(String id) {
    return new Vehicle(id, routeTag, dirId, latitude, longitude, speed, secsSinceReport, index);
  }

  public String getRouteTag() {
    return routeTag;
  }

  public Vehicle withRouteTag(String routeTag) {
    return new Vehicle(id, routeTag, dirId, latitude, longitude, speed, secsSinceReport, index);
  }

  public String getDirId() {
    return dirId;
  }

  public Vehicle withDirId(String dirId) {
    return new Vehicle(id, routeTag, dirId, latitude, longitude, speed, secsSinceReport, index);
  }

  public float getLatitude() {
    return latitude;
  }

  public Vehicle withLatitude(float latitude) {
    return new Vehicle(id, routeTag, dirId, latitude, longitude, speed, secsSinceReport, index);
  }

  public float getLongitude() {
    return longitude;
  }

  public Vehicle withLongitude(float longitude) {
    return new Vehicle(id, routeTag, dirId, latitude, longitude, speed, secsSinceReport, index);
  }

  public int getSpeed() {
    return speed;
  }

  public Vehicle withSpeed(int speed) {
    return new Vehicle(id, routeTag, dirId, latitude, longitude, speed, secsSinceReport, index);
  }

  public int getSecsSinceReport() {
    return secsSinceReport;
  }

  public Vehicle withSecsSinceReport(int secsSinceReport) {
    return new Vehicle(id, routeTag, dirId, latitude, longitude, speed, secsSinceReport, index);
  }

  public int getIndex() {
    return index;
  }

  public Vehicle withIndex(int index) {
    return new Vehicle(id, routeTag, dirId, latitude, longitude, speed, secsSinceReport, index);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Vehicle vehicle = (Vehicle) o;
    return Float.compare(vehicle.latitude, latitude) == 0 &&
        Float.compare(vehicle.longitude, longitude) == 0 &&
        speed == vehicle.speed &&
        secsSinceReport == vehicle.secsSinceReport &&
        index == vehicle.index &&
        Objects.equals(id, vehicle.id) &&
        Objects.equals(routeTag, vehicle.routeTag) &&
        Objects.equals(dirId, vehicle.dirId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, routeTag, dirId, latitude, longitude, speed, secsSinceReport, index);
  }

  @Override
  public String toString() {
    return "Vehicle{" +
        "id='" + id + '\'' +
        ", routeTag='" + routeTag + '\'' +
        ", dirId='" + dirId + '\'' +
        ", latitude=" + latitude +
        ", longitude=" + longitude +
        ", speed=" + speed +
        ", secsSinceReport=" + secsSinceReport +
        ", index=" + index +
        '}';
  }
}
