package it.swim.transit;

import recon.ReconName;

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
}
