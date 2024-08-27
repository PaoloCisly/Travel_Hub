package it.unimib.travelhub.ui.profile;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

import it.unimib.travelhub.model.Travels;
import it.unimib.travelhub.model.TravelsResponse;

public class ProfileFragmentAdapter extends FragmentStateAdapter{

    private final TravelsResponse travelResponse;
    public ProfileFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, TravelsResponse travelResponse) {
        super(fragmentManager, lifecycle);
        this.travelResponse = travelResponse;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            List<Travels> ongoing  = travelResponse.getOnGoingTravelList();
            ongoing.addAll(travelResponse.getFutureTravelsList());
            return new OngoingTravelsFragment(ongoing);
        }else {
            return new TerminatedTravelsFragment(travelResponse.getDoneTravelsList());
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
