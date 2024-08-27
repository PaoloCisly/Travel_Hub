package it.unimib.travelhub.ui.travels;

import static it.unimib.travelhub.util.Constants.DESTINATIONS_HINTS;
import static it.unimib.travelhub.util.Constants.DESTINATIONS_TEXTS;
import static it.unimib.travelhub.util.Constants.ENCRYPTED_SHARED_PREFERENCES_FILE_NAME;
import static it.unimib.travelhub.util.Constants.FRIENDS_HINTS;
import static it.unimib.travelhub.util.Constants.FRIENDS_TEXTS;
import static it.unimib.travelhub.util.Constants.ID_TOKEN;
import static it.unimib.travelhub.util.Constants.USERNAME;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.PopupMenu;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import it.unimib.travelhub.R;
import it.unimib.travelhub.adapter.TextBoxesRecyclerAdapter;
import it.unimib.travelhub.adapter.UsersRecyclerAdapter;
import it.unimib.travelhub.crypto_util.DataEncryptionUtil;
import it.unimib.travelhub.data.repository.travels.ITravelsRepository;
import it.unimib.travelhub.data.repository.user.IUserRepository;
import it.unimib.travelhub.databinding.FragmentEditTravelBinding;
import it.unimib.travelhub.model.Result;
import it.unimib.travelhub.model.TravelMember;
import it.unimib.travelhub.model.TravelSegment;
import it.unimib.travelhub.model.Travels;
import it.unimib.travelhub.model.User;
import it.unimib.travelhub.ui.welcome.UserViewModel;
import it.unimib.travelhub.ui.welcome.UserViewModelFactory;
import it.unimib.travelhub.util.ServiceLocator;

public class NewTravelFragment extends Fragment {
    private FragmentEditTravelBinding binding;
    private TextBoxesRecyclerAdapter textBoxesRecyclerAdapter;
    private static final String TAG = NewTravelFragment.class.getSimpleName();
    final Calendar myCalendar= Calendar.getInstance();
    private List<String> friendTextList;
    private List<String> destinationsText;
    private List<String> hintsList;
    private List<String> friendHintsList;
    private Activity mainActivity;
    private IUserRepository userRepository;
    private DataEncryptionUtil dataEncryptionUtil;
    private List<TravelMember> memberList;
    private UserViewModel userViewModel;

    public static NewTravelFragment newInstance() {
        NewTravelFragment fragment = new NewTravelFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            destinationsText = savedInstanceState.getStringArrayList(DESTINATIONS_TEXTS);
            friendTextList = savedInstanceState.getStringArrayList(FRIENDS_TEXTS);
            hintsList = savedInstanceState.getStringArrayList(DESTINATIONS_HINTS);
            friendHintsList = savedInstanceState.getStringArrayList(FRIENDS_HINTS);
            //binding.titleFormEditText.setText(savedInstanceState.getString(TRAVEL_TITLE));
            //binding.descriptionFormEditText.setText(savedInstanceState.getString(TRAVEL_DESCRIPTION));
        } else {
            destinationsText = new ArrayList<>();
            friendTextList = new ArrayList<>();
            hintsList = new ArrayList<>();
            friendHintsList = new ArrayList<>();
        }
        dataEncryptionUtil = new DataEncryptionUtil(requireActivity().getApplication());

        ITravelsRepository travelsRepository =
                ServiceLocator.getInstance().getTravelsRepository(
                        requireActivity().getApplication()
                );

        if (travelsRepository == null) {
            Snackbar.make(requireActivity().findViewById(android.R.id.content),
                    getString(R.string.unexpected_error), Snackbar.LENGTH_SHORT).show();
        }

        userRepository = ServiceLocator.getInstance().getUserRepository();

        if (userRepository != null) {
            userViewModel = new ViewModelProvider(
                    requireActivity(),
                    new UserViewModelFactory(userRepository)).get(UserViewModel.class);
        } else {
            Snackbar.make(requireActivity().findViewById(android.R.id.content),
                    getString(R.string.unexpected_error), Snackbar.LENGTH_SHORT).show();
        }

