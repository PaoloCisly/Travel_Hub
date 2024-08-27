package it.unimib.travelhub.ui.travels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import it.unimib.travelhub.model.Travels;

public class FragmentActivityAddViewModel extends ViewModel {
    private final MutableLiveData<Travels> travelMutableLiveData;
    public FragmentActivityAddViewModel() {
        travelMutableLiveData = new MutableLiveData<>();
    }

    public void setTravel(Travels travel) {
        travelMutableLiveData.setValue(travel);
    }

    public MutableLiveData<Travels> getTravel() {
        return travelMutableLiveData;
    }
}
