package it.swim.transit.model;

import java.util.Objects;
import recon.Form;
import recon.ReconName;
import recon.Value;

@ReconName("vehicle")
public class Vehicle {

  private String id = "";
  private String uri = "";
  private String agency = "";
  private String routeTag = "";
  private String dirId = "";
  private float latitude = 0.0f;
  private float longitude = 0.0f;
  private int speed = 0;
  private int secsSinceReport = -1;
  private int index = 0;
  private String heading = "";
  private String routeTitle = "";

  public Vehicle() {
  }

  public Vehicle(String id, String uri, String agency, String routeTag, String dirId, float latitude, float longitude,
                 int speed, int secsSinceReport, int index, String heading, String routeTitle) {
    this.id = id;
    this.uri = uri;
    this.agency = agency;
    this.routeTag = routeTag;
    this.dirId = dirId;
    this.latitude = latitude;
    this.longitude = longitude;
    this.speed = speed;
    this.secsSinceReport = secsSinceReport;
    this.index = index;
    this.heading = heading;
    this.routeTitle = routeTitle;
  }

  public String getId() {
    return id;
  }

  public Vehicle withId(String id) {
    return new Vehicle(id, uri, agency, routeTag, dirId, latitude, longitude, speed, secsSinceReport, index, heading, routeTitle);
  }

  public String getUri() {
    return uri;
  }

  public Vehicle withUri(String uri) {
    return new Vehicle(id, uri, agency, routeTag, dirId, latitude, longitude, speed, secsSinceReport, index, heading, routeTitle);
  }

  public String getAgency() {
    return agency;
  }

  public Vehicle withAgency(String agency) {
    return new Vehicle(id, uri, agency, routeTag, dirId, latitude, longitude, speed, secsSinceReport, index, heading, routeTitle);
  }

  public String getRouteTag() {
    return routeTag;
  }

  public Vehicle withRouteTag(String routeTag) {
    return new Vehicle(id, uri, agency, routeTag, dirId, latitude, longitude, speed, secsSinceReport, index, heading, routeTitle);
  }

  public String getDirId() {
    return dirId;
  }

  public Vehicle withDirId(String dirId) {
    return new Vehicle(id, uri, agency, routeTag, dirId, latitude, longitude, speed, secsSinceReport, index, heading, routeTitle);
  }

  public float getLatitude() {
    return latitude;
  }

  public Vehicle withLatitude(float latitude) {
    return new Vehicle(id, uri, agency, routeTag, dirId, latitude, longitude, speed, secsSinceReport, index, heading, routeTitle);
  }

  public float getLongitude() {
    return longitude;
  }

  public Vehicle withLongitude(float longitude) {
    return new Vehicle(id, uri, agency, routeTag, dirId, latitude, longitude, speed, secsSinceReport, index, heading, routeTitle);
  }

  public int getSpeed() {
    return speed;
  }

  public Vehicle withSpeed(int speed) {
    return new Vehicle(id, uri, agency, routeTag, dirId, latitude, longitude, speed, secsSinceReport, index, heading, routeTitle);
  }

  public int getSecsSinceReport() {
    return secsSinceReport;
  }

  public Vehicle withSecsSinceReport(int secsSinceReport) {
    return new Vehicle(id, uri, agency, routeTag, dirId, latitude, longitude, speed, secsSinceReport, index, heading, routeTitle);
  }

  public int getIndex() {
    return index;
  }

  public Vehicle withIndex(int index) {
    return new Vehicle(id, uri, agency, routeTag, dirId, latitude, longitude, speed, secsSinceReport, index, heading, routeTitle);
  }

  public String getHeading() {
    return heading;
  }

  public Vehicle withHeading(String heading) {
    return new Vehicle(id, uri, agency, routeTag, dirId, latitude, longitude, speed, secsSinceReport, index, heading, routeTitle);
  }

  public String getRouteTitle() {
    return routeTitle;
  }

  public Vehicle withRouteTitle(String routeTitle) {
    return new Vehicle(id, uri, agency, routeTag, dirId, latitude, longitude, speed, secsSinceReport, index, heading, routeTitle);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Vehicle vehicle = (Vehicle) o;
    return Float.compare(vehicle.latitude, latitude) == 0 &&
        Float.compare(vehicle.longitude, longitude) == 0 &&
        speed == vehicle.speed &&
        secsSinceReport == vehicle.secsSinceReport &&
        index == vehicle.index &&
        Objects.equals(id, vehicle.id) &&
        Objects.equals(uri, vehicle.uri) &&
        Objects.equals(agency, vehicle.agency) &&
        Objects.equals(routeTag, vehicle.routeTag) &&
        Objects.equals(dirId, vehicle.dirId) &&
        Objects.equals(heading, vehicle.heading) &&
        Objects.equals(routeTitle, vehicle.routeTitle);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, agency, routeTag, dirId, latitude, longitude, speed, secsSinceReport, index, heading, routeTitle);
  }

  @Override
  public String toString() {
    return "Vehicle{" +
        "id='" + id + '\'' +
        "uri='" + uri + '\'' +
        ", agency='" + agency + '\'' +
        ", routeTag='" + routeTag + '\'' +
        ", dirId='" + dirId + '\'' +
        ", latitude=" + latitude +
        ", longitude=" + longitude +
        ", speed=" + speed +
        ", secsSinceReport=" + secsSinceReport +
        ", index=" + index +
        ", heading=" + heading +
        ", routeTitle =" + routeTitle +
        '}';
  }

  public Value toValue() {
    return Form.forClass(Vehicle.class).mold(this);
  }
}
