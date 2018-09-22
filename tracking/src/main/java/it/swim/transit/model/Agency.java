package it.swim.transit.model;

import recon.ReconName;

import java.util.Objects;

@ReconName("agency")
public class Agency {

  private String id = "";
  private String state = "";
  private String country = "";

  public Agency() {
  }

  public Agency(String id, String state, String country) {
    this.id = id;
    this.state = state;
    this.country = country;
  }

  public String getId() {
    return id;
  }

  public String getState() {
    return state;
  }

  public String getCountry() {
    return country;
  }

  public String getUri() {
    return "/agency/" + getCountry() + "/" + getState() + "/" + getId();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Agency agency = (Agency) o;
    return Objects.equals(id, agency.id) &&
        Objects.equals(state, agency.state) &&
        Objects.equals(country, agency.country);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, state, country);
  }

  @Override
  public String toString() {
    return "Agency{" +
        "id='" + id + '\'' +
        ", state='" + state + '\'' +
        ", country='" + country + '\'' +
        '}';
  }
}
