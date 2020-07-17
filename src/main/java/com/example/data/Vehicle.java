package com.example.data;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import java.util.UUID;

@Entity(name = "all_gtfs")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Vehicle extends PanacheEntityBase {

    @Entity(name = "ROUTE435")
    public static class Route435 extends Vehicle {
    }

    @Id
    private String id = UUID.randomUUID().toString();

    private String vid;

    private String label;

    private double lat;

    private double lon;

    private long lastUpdate;

    public Vehicle() {
    }

    public Vehicle(String id, String vid, String label, double lat, double lon, long lastUpdate) {
        this.id = id;
        this.vid = vid;
        this.label = label;
        this.lat = lat;
        this.lon = lon;
        this.lastUpdate = lastUpdate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVid() {
        return vid;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

}

