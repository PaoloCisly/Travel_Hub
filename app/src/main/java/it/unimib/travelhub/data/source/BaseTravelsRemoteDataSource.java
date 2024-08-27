package it.unimib.travelhub.data.source;

import it.unimib.travelhub.model.Travels;

public abstract class BaseTravelsRemoteDataSource {
    protected TravelsCallback travelsCallback;

    public void setTravelsCallback(TravelsCallback travelsCallback) {
        this.travelsCallback = travelsCallback;
    }

    public abstract void getAllUserTravel();
    public abstract void addTravel(Travels travel);
    // public abstract void getTravelById(String travelId);


    public abstract void updateTravel(Travels newTravel, Travels oldTravel);

    public abstract void deleteTravel(Travels travels);

}
