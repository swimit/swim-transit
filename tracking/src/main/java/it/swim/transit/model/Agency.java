package it.swim.transit.model;

import java.util.Objects;
import recon.Form;
import recon.ReconName;
import recon.Value;

@ReconName("agency")
public class Agency {

  private String id = "";
  private String state = "";
  private String country = "";
  private int index = 0;

  public Agency() {
  }

  public Agency(String id, String state, String country, int index) {
    this.id = id;
    this.state = state;
    this.country = country;
    this.index = index;
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

  public int getIndex() {
    return index;
  }

  public String getUri() {
    return "/agency/" + getCountry() + "/" + getState() + "/" + getId();
  }

  public Value toValue() {
    return Form.forClass(Agency.class).mold(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Agency agency = (Agency) o;
    return Objects.equals(id, agency.id) &&
        Objects.equals(state, agency.state) &&
        Objects.equals(country, agency.country) &&
        Objects.equals(index, agency.index);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, state, country, index);
  }

  @Override
  public String toString() {
    return "Agency{" +
        "id='" + id + '\'' +
        ", state='" + state + '\'' +
        ", country='" + country + '\'' +
        ", index=" + index +
        '}';
  }
}
