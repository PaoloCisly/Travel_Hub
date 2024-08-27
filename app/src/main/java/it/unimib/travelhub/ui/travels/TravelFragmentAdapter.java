package it.unimib.travelhub.ui.travels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import it.unimib.travelhub.model.Travels;

public class TravelFragmentAdapter extends FragmentStateAdapter{

    private final Travels travel;
    public TravelFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, Travels travel) {
        super(fragmentManager, lifecycle);
        this.travel = travel;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        Log.d("TravelFragmentAdapter", "position: " + position);
        if (position == 0) {
            return new TravelDashboardFragment(travel);
        }else {
            return new TravelItineraryFragment(travel);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
