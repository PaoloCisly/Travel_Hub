package it.unimib.travelhub.ui.main;

import static it.unimib.travelhub.util.Constants.ENCRYPTED_SHARED_PREFERENCES_FILE_NAME;
import static it.unimib.travelhub.util.Constants.LAST_UPDATE;
import static it.unimib.travelhub.util.Constants.PICS_FOLDER;
import static it.unimib.travelhub.util.Constants.PROFILE_PICTURE_FILE_NAME;
import static it.unimib.travelhub.util.Constants.SHARED_PREFERENCES_FILE_NAME;
import static it.unimib.travelhub.util.Constants.TRAVEL_ADDED;
import static it.unimib.travelhub.util.Constants.TRAVEL_DELETED;
import static it.unimib.travelhub.util.Constants.USER_PROFILE_IMAGE;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.text.SimpleDateFormat;

import it.unimib.travelhub.R;
import it.unimib.travelhub.adapter.UsersRecyclerAdapter;
import it.unimib.travelhub.crypto_util.DataEncryptionUtil;
import it.unimib.travelhub.data.repository.travels.ITravelsRepository;
import it.unimib.travelhub.data.repository.user.IUserRepository;
import it.unimib.travelhub.data.source.RemoteFileStorageSource;
import it.unimib.travelhub.databinding.FragmentHomeBinding;
import it.unimib.travelhub.model.Result;
import it.unimib.travelhub.model.Travels;
import it.unimib.travelhub.model.TravelsResponse;
import it.unimib.travelhub.ui.travels.AddTravelActivity;
import it.unimib.travelhub.ui.travels.TravelActivity;
import it.unimib.travelhub.ui.travels.TravelsViewModel;
import it.unimib.travelhub.ui.travels.TravelsViewModelFactory;
import it.unimib.travelhub.util.ServiceLocator;
import it.unimib.travelhub.util.SharedPreferencesUtil;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private static final String TAG = HomeFragment.class.getSimpleName();
    private FragmentHomeBinding binding;
    private TravelsViewModel travelsViewModel;
    private SharedPreferencesUtil sharedPreferencesUtil;
    private TravelsResponse travelsResponse;
    private Travels onGoingTravel;
    private Travels futureTravel;
    private Travels doneTravel;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected RecyclerView friendsRecyclerView;
    private IUserRepository userRepository;
    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment HomeFragment.
     */
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ITravelsRepository travelsRepository =
                ServiceLocator.getInstance().getTravelsRepository(
                        requireActivity().getApplication()
                );

        if (travelsRepository != null) {
            // This is the way to create a ViewModel with custom parameters
            // (see NewsViewModelFactory class for the implementation details)
            travelsViewModel = new ViewModelProvider(
                    requireActivity(),
                    new TravelsViewModelFactory(travelsRepository)).get(TravelsViewModel.class);
        } else {
            Snackbar.make(requireActivity().findViewById(android.R.id.content),
                    getString(R.string.unexpected_error), Snackbar.LENGTH_SHORT).show();
        }

        userRepository = ServiceLocator.getInstance().
                getUserRepository();

        if (sharedPreferencesUtil == null) {
            sharedPreferencesUtil = new SharedPreferencesUtil(requireActivity().getApplication());
        }
        DataEncryptionUtil dataEncryptionUtil = new DataEncryptionUtil(requireActivity().getApplication());

        try{
            String profileImageURL = dataEncryptionUtil.readSecretDataWithEncryptedSharedPreferences(
                    ENCRYPTED_SHARED_PREFERENCES_FILE_NAME,
                    USER_PROFILE_IMAGE
            );
            if (profileImageURL != null) {
                try {
                    File file = new File(requireActivity().getFilesDir() + PICS_FOLDER + PROFILE_PICTURE_FILE_NAME);
                    if (!file.exists())
                        if(!file.createNewFile())
                            Log.e(TAG, "Error creating profile image file");
                    userRepository.downloadProfileImage(profileImageURL, file, new RemoteFileStorageSource.downloadCallback() {
                        @Override
                        public void onSuccessDownload(String url) {
                            Log.d(TAG, "Profile image downloaded successfully");
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Error downloading profile image: " + e.getMessage());
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error creating profile image file: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error decrypting profile image URL: " + e.getMessage());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        mLayoutManager = new LinearLayoutManager(getActivity());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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

        Intent intent = requireActivity().getIntent();

        if (intent.getBooleanExtra(TRAVEL_ADDED, false)) {
            Snackbar.make(requireActivity().findViewById(android.R.id.content),
                    getString(R.string.travel_added_success),
                    Snackbar.LENGTH_SHORT).show();
            intent.removeExtra(TRAVEL_ADDED);
        }

        if (intent.getBooleanExtra(TRAVEL_DELETED, false)) {
            Snackbar.make(requireActivity().findViewById(android.R.id.content),
                    getString(R.string.travel_deleted_success),
                    Snackbar.LENGTH_SHORT).show();
            intent.removeExtra(TRAVEL_DELETED);
        }

        String lastUpdate = "0";
        if (sharedPreferencesUtil.readStringData(SHARED_PREFERENCES_FILE_NAME, LAST_UPDATE) != null) {
            lastUpdate = sharedPreferencesUtil.readStringData(SHARED_PREFERENCES_FILE_NAME, LAST_UPDATE);
        }

        friendsRecyclerView = binding.friendsRecyclerView;
//        travelSegmentsRecyclerView = binding.segmentsRecyclerView;

        binding.homeCardOther.setVisibility(View.GONE);
        binding.homeCardNoTravel.setVisibility(View.GONE);
        binding.homeLayoutOther.setVisibility(View.GONE);
        binding.homeCardOngoing.setVisibility(View.GONE);

        travelsViewModel.getTravels(Long.parseLong(lastUpdate)).observe(getViewLifecycleOwner(),
            result -> {
                binding.homeProgressBar.setVisibility(View.GONE);
                if (result.isSuccess()) {
                    Log.d(TAG, "TravelsResponse: " + ((Result.TravelsResponseSuccess) result).getData());
                    travelsResponse = ((Result.TravelsResponseSuccess) result).getData();

                    onGoingTravel = travelsResponse.getOnGoingTravel();
                    futureTravel = travelsResponse.getFutureTravel();
                    doneTravel = travelsResponse.getDoneTravel();

                    if (onGoingTravel != null) {
                        setOngoingView(onGoingTravel);

                        if (futureTravel != null) {
                            setFutureView(futureTravel);
                        } else if (doneTravel != null) {
                            setPastView(doneTravel);
                        }else{
                            //binding.homeCardNoTravel.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primaryVariantColor));
                            binding.homeTextNoFutureTravels.setText(R.string.no_travels_msg);
                            binding.homeNewTravelImage.setVisibility(View.GONE);
                            binding.homeCardNoTravel.setVisibility(View.VISIBLE);
                        }

                    } else if (futureTravel != null) {
                        setOngoingView(futureTravel);

                        if (doneTravel != null) {
                            setPastView(doneTravel);
                        }else {
                            binding.homeTextNoFutureTravels.setText(R.string.no_travels_msg);
                            binding.homeCardNoTravel.setVisibility(View.VISIBLE);
                        }


                    } else {
                        binding.homeTextNoFutureTravels.setText(R.string.no_travels_msg);
                        binding.homeNewTravelImage.setVisibility(View.GONE);
                        binding.homeCardNoTravel.setVisibility(View.VISIBLE);
                        if (doneTravel != null) {
                            setPastView(doneTravel);
                        }
                    }

                    binding.homeButtonCreateTravel.setOnClickListener(v -> {
                        Intent AddTravelintent = new Intent(getActivity(), AddTravelActivity.class);
                        startActivity(AddTravelintent);
                        //requireActivity().finish();
                    });

                    binding.seeAll.setOnClickListener(v -> {
                        ViewPager2 viewPager = requireActivity().findViewById(R.id.viewPagerMain);
                        viewPager.setCurrentItem(3);
                    });

                    binding.homeOngoingButton.setOnClickListener(v -> {
                        Intent intentOngoing = new Intent(getActivity(), TravelActivity.class);
                        intentOngoing.putExtra("travel", onGoingTravel);
                        startActivity(intentOngoing);
                    });

                } else {
                    Log.d(TAG, "TravelsResponse Error: " + ((Result.Error) result).getMessage());
                    binding.homeCardNoTravel.setVisibility(View.VISIBLE);
                    binding.homeTextNoFutureTravels.setText(R.string.no_travels_msg);
                    binding.homeButtonCreateTravel.setOnClickListener(v -> {
                        Intent AddTravelintent = new Intent(getActivity(), AddTravelActivity.class);
                        startActivity(AddTravelintent);
                        //requireActivity().finish();
                    });
                }
            });
    }

    private void setOngoingView(Travels onGoingTravel) {
        RecyclerView.LayoutManager layoutManagerRunning =
                new LinearLayoutManager(requireContext(),
                        LinearLayoutManager.HORIZONTAL, false);

        binding.homeCardOngoing.setVisibility(View.VISIBLE);
        UsersRecyclerAdapter travelRecyclerAdapterRunning = new UsersRecyclerAdapter(onGoingTravel.getMembers(), 2, null, null, userRepository, "#FFFFFF");
        friendsRecyclerView.setLayoutManager(layoutManagerRunning);
        friendsRecyclerView.setAdapter(travelRecyclerAdapterRunning);

        long currentTime = System.currentTimeMillis();

        binding.cardViewStatus.setVisibility(View.VISIBLE);

        if(onGoingTravel.getStartDate().getTime() <= currentTime && onGoingTravel.getEndDate().getTime() >= currentTime){
            binding.textViewStatus.setText(R.string.status_ongoing);
            binding.textViewStatus.setTextColor(Color.parseColor("#BCECAF"));
            binding.cardViewStatus.getBackground().setTint(Color.parseColor("#2A2FFF00"));
            binding.imageViewStatus.setColorFilter(Color.parseColor("#BCECAF"));
        } else {
            binding.textViewStatus.setText(R.string.status_future);
            binding.textViewStatus.setTextColor(Color.parseColor("#BCECAF"));
            binding.cardViewStatus.getBackground().setTint(Color.parseColor("#7A8959DF"));
            binding.imageViewStatus.setColorFilter(Color.parseColor("#BCECAF"));
        }

        binding.homeOngoingTitle.setText(onGoingTravel.getTitle());
        binding.homeOngoingLocation.setText(onGoingTravel.getDestinations().get(0).getLocation());
        String dates = new SimpleDateFormat("dd/MM/yyyy", requireActivity().getResources().getConfiguration().getLocales().get(0))
                .format(onGoingTravel.getStartDate()) + " - " +
                new SimpleDateFormat("dd/MM/yyyy", requireActivity().getResources().getConfiguration().getLocales().get(0))
                        .format(onGoingTravel.getEndDate());
        binding.homeOngoingDates.setText(dates);
        String destinations = (onGoingTravel.getDestinations().size() - 1) + " " + getResources().getString(R.string.travel_segment_number);
        binding.homeOngoingNSegments.setText(destinations);
    }

    private void setFutureView(Travels futureTravel) {
        binding.homeCardOther.setVisibility(View.VISIBLE);
        binding.homeLayoutOther.setVisibility(View.VISIBLE);

        binding.homeCardOther.setOnClickListener(v -> {
            Intent intentOtherTravel = new Intent(getActivity(), TravelActivity.class);
            intentOtherTravel.putExtra("travel", futureTravel);
            startActivity(intentOtherTravel);
        });

        CardView travel_card = binding.homeTravelItem.getRoot();
        //ImageView travel_image = travel_card.findViewById(R.id.travel_image);
        TextView travel_title = travel_card.findViewById(R.id.travel_title);
        TextView travel_date = travel_card.findViewById(R.id.travel_date);
        TextView travel_destinations = travel_card.findViewById(R.id.travel_destinations);

        travel_title.setText(futureTravel.getTitle());
        travel_date.setText(
                new SimpleDateFormat("dd/MM", requireActivity().getResources().getConfiguration().getLocales().get(0))
                        .format(futureTravel.getStartDate())
        );
        String destinations = (futureTravel.getDestinations().size() - 1) + " " + getResources().getString(R.string.travel_segment_number);
        travel_destinations.setText(destinations);

    }
    private void setPastView(Travels pastTravel) {
        binding.homeCardOther.setVisibility(View.VISIBLE);
        binding.homeLayoutOther.setVisibility(View.VISIBLE);
        binding.homeCardOther.setOnClickListener(v -> {
            Intent intentOtherTravel = new Intent(getActivity(), TravelActivity.class);
            intentOtherTravel.putExtra("travel", pastTravel);
            startActivity(intentOtherTravel);
        });

        CardView travel_card = binding.homeTravelItem.getRoot();
        //ImageView travel_image = travel_card.findViewById(R.id.travel_image);
        TextView travel_title = travel_card.findViewById(R.id.travel_title);
        TextView travel_date = travel_card.findViewById(R.id.travel_date);
        TextView travel_destinations = travel_card.findViewById(R.id.travel_destinations);

        travel_title.setText(pastTravel.getTitle());
        travel_date.setText(
                new SimpleDateFormat("dd/MM", requireActivity().getResources().getConfiguration().getLocales().get(0))
                        .format(pastTravel.getEndDate())
        );
        String destinations = (pastTravel.getDestinations().size() - 1) + " " + getResources().getString(R.string.travel_segment_number);
        travel_destinations.setText(destinations);

    }

}