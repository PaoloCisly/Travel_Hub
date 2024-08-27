package it.unimib.travelhub.data.source;

import java.util.List;

import it.unimib.travelhub.model.Travels;
import it.unimib.travelhub.model.TravelsResponse;

public interface TravelsCallback {
    void onSuccessFromRemote(TravelsResponse travelsResponse, long lastUpdate);
    void onFailureFromRemote(Exception exception);
    void onSuccessFromLocal(TravelsResponse travelsResponse);
    void onFailureFromLocal(Exception exception);
    void onSuccessSynchronization(Travels travel);
    void onSuccessDeletion();
    void onSuccessDeletionFromRemote(Travels travel);
    void onSuccessFromCloudWriting(Travels travel);
    void onSuccessDeletionAfterSync(List<Travels> travelsList);
    void onSuccessDeletionFromLocal(Travels travel);

    void onUpdateSuccess(Travels travel);
}
