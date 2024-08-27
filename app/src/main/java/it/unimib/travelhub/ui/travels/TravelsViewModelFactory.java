package it.unimib.travelhub.ui.travels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import it.unimib.travelhub.data.repository.travels.ITravelsRepository;

public class TravelsViewModelFactory implements ViewModelProvider.Factory {

    private final ITravelsRepository iTravelsRepository;

    public TravelsViewModelFactory(ITravelsRepository iTravelsRepository) {
        this.iTravelsRepository = iTravelsRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new TravelsViewModel(iTravelsRepository);
    }
}
