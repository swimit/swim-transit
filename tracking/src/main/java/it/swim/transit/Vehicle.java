package it.swim.transit;

import it.swim.recon.*;


@ReconName("Vehicle")
public class Vehicle {
	private String id;
	private String routeId;
	private String dirId;
	private String latitude;
	private String longitude;
	private String speed;
	private String secsSinceReport;
	
	public Vehicle (String id, String dId, String rId, String lat, String lng, String speed, String secsSinceRprt) {
		this.id = id;
		this.dirId = dId;
		this.routeId = rId;
		this.latitude = lat;
		this.longitude = lng;
		this.speed = speed;
		this.secsSinceReport = secsSinceRprt;
	}
	
	public String getId() {return this.id;}
	
	public String getDirId() {return this.dirId;}
	
	public String getRouteId() {return this.routeId;}
	
	public String getLatitude() {return this.latitude;}
	
	public String getLongitude() {return this.longitude;}
	
	public String getSpeed() {return this.speed;}
	
	public String getSecsSinceReport() {return this.secsSinceReport;}
	
	public String toString() {
		return String.format("Vehicle{id:%s,routeId:%s,dirId:%s,latitude:%s,longitude:%s,speed:%s,secsSinceReport:%s}", 
				this.id, this.routeId, this.dirId, this.latitude, this.longitude, this.speed, this.secsSinceReport);
	}
	
	public boolean equals(Vehicle a) {
		System.out.println("called");
		/*boolean truth = this.id.equals((a.getId())) 
				&& this.routeId.equals(a.getRouteId()) 
				&& this.dirId.equals(a.getDirId()) 
				&& this.latitude.equals(a.getLatitude()) 
				&& this.longitude.equals(a.getLongitude()) 
				&& this.speed.equals(a.getSpeed()) 
				&& this.secsSinceReport.equals(a.getSecsSinceReport());*/
		System.out.println(a.getId());
		System.out.println("Finished");
		//return truth;
		return true;
		}
}