        Places.initialize(requireContext(), "AIzaSyCFJYe15Sn6wp0A8yYWl3qv8t5pHsxaYUU");
        @SuppressWarnings("unused")
        PlacesClient placesClient = Places.createClient(requireContext());
        memberList = new ArrayList<>();
        TravelMember creator = new TravelMember(TravelMember.Role.CREATOR);
        creator.setUsername(getLoggedUsername());
        creator.setIdToken(getLoggedIdToken());
        memberList.add(creator);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditTravelBinding.inflate(inflater, container, false);
        mainActivity = requireActivity();

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                AlertDialog.Builder AlertBuilder = new AlertDialog.Builder(requireActivity());
                AlertBuilder.setMessage("Are you sure you want to come back? Your new travel will be lost.");
                AlertBuilder.setCancelable(true);

                AlertBuilder.setPositiveButton(
                        "Yes",
                        (dialog, id) -> {
                            dialog.cancel();
                            requireActivity().finish();
                        });

                AlertBuilder.setNegativeButton(
                        "No",
                        (dialog, id) -> dialog.cancel());

                AlertDialog alert = AlertBuilder.create();
                alert.show();
            }
        });

        return binding.getRoot();
    }

    private void updateLabel(EditText editText) {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, getResources().getConfiguration().getLocales().get(0));
        editText.setText(sdf.format(myCalendar.getTime()));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        DatePickerDialog.OnDateSetListener date1 = (v, year, month, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR,year);
            myCalendar.set(Calendar.MONTH,month);
            myCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
            updateLabel(binding.editTxtFromForm);
        };
        DatePickerDialog.OnDateSetListener date2 = (v, year, month, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR,year);
            myCalendar.set(Calendar.MONTH,month);
            myCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
            updateLabel(binding.editTxtToForm);
        };
        binding.editTxtFromForm.setOnClickListener(v ->
        {
            if (getContext() != null)
                new DatePickerDialog(
                        getContext(), date1,
                        Objects.requireNonNull(binding.editTxtFromForm.getText()).toString().isEmpty() ?
                                myCalendar.get(Calendar.YEAR) :
                                Integer.parseInt(binding.editTxtFromForm.getText().toString().split("/")[2]),
                        Objects.requireNonNull(binding.editTxtFromForm.getText()).toString().isEmpty() ?
                                myCalendar.get(Calendar.MONTH) :
                                Integer.parseInt(binding.editTxtFromForm.getText().toString().split("/")[1]) - 1,
                        Objects.requireNonNull(binding.editTxtFromForm.getText()).toString().isEmpty() ?
                                myCalendar.get(Calendar.DAY_OF_MONTH) :
                                Integer.parseInt(binding.editTxtFromForm.getText().toString().split("/")[0])).show();
        });

        binding.editTxtToForm.setOnClickListener(v ->
        {
            if (getContext() != null)
                new DatePickerDialog(
                        getContext(), date2,
                        Objects.requireNonNull(binding.editTxtToForm.getText()).toString().isEmpty() ?
                                (Objects.requireNonNull(binding.editTxtFromForm.getText()).toString().isEmpty() ?
                                        myCalendar.get(Calendar.YEAR) :
                                        Integer.parseInt(binding.editTxtFromForm.getText().toString().split("/")[2])) :
                                Integer.parseInt(binding.editTxtToForm.getText().toString().split("/")[2]),
                        Objects.requireNonNull(binding.editTxtToForm.getText()).toString().isEmpty() ?
                                (Objects.requireNonNull(binding.editTxtFromForm.getText()).toString().isEmpty() ?
                                        myCalendar.get(Calendar.MONTH) :
                                        Integer.parseInt(binding.editTxtFromForm.getText().toString().split("/")[1]) - 1) :
                                Integer.parseInt(binding.editTxtToForm.getText().toString().split("/")[1]) - 1,
                        Objects.requireNonNull(binding.editTxtToForm.getText()).toString().isEmpty() ?
                                (Objects.requireNonNull(binding.editTxtFromForm.getText()).toString().isEmpty() ?
                                        myCalendar.get(Calendar.DAY_OF_MONTH) :
                                        Integer.parseInt(binding.editTxtFromForm.getText().toString().split("/")[0])) :
                                Integer.parseInt(binding.editTxtToForm.getText().toString().split("/")[0])).show();
        });

        textBoxesRecyclerAdapter = new TextBoxesRecyclerAdapter(hintsList, destinationsText,new TextBoxesRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                int actualItems = textBoxesRecyclerAdapter.getItemCount();
                if (actualItems > 0){
                    removeItem(textBoxesRecyclerAdapter,position);
                }
            }
            @Override
            public void onKeyPressed(int position, String text) {
                textBoxesRecyclerAdapter.getDestinationsTexts().set(position, text);
                destinationsText = textBoxesRecyclerAdapter.getDestinationsTexts(); // Update the list immediately
            }
        });

        LinearLayoutManager mLayoutManager;

       /* binding.recyclerDestinations.setLayoutManager(mLayoutManager);
        binding.recyclerDestinations.setAdapter(textBoxesRecyclerAdapter);

        binding.addDestinationButton.setOnClickListener(v -> {
            updateItem(textBoxesRecyclerAdapter, R.string.destination);
        });*/

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        binding.addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                @SuppressLint("InflateParams") View view1 = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_layout_add_participant, null);
                bottomSheetDialog.setContentView(view1);
                bottomSheetDialog.show();

                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button add_participant = view1.findViewById(R.id.button_add_partecipant);
                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextInputLayout username = view1.findViewById(R.id.username_text_field);

                add_participant.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (Objects.requireNonNull(username.getEditText()).getText() == null || username.getEditText().getText().toString().isEmpty()) {
                            username.setError(getString(R.string.error_empty_username));
                        }else{
                            username.setError(null);
                            userViewModel.isUsernameAlreadyTaken(username.getEditText().getText().toString());
                            Observer<Result> observer = new Observer<Result>(){
                                @SuppressLint("NotifyDataSetChanged")
                                @Override
                                public void onChanged(Result result) {
                                    if (result instanceof Result.Error) {
                                        Log.d(TAG, ((Result.Error) result).getMessage());
                                        username.setError(((Result.Error) result).getMessage());
                                    } else {
                                        User user = ((Result.UserResponseSuccess) result).getData();
                                        Log.d(TAG, "User found: " + user);
                                        if (user != null) {
                                            memberList.add(new TravelMember(user.getUsername(), user.getIdToken(), TravelMember.Role.MEMBER));
                                            Objects.requireNonNull(binding.friendsRecyclerView.getAdapter()).notifyDataSetChanged();
                                            Log.d(TAG, "Member list: " + memberList);
                                            bottomSheetDialog.dismiss();
                                        } else {
                                            username.setError(getString(R.string.error_empty_username));
                                        }
                                    }
                                    userViewModel.getIsUsernameAlreadyTaken().removeObserver(this);
                                }
                            };

                            userViewModel.getIsUsernameAlreadyTaken().observe(getViewLifecycleOwner(), observer);

                        }
                    }

                });
            }


        });

        RecyclerView recyclerView = binding.friendsRecyclerView;
        mLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        UsersRecyclerAdapter usersRecyclerAdapter = new UsersRecyclerAdapter(memberList, 1, requireActivity(),
                (travelMember, seg_long_button) -> {
                    if (getContext() == null) {
                        return;
                    }
                    PopupMenu popupMenu = new PopupMenu(getContext(), seg_long_button);
                    popupMenu.getMenuInflater().inflate(R.menu.edit_travel_member, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(item -> {
                        // Toast message on menu item clicked
                        if (item.getItemId() == R.id.delete_member) {
                            removeUser(travelMember);
                        }
                        return true;
                    });
                    popupMenu.show();
                }, userRepository);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(usersRecyclerAdapter);




//        friendTextBoxesRecyclerAdapter = new TextBoxesRecyclerAdapter(friendHintsList, friendTextList, new TextBoxesRecyclerAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(int position) {
//                int actualItems = friendTextBoxesRecyclerAdapter.getItemCount();
//                if (actualItems > 0){
//                    removeItem(friendTextBoxesRecyclerAdapter,position);
//                }
//            }
//            @Override
//            public void onKeyPressed(int position, String text) {
//                friendTextBoxesRecyclerAdapter.getDestinationsTexts().set(position, text);
//                friendTextList = friendTextBoxesRecyclerAdapter.getDestinationsTexts(); // Update the list immediately
//            }
//        });
//        LinearLayoutManager friendLayoutManager =
//                new LinearLayoutManager(requireContext(),
//                        LinearLayoutManager.VERTICAL, false);
//
//        binding.recyclerFriends.setLayoutManager(friendLayoutManager);
//        binding.recyclerFriends.setAdapter(friendTextBoxesRecyclerAdapter);
//        binding.addFriendButton.setOnClickListener(v -> {
//            updateItem(friendTextBoxesRecyclerAdapter, R.string.add_friends_username);
//        });

        //make the save button non clickable and make it appear it is not clickable
        mainActivity.findViewById(R.id.button_save_activity).setClickable(false);
        mainActivity.findViewById(R.id.button_save_activity).setAlpha(0.5f);

        binding.addDestinationButton.setOnClickListener(v -> {
            if(checkNullValues()){
                return;
            }
            goToNewFragment(buildTravel());
        });

        binding.showAutocompleteLocatorLayout.setOnClickListener(v -> {
            Log.d("TravelItineraryFragment", "autocompleteNewLayout clicked");
            binding.autocompleteNewLayout.setVisibility(View.VISIBLE);
            binding.autocompleteNewLayout.setOnClickListener(v1 -> binding.autocompleteNewLayout.setVisibility(View.GONE));
        });

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(view.findViewById(R.id.autocomplete_new_fragment).getId());
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
                binding.departureFormEditText.setText(place.getName());
                binding.latitudeEditText.setText(String.valueOf(Objects.requireNonNull(place.getLatLng()).latitude));
                binding.longitudeEditText.setText(String.valueOf(place.getLatLng().longitude));
                binding.autocompleteNewLayout.setVisibility(View.GONE);
            }
        });

    }

    private void
    goToNewFragment(Travels travel){
        Bundle bundle = new Bundle();
        bundle.putSerializable("travel", travel);

        NewTravelSegment newTravelSegment = new NewTravelSegment();
        newTravelSegment.setArguments(bundle);

        // navigate to EditTravelSegment
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.activityAddFragmentContainerView, newTravelSegment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
    @SuppressLint("NotifyDataSetChanged")
    private void removeUser(TravelMember travelMember){
        if (travelMember.getRole() == TravelMember.Role.CREATOR){
            Snackbar.make(requireActivity().findViewById(android.R.id.content),
                    getString(R.string.cannot_remove_creator), Snackbar.LENGTH_SHORT).show();
            return;
        }
        memberList.remove(travelMember);
        Objects.requireNonNull(binding.friendsRecyclerView.getAdapter()).notifyDataSetChanged();
    }

    public Travels buildTravel(){
        Travels travel = new Travels();

        String userId = getLoggedUsername();
        String travelId = buildTravelId(userId);
        String title = Objects.requireNonNull(binding.titleFormEditText.getText()).toString();
        String description = Objects.requireNonNull(binding.descriptionFormEditText.getText()).toString();

        String start = Objects.requireNonNull(binding.editTxtFromForm.getText()) + " 00:00:00";
        String end = Objects.requireNonNull(binding.editTxtToForm.getText()) + " 23:59:59";
        Date startDate = parseStringToDate(start);
        Date endDate = parseStringToDate(end);
        if(startDate == null || endDate == null){
            throw new RuntimeException("Error while parsing dates, impossible to build the travel");
        }

        String departure = Objects.requireNonNull(binding.departureFormEditText.getText()).toString();
        double lat = Double.parseDouble(Objects.requireNonNull(binding.latitudeEditText.getText()).toString());
        double lng = Double.parseDouble(Objects.requireNonNull(binding.longitudeEditText.getText()).toString());

        List<TravelSegment> destinations = buildDestinationsList(departure, lat, lng);
        travel.setId(Long.parseLong(travelId));
        travel.setTitle(title);
        travel.setDescription(description);
        travel.setStartDate(startDate);
        travel.setEndDate(endDate);
        travel.setDestinations(destinations);
        travel.setMembers(memberList);

        return travel;
    }

    public List <TravelSegment> buildDestinationsList(String departure, double lat, double lng){
        List<TravelSegment> destinations = new ArrayList<>();
        TravelSegment start = new TravelSegment(departure);

        start.setLatLng(lat, lng);
        start.setDateTo(parseStringToDate(Objects.requireNonNull(binding.editTxtFromForm.getText()) + " 00:00:00"));
        start.setDateFrom(parseStringToDate(Objects.requireNonNull(binding.editTxtFromForm.getText()) + " 00:00:00"));
        destinations.add(start);
        return destinations;
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

    public String buildTravelId(String userId){
        return String.valueOf((userId + System.currentTimeMillis()).hashCode());
    }
    public String getLoggedUsername(){
        String userId;
        try {
            userId = dataEncryptionUtil.readSecretDataWithEncryptedSharedPreferences(
                    ENCRYPTED_SHARED_PREFERENCES_FILE_NAME,
                    USERNAME);
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
        return userId;
    }

    public String getLoggedIdToken(){
        String userId;
        try {
            userId = dataEncryptionUtil.readSecretDataWithEncryptedSharedPreferences(
                    ENCRYPTED_SHARED_PREFERENCES_FILE_NAME,
                    ID_TOKEN);
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
        return userId;
    }

    private boolean checkNullValues() {
        boolean isNull = false;
        //mandatory fields are title, from, to, departure, destinations
        if (Objects.requireNonNull(binding.titleFormEditText.getText()).toString().isEmpty()) {
            binding.titleFormEditText.setError(getString(R.string.title_empty_error));
            isNull = true;
        }else{
            binding.titleFormEditText.setError(null);
        }
        if (Objects.requireNonNull(binding.editTxtFromForm.getText()).toString().isEmpty()) {
            binding.editTxtFromForm.setError(getString(R.string.date_empty_error));
            isNull = true;
        }else{
            binding.editTxtFromForm.setError(null);
        }
        if (Objects.requireNonNull(binding.editTxtToForm.getText()).toString().isEmpty()) {
            binding.editTxtToForm.setError(getString(R.string.date_empty_error));
            isNull = true;
        }else{
            binding.editTxtToForm.setError(null);
        }
        if (Objects.requireNonNull(binding.departureFormEditText.getText()).toString().isEmpty()) {
            binding.departureFormEditText.setError(getString(R.string.departure_empty_error));
            isNull = true;
        }else{
            binding.departureFormEditText.setError(null);
        }
        return isNull;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void removeItem(TextBoxesRecyclerAdapter adapter, int position) {
        adapter.getTextBoxesHints().remove(position);
        adapter.getDestinationsTexts().remove(position);
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        userViewModel.getIsUserRegistered().removeObservers(getViewLifecycleOwner());
    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(DESTINATIONS_TEXTS, (ArrayList<String>) destinationsText);
        outState.putStringArrayList(FRIENDS_TEXTS, (ArrayList<String>) friendTextList);
        outState.putStringArrayList(DESTINATIONS_HINTS, (ArrayList<String>) hintsList);
        outState.putStringArrayList(FRIENDS_HINTS, (ArrayList<String>) friendHintsList);
        //outState.putString(FRIEND, binding.friendsEmailFormEditText.getText().toString());
        //outState.putString(TRAVEL_TITLE, binding.titleFormEditText.getText().toString());
        //outState.putString(TRAVEL_DESCRIPTION, binding.descriptionFormEditText.getText().toString());
    }
}