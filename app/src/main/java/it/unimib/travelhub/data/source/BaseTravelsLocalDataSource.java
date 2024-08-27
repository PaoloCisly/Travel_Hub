package it.unimib.travelhub.data.source;

import java.util.List;

import it.unimib.travelhub.model.Travels;

public abstract class BaseTravelsLocalDataSource {
    protected TravelsCallback travelsCallback;

    public void setTravelsCallback(TravelsCallback travelsCallback) {
        this.travelsCallback = travelsCallback;
    }

    public abstract void getTravels();
    public abstract void updateTravel(Travels travels);
    public abstract void insertTravels(List<Travels> travelsList);

    public abstract void deleteTravel(Travels travel);
    public abstract void deleteAll();
    public abstract void deleteAllAfterSync(List<Travels> travelsList);
}
