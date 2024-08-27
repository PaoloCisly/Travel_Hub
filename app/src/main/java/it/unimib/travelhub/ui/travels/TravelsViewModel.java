package it.unimib.travelhub.ui.travels;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import it.unimib.travelhub.data.repository.travels.ITravelsRepository;
import it.unimib.travelhub.model.Result;
import it.unimib.travelhub.model.Travels;

public class TravelsViewModel extends ViewModel {

        private static final String TAG = TravelsViewModel.class.getSimpleName();

        private final ITravelsRepository travelsRepository;
        private MutableLiveData<Result> travelsListLiveData;
        private MutableLiveData<Result> updateTravelsMutableLiveData;

        public TravelsViewModel(ITravelsRepository iTravelsRepository) {
            this.travelsRepository = iTravelsRepository;
        }

        /**
        * Returns the LiveData object associated with the
        * travels list to the Fragment/Activity.
        * @return The LiveData object associated with the travels list.
        */
        public MutableLiveData<Result> getTravels(long lastUpdate) {
            if (travelsListLiveData == null) {
                fetchTravels(lastUpdate);
            }
            Log.d(TAG, "Travels list: " + travelsListLiveData.getValue());
            return travelsListLiveData;
        }

        public MutableLiveData<Result> getUpdateTravelsMutableLiveData() {
            if (updateTravelsMutableLiveData == null) {
                updateTravelsMutableLiveData = new MutableLiveData<>();
            }
            return updateTravelsMutableLiveData;
        }
        /**
        * Fetches the travels list from the repository.
        */
        private void fetchTravels(long lastUpdate) {
            travelsListLiveData = travelsRepository.fetchTravels(lastUpdate);
        }
        public void addTravel(Travels travel) {
            travelsListLiveData = travelsRepository.addTravel(travel);
        }

        public MutableLiveData<Result> getTravelAddLiveData() {
            if (travelsListLiveData == null) {
                travelsListLiveData = new MutableLiveData<>();
            }
            return travelsListLiveData;
        }

    public MutableLiveData<Result> deleteTravel(Travels travel) {
        return travelsRepository.deleteTravel(travel);
    }

    public void updateTravel(Travels newTravel, Travels oldTravel){
        updateTravelsMutableLiveData = travelsRepository.updateTravel(newTravel, oldTravel);
    }
}
