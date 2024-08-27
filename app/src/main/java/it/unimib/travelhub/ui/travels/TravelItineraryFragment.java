package it.unimib.travelhub.ui.travels;

import static it.unimib.travelhub.util.Constants.TRAVEL_UPDATED;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.PopupMenu;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;

import it.unimib.travelhub.R;
import it.unimib.travelhub.adapter.TravelSegmentRecyclerAdapter;
import it.unimib.travelhub.data.repository.travels.ITravelsRepository;
import it.unimib.travelhub.databinding.FragmentTravelItineraryBinding;
import it.unimib.travelhub.model.Result;
import it.unimib.travelhub.model.TravelSegment;
import it.unimib.travelhub.model.Travels;
import it.unimib.travelhub.util.ServiceLocator;

public class TravelItineraryFragment extends Fragment {

    private static final String TAG = TravelItineraryFragment.class.getSimpleName();
    private FragmentTravelItineraryBinding binding;
    protected RecyclerView.LayoutManager travelLayoutManager;
    private static final String TRAVEL = "travel";
    private static Travels travel;
    private TravelsViewModel travelsViewModel;
    final Calendar myCalendar= Calendar.getInstance();

    public TravelItineraryFragment(Travels travel) {
        TravelItineraryFragment.travel = travel;
        // Required empty public constructor
    }

    public static TravelItineraryFragment newInstance(Travels travel) {
        TravelItineraryFragment fragment = new TravelItineraryFragment(travel);
        Bundle args = new Bundle();
        args.putSerializable(TRAVEL, travel);
        fragment.setArguments(args);
        return fragment;
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

        if (getArguments() != null) {
            travel = (Travels) getArguments().getSerializable(TRAVEL);
        }

        Places.initialize(requireContext(), "AIzaSyCFJYe15Sn6wp0A8yYWl3qv8t5pHsxaYUU");
        @SuppressWarnings("unused")
        PlacesClient placesClient = Places.createClient(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTravelItineraryBinding.inflate(inflater, container, false);

        if(((TravelActivity) requireActivity()).isTravelCreator){
            binding.buttonAddSegment.setVisibility(View.VISIBLE);
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {


        super.onViewCreated(view, savedInstanceState);

        RecyclerView travelSegmentsRecyclerView = binding.segmentsRecyclerView;
        travelLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        TravelSegmentRecyclerAdapter travelSegmentRecyclerAdapter = new TravelSegmentRecyclerAdapter(
                (travelSegment, seg_more) -> {
                    if (getContext() == null) {
                        Log.e(TAG, "Context is null");
                        return;
                    }
                    PopupMenu popupMenu = new PopupMenu(getContext(), seg_more);
                    popupMenu.getMenuInflater().inflate(R.menu.edit_travel_segment, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(item -> {
                        // Toast message on menu item clicked
                        if (item.getItemId() == R.id.delete_segment) {
                            delete_travel_segment(travelSegment);
                        }
                        return true;
                    });
                    popupMenu.show();

                }, travel.getDestinations(), travel);
        travelSegmentsRecyclerView.setLayoutManager(travelLayoutManager);
        travelSegmentsRecyclerView.setAdapter(travelSegmentRecyclerAdapter);
        binding.buttonAddSegment.setOnClickListener(viewAdd -> addSegmentHandler());

    }



    private void addSegmentHandler(){
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
            @SuppressLint("InflateParams") View view1 = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_layout_new_segment, null);
            bottomSheetDialog.setContentView(view1);
            bottomSheetDialog.show();

            TravelSegment travelSegment = new TravelSegment();

            TextInputLayout seg_date_from = view1.findViewById(R.id.seg_date_from_text_field);
            TextInputLayout seg_date_to = view1.findViewById(R.id.seg_date_to_text_field);

            DatePickerDialog datePickerDialogFromDate = new DatePickerDialog(requireContext());
            datePickerDialogFromDate.getDatePicker().setMinDate(travel.getStartDate().getTime());
            datePickerDialogFromDate.getDatePicker().setMaxDate(travel.getEndDate().getTime());

            datePickerDialogFromDate.setOnDateSetListener((view2, year, month, dayOfMonth) -> {
                myCalendar.set(Calendar.YEAR,year);
                myCalendar.set(Calendar.MONTH,month);
                myCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                String date = updateLabel(Objects.requireNonNull(seg_date_from.getEditText()));
                travelSegment.setDateFrom(parseStringToDate(date + " 00:00:00"));
            });

            DatePickerDialog datePickerDialogToDate = new DatePickerDialog(requireContext());
            datePickerDialogToDate.getDatePicker().setMinDate(travel.getStartDate().getTime());
            datePickerDialogToDate.getDatePicker().setMaxDate(travel.getEndDate().getTime());

            datePickerDialogToDate.setOnDateSetListener((view2, year, month, dayOfMonth) -> {
                myCalendar.set(Calendar.YEAR,year);
                myCalendar.set(Calendar.MONTH,month);
                myCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                String date = updateLabel(Objects.requireNonNull(seg_date_to.getEditText()));
                travelSegment.setDateTo(parseStringToDate(date + " 23:59:59"));
            });

            Objects.requireNonNull(seg_date_from.getEditText()).setOnClickListener(v ->
                    datePickerDialogFromDate.show());

            Objects.requireNonNull(seg_date_to.getEditText()).setOnClickListener(v ->
                    datePickerDialogToDate.show());
            TextInputLayout seg_location = view1.findViewById(R.id.seg_location_text_field);
            TextInputLayout seg_description = view1.findViewById(R.id.seg_description_text_field);

            AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                    requireActivity().getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
            assert autocompleteFragment != null;
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onError(@NonNull Status status) {
                    Log.d("TravelItineraryFragment", "An error occurred: " + status);
                }

                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    Log.d("TravelItineraryFragment", "Place: " + place.getLatLng());
                    Objects.requireNonNull(seg_location.getEditText()).setText(place.getName());
                    travelSegment.setLatLng(Objects.requireNonNull(place.getLatLng()).latitude, place.getLatLng().longitude);
                }
            });

            MaterialButton createButton = view1.findViewById(R.id.button_create_segment);
            createButton.setOnClickListener(view22 -> {
                travelSegment.setLocation(Objects.requireNonNull(seg_location.getEditText()).getText().toString());
                travelSegment.setDescription(Objects.requireNonNull(seg_description.getEditText()).getText().toString());

                if (travelSegment.getDateFrom() == null || travelSegment.getDateTo() == null || travelSegment.getLocation().isEmpty() || travelSegment.getLat() == 0 || travelSegment.getLng() == 0 ) {
                    seg_date_from.setError(getString(R.string.select_start_date_error));
                    seg_date_to.setError(getString(R.string.select_end_date_error));
                    seg_location.setError(getString(R.string.select_location_error));
                    return;
                }
                travel.getDestinations().add(travelSegment);
                Collections.sort(travel.getDestinations());
                updateTravel(travel);
                bottomSheetDialog.dismiss();
            });

    }
    public void delete_travel_segment(TravelSegment travelSegment) {
        travel.getDestinations().remove(travelSegment);
        Collections.sort(travel.getDestinations());
        updateTravel(travel);
    }
    private Date parseStringToDate(String date){
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", requireContext().getResources().getConfiguration().getLocales().get(0));
        Date parsedDate = null;
        try {
            parsedDate = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return parsedDate;
    }
    private String updateLabel(EditText editText) {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, requireContext().getResources().getConfiguration().getLocales().get(0));
        String s = sdf.format(myCalendar.getTime());
        editText.setText(s);
        return s;
    }
    public void updateTravel(Travels travel) {
        travelsViewModel.updateTravel(travel, TravelActivity.oldTravel);
        travelsViewModel.getUpdateTravelsMutableLiveData().observe(getViewLifecycleOwner(), result -> {
            if (result != null && result.isSuccess()) {
                Result.TravelsResponseSuccess travelResponse = (Result.TravelsResponseSuccess) result;
                Travels travelUpdated = travelResponse.getData().getTravelsList().get(0);
                Log.d(TAG, "Travel updated: " + travelUpdated);
                    Intent intent = new Intent(requireActivity(), TravelActivity.class);
                    intent.putExtra("travel", travelUpdated);
                    intent.putExtra(TRAVEL_UPDATED, true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

            } else {
                Result.Error error = (Result.Error) result;
                Log.d(TAG, "Travel not updated, Error: " + error.getMessage());
            }
        });

    }
}