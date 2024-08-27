package it.unimib.travelhub.ui.profile;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import it.unimib.travelhub.adapter.TravelRecyclerAdapter;
import it.unimib.travelhub.databinding.FragmentOngoingTravelsBinding;
import it.unimib.travelhub.model.Travels;
import it.unimib.travelhub.ui.travels.TravelActivity;

public class OngoingTravelsFragment extends Fragment {
    private FragmentOngoingTravelsBinding binding;
    protected RecyclerView.LayoutManager mLayoutManager;
    private List<Travels> travels;

    public OngoingTravelsFragment() {
        // Required empty public constructor
    }

    public OngoingTravelsFragment(List<Travels> travels){
        super();
        this.travels = travels;
    }
    public static OngoingTravelsFragment newInstance() {
        return new OngoingTravelsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentOngoingTravelsBinding.inflate(inflater, container, false);
        mLayoutManager = new LinearLayoutManager(getActivity());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.clear();
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                return false;
            }
        });


        RecyclerView travelRecyclerViewRunning = binding.ongoingActivitiesRecyclerView;
        RecyclerView.LayoutManager layoutManagerRunning =
                new LinearLayoutManager(requireContext(),
                        LinearLayoutManager.VERTICAL, false);
        TravelRecyclerAdapter travelRecyclerAdapterRunning = new TravelRecyclerAdapter(travels,
                travel -> {
                    Intent intent = new Intent(requireActivity(), TravelActivity.class);
                    intent.putExtra("travel", travel);
                    startActivity(intent);
                });
        travelRecyclerViewRunning.setLayoutManager(layoutManagerRunning);
        travelRecyclerViewRunning.setAdapter(travelRecyclerAdapterRunning);


    }

}