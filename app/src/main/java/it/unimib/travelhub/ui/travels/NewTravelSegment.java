package it.unimib.travelhub.ui.travels;

import static it.unimib.travelhub.util.Constants.TRAVEL_ADDED;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import it.unimib.travelhub.R;
import it.unimib.travelhub.data.repository.travels.ITravelsRepository;
import it.unimib.travelhub.databinding.FragmentEditTravelSegmentBinding;
import it.unimib.travelhub.model.Result;
import it.unimib.travelhub.model.TravelSegment;
import it.unimib.travelhub.model.Travels;
import it.unimib.travelhub.ui.main.MainActivity;
import it.unimib.travelhub.util.ServiceLocator;

public class NewTravelSegment extends Fragment {

    private FragmentEditTravelSegmentBinding binding;
    private Travels travel;

    private TravelsViewModel travelsViewModel;

    final Calendar myCalendar= Calendar.getInstance();
    Button saveTravelBtn;

    private static final String TAG = NewTravelSegment.class.getSimpleName();

    public NewTravelSegment() {
        // Required empty public constructor
    }

    public static NewTravelSegment newInstance() {
        return new NewTravelSegment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            travel = (Travels) getArguments().getSerializable("travel");
        } else {
            Log.d("EditTravelSegment", "onCreate: no arguments");
        }
        saveTravelBtn = requireActivity().findViewById(R.id.button_save_activity);

        ITravelsRepository travelsRepository =
                ServiceLocator.getInstance().getTravelsRepository(
                        requireActivity().getApplication()
                );

        if (travelsRepository != null) {
            travelsViewModel = new ViewModelProvider(
                    requireActivity(),
                    new TravelsViewModelFactory(travelsRepository)).get(TravelsViewModel.class);
        }

        Places.initialize(requireContext(), "AIzaSyCFJYe15Sn6wp0A8yYWl3qv8t5pHsxaYUU");
        @SuppressWarnings("unused")
        PlacesClient placesClient = Places.createClient(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentEditTravelSegmentBinding.inflate(inflater, container, false);

        Log.d("EditTravelSegment", "onCreate: " + travel);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", getResources().getConfiguration().getLocales().get(0));
        Date toDate = travel.getDestinations().get(travel.getDestinations().size() - 1).getDateTo();
        if (toDate != null)
            binding.fromEditText.setText(sdf.format(toDate));

        binding.addDestinationButtonSegment.setOnClickListener(v -> {
            if(checkNulls()){
                return;
            }
            Bundle bundle = new Bundle();
            travel.getDestinations().add(buildTravelSegment());
            bundle.putSerializable("travel", travel);

            NewTravelSegment newTravelSegment = new NewTravelSegment();
            newTravelSegment.setArguments(bundle);
            // change fragment and pass the data
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.activityAddFragmentContainerView, newTravelSegment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        saveTravelBtn.setClickable(true);
       //make it visible appear clickable
        saveTravelBtn.setAlpha(1f);




        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(requireActivity());
                builder1.setMessage("Are you sure you want to come back? Your last destination will be lost.");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Yes",
                        (dialog, id) -> {
                            dialog.cancel();
                            travel.getDestinations().remove(travel.getDestinations().size() - 1);
                            getParentFragmentManager().popBackStack();
                        });

                builder1.setNegativeButton(
                        "No",
                        (dialog, id) -> dialog.cancel());

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DatePickerDialog.OnDateSetListener date1 = (v, year, month, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR,year);
            myCalendar.set(Calendar.MONTH,month);
            myCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
            updateLabel(binding.fromEditText);
        };
        DatePickerDialog.OnDateSetListener date2 = (v, year, month, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR,year);
            myCalendar.set(Calendar.MONTH,month);
            myCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
            updateLabel(binding.toEditText);
        };
        binding.fromEditText.setOnClickListener(v ->
        {
            if (getContext() != null)
                new DatePickerDialog(
                        getContext(), date1,
                        Objects.requireNonNull(binding.fromEditText.getText()).toString().isEmpty() ?
                                myCalendar.get(Calendar.YEAR) :
                                Integer.parseInt(binding.fromEditText.getText().toString().split("/")[2]),
                        Objects.requireNonNull(binding.fromEditText.getText()).toString().isEmpty() ?
                                myCalendar.get(Calendar.MONTH) :
                                Integer.parseInt(binding.fromEditText.getText().toString().split("/")[1]) - 1,
                        Objects.requireNonNull(binding.fromEditText.getText()).toString().isEmpty() ?
                                myCalendar.get(Calendar.DAY_OF_MONTH) :
                                Integer.parseInt(binding.fromEditText.getText().toString().split("/")[0])).show();
        });

        binding.toEditText.setOnClickListener(v ->
        {
            if (getContext() != null)
                new DatePickerDialog(
                        getContext(), date2,
                        Objects.requireNonNull(binding.toEditText.getText()).toString().isEmpty() ?
                                (Objects.requireNonNull(binding.fromEditText.getText()).toString().isEmpty() ?
                                        myCalendar.get(Calendar.YEAR) :
                                        Integer.parseInt(binding.fromEditText.getText().toString().split("/")[2])) :
                                Integer.parseInt(binding.toEditText.getText().toString().split("/")[2]),
                        Objects.requireNonNull(binding.toEditText.getText()).toString().isEmpty() ?
                                (Objects.requireNonNull(binding.fromEditText.getText()).toString().isEmpty() ?
                                        myCalendar.get(Calendar.MONTH) :
                                        Integer.parseInt(binding.fromEditText.getText().toString().split("/")[1]) - 1) :
                                Integer.parseInt(binding.toEditText.getText().toString().split("/")[1]) - 1,
                        Objects.requireNonNull(binding.toEditText.getText()).toString().isEmpty() ?
                                (Objects.requireNonNull(binding.fromEditText.getText()).toString().isEmpty() ?
                                        myCalendar.get(Calendar.DAY_OF_MONTH) :
                                        Integer.parseInt(binding.fromEditText.getText().toString().split("/")[0])) :
                                Integer.parseInt(binding.toEditText.getText().toString().split("/")[0])).show();
        });

        binding.showAutocompleteLocatorLayout.setOnClickListener(v -> {
            Log.d("TravelItineraryFragment", "autocompleteNewLayout clicked");
            binding.autocompleteNewLayout.setVisibility(View.VISIBLE);
            binding.autocompleteNewLayout.setOnClickListener(v1 -> binding.autocompleteNewLayout.setVisibility(View.GONE));
        });

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(view.findViewById(R.id.autocomplete_new_fragment).getId());
        Log.d("TravelItineraryFragment", "autocompleteFragment: " + autocompleteFragment);
        assert autocompleteFragment != null;
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));


        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onError(@NonNull Status status) {
                Log.d("TravelItineraryFragment", "An error occurred: " + status);
                binding.autocompleteNewLayout.setVisibility(View.GONE);
            }

            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.d("TravelItineraryFragment", "Place: " + place.getLatLng());
                binding.destinationEditText.setText(place.getName());
                binding.latitudeEditText.setText(String.valueOf(Objects.requireNonNull(place.getLatLng()).latitude));
                binding.longitudeEditText.setText(String.valueOf(place.getLatLng().longitude));
                binding.autocompleteNewLayout.setVisibility(View.GONE);
            }
        });


        saveTravelBtn.setOnClickListener(v -> {
            if(checkNulls()){
                return;
            }
            travel.getDestinations().add(buildTravelSegment());
            Log.d(TAG, "TRAVEL TO CREATE: " + travel);
            //add the travel
            travelsViewModel.addTravel(travel);
            attachTravelObserver();

        });
    }

    private TravelSegment buildTravelSegment() {
        TravelSegment travelSegment = new TravelSegment();
        travelSegment.setLocation(Objects.requireNonNull(binding.destinationEditText.getText()).toString());
        travelSegment.setDescription(Objects.requireNonNull(binding.descriptionEditText.getText()).toString());
        String dateFrom = Objects.requireNonNull(binding.fromEditText.getText()) + " 00:00:00";
        String dateTo = Objects.requireNonNull(binding.toEditText.getText()) + " 23:59:59";

        travelSegment.setLatLng(Double.parseDouble(Objects.requireNonNull(binding.latitudeEditText.getText()).toString()),
                Double.parseDouble(Objects.requireNonNull(binding.longitudeEditText.getText()).toString()));
        travelSegment.setDateFrom(parseStringToDate(dateFrom));
        travelSegment.setDateTo(parseStringToDate(dateTo));
        return travelSegment;
    }

    private boolean checkNulls(){
        boolean isNull = false;
        if (Objects.requireNonNull(binding.destinationEditText.getText()).toString().isEmpty()) {
            binding.destinationEditText.setError(getString(R.string.destination_error));
            isNull = true;
        } else {
            binding.destinationEditText.setError(null);
        }
        if (Objects.requireNonNull(binding.fromEditText.getText()).toString().isEmpty()) {
            binding.fromEditText.setError(getString(R.string.from_need));
            isNull = true;
        } else {
            binding.fromEditText.setError(null);
        }
        if (Objects.requireNonNull(binding.toEditText.getText()).toString().isEmpty()) {
            binding.toEditText.setError(getString(R.string.to_need));
            isNull = true;
        } else{
            binding.fromEditText.setError(null);
        }
        Date dateFrom = null;
        Date dateTo = null;
        if (!Objects.requireNonNull(binding.fromEditText.getText()).toString().isEmpty())
            dateFrom = parseStringToDate(binding.fromEditText.getText().toString() + " 00:00:00");
        Date startDate = travel.getStartDate();
        if (!Objects.requireNonNull(binding.toEditText.getText()).toString().isEmpty())
            dateTo = parseStringToDate(binding.toEditText.getText().toString() + " 23:59:59");
        Date endDate = travel.getEndDate();
        if (dateFrom != null) {
            if (dateFrom.before(startDate)) {
                binding.fromEditText.setError(getString(R.string.date_under_limit));
                isNull = true;
            }else if(dateFrom.after(endDate)){
                binding.fromEditText.setError(getString(R.string.date_over_limit));
                isNull = true;
            }else{
                binding.fromEditText.setError(null);
            }
        }
        if (dateTo != null) {
            if (dateTo.before(startDate)) {
                binding.toEditText.setError(getString(R.string.date_under_limit));
                isNull = true;
            }else if(dateTo.after(endDate)){
                binding.toEditText.setError(getString(R.string.date_over_limit));
                isNull = true;
            }else{
                binding.toEditText.setError(null);
            }
        }


        return isNull;
    }

    public Date parseStringToDate(String date){
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", getResources().getConfiguration().getLocales().get(0));
        Date parsedDate = null;
        try {
            parsedDate = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return parsedDate;
    }

    private void updateLabel(EditText editText) {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, getResources().getConfiguration().getLocales().get(0));
        editText.setText(sdf.format(myCalendar.getTime()));
    }

    private void attachTravelObserver(){
        travelsViewModel.getTravelAddLiveData().observe(getViewLifecycleOwner(), result -> {
            if(result.isSuccess()){
                Log.d(TAG, "travel " + ((Result.TravelsResponseSuccess) result).getData().toString() + " added successfully");

                Intent intent = new Intent(requireActivity(), MainActivity.class);
                intent.putExtra(TRAVEL_ADDED, true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();

            } else {
                Snackbar.make(requireActivity().findViewById(android.R.id.content),
                        ((Result.Error)result).getMessage(),
                        Snackbar.LENGTH_SHORT).show();
                Log.d(TAG, "Error while adding travel: " + ((Result.Error)result).getMessage());
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}