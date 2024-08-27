package it.unimib.travelhub.model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Date;

public class TravelSegment implements Serializable, Comparable<TravelSegment> {

    private String location;
    private Date dateFrom;
    private Date dateTo;
    private String description;
    private double lat;
    private double lng;

    public TravelSegment() {
        this.location = null;
        this.dateFrom = null;
        this.dateTo = null;
        this.description = null;
    }

    public TravelSegment(String location){
        super();
        this.location = location;
    }

    public TravelSegment(String location, Date dateFrom, Date dateTo, String description) {
        super();
        this.location = location;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public String getDescription() {
        return description;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int compareTo(TravelSegment travels) {
        if (this.dateFrom.compareTo(travels.dateFrom) == 0)
            return this.dateTo.compareTo(travels.dateTo);
        else
            return this.dateFrom.compareTo(travels.dateFrom);
    }

    @NonNull
    @Override
    public String toString() {
        return "TravelSegment{" +
                "location='" + location + '\'' +
                ", dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                ", description='" + description + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                '}';
    }

    public void setLatLng(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() {
        return this.lat;
    }

    public double getLng() {
        return this.lng;
    }
}
