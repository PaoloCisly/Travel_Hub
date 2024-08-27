package it.unimib.travelhub.model;


import android.util.Log;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import it.unimib.travelhub.data.source.TravelsCallback;

public class TravelsResponse implements Serializable {
    private static final String TAG = "TravelsResponse";
    private List<Travels> travelsList;
    private final TravelsCallback travelsCallback;
    private final Integer length;

    public TravelsResponse(List<Travels> travelsList) {
        Collections.sort(travelsList);
        this.travelsList = travelsList;
        this.travelsCallback = null;
        this.length = travelsList.size();
    }

    public TravelsResponse(Integer length, TravelsCallback travelsCallback) {
        this.travelsList = new java.util.ArrayList<>(length);
        this.travelsCallback = travelsCallback;
        this.length = length;
    }

    public List<Travels> getTravelsList() {
        return travelsList;
    }

    public Travels getOnGoingTravel() {
        return getOnGoingTravelList().isEmpty() ? null : getOnGoingTravelList().get(0);
    }

    public List<Travels> getOnGoingTravelList() {
        Date currentDate = new Date();
        List<Travels> OngoingTravelsList = new java.util.ArrayList<>();
        for (Travels travel : travelsList) {
            if (travel.getStartDate().before(currentDate) && travel.getEndDate().after(currentDate)) {
                OngoingTravelsList.add(travel);
            }
        }
        return OngoingTravelsList;
    }

    public Travels getFutureTravel() {
        return getFutureTravelsList().isEmpty() ? null : getFutureTravelsList().get(0);
    }

    public Travels getDoneTravel() {
        return getDoneTravelsList().isEmpty() ? null : getDoneTravelsList().get(0);
    }

    public List<Travels> getFutureTravelsList() {
        Date currentDate = new Date();
        List<Travels> futureTravelsList = new java.util.ArrayList<>();
        for (Travels travels : travelsList) {
            if (travels.getStartDate().after(currentDate)) {
                futureTravelsList.add(travels);
            }
        }
        return futureTravelsList;
    }

    public List<Travels> getDoneTravelsList() {
        Date currentDate = new Date();
        List<Travels> doneTravelsList = new java.util.ArrayList<>();
        for (Travels travels : travelsList) {
            if (travels.getEndDate().before(currentDate)) {
                doneTravelsList.add(travels);
            }
        }
        Collections.reverse(doneTravelsList);
        return doneTravelsList;
    }

    public void setTravelsList(List<Travels> travelsList) {
        this.travelsList = travelsList;
    }

    public void addTravel(Travels travel) {
        travelsList.add(travel);
        if (travelsList.size() == length) {
            Collections.sort(travelsList);
            Log.d(TAG, "Travels list size: " + travelsList.size());
            travelsCallback.onSuccessFromRemote(this, System.currentTimeMillis());
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "TravelsResponse{" +
                "Travels List=" + travelsList +
                '}';
    }


}
