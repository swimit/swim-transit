package it.swim.transit.model;

import recon.ReconName;

@ReconName("route")
public class Route {

  private String tag = "";
  private String title = "";


  public Route() {
  }

  public Route(String tag, String title) {
    this.tag = tag;
    this.title = title;
  }

  public String getTag() {
    return tag;
  }

  public Route withTag(String tag) {
    return new Route(tag, title);
  }

  public String getTitle() {
    return title;
  }

  public Route withTitle(String title) {
    return new Route(tag, title);
  }
}
