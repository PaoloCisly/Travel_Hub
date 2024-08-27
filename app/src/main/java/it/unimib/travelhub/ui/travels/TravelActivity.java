package it.unimib.travelhub.ui.travels;

import static it.unimib.travelhub.util.Constants.ENCRYPTED_SHARED_PREFERENCES_FILE_NAME;
import static it.unimib.travelhub.util.Constants.ID_TOKEN;
import static it.unimib.travelhub.util.Constants.TRAVEL_DELETED;
import static it.unimib.travelhub.util.Constants.TRAVEL_UPDATED;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import it.unimib.travelhub.R;
import it.unimib.travelhub.crypto_util.DataEncryptionUtil;
import it.unimib.travelhub.data.repository.travels.ITravelsRepository;
import it.unimib.travelhub.databinding.ActivityTravelBinding;
import it.unimib.travelhub.model.Result;
import it.unimib.travelhub.model.TravelMember;
import it.unimib.travelhub.model.TravelSegment;
import it.unimib.travelhub.model.Travels;
import it.unimib.travelhub.ui.main.MainActivity;
import it.unimib.travelhub.util.ServiceLocator;

public class TravelActivity extends AppCompatActivity {

    private static final String TAG = TravelActivity.class.getSimpleName();
    private boolean isTravelUpdated;
    private TravelsViewModel travelsViewModel;
    private ActivityTravelBinding binding;
    private Travels travel;
    public static Travels oldTravel;
    public boolean enableEdit;
    public boolean isTravelCreator;
    final Calendar myCalendar= Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityTravelBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        TravelActivityArgs args = TravelActivityArgs.fromBundle(getIntent().getExtras());

        if (getIntent().getBooleanExtra(TRAVEL_UPDATED, false)) {
            Snackbar.make(TravelActivity.this.findViewById(android.R.id.content),
                    R.string.travel_updated_success_msg, Snackbar.LENGTH_SHORT).show();
        }

        travel = args.getTravel();
        Log.d(TAG, "Intent members: " + travel.getMembers());
        List<TravelMember> members = new ArrayList<>(travel.getMembers());
        List<TravelSegment> destinations = new ArrayList<>(travel.getDestinations());
        oldTravel = new Travels(travel.getId(), travel.getTitle(), travel.getDescription(), travel.getStartDate(), travel.getEndDate(), members, destinations);


        isTravelCreator = isUserCreator();

        if(isTravelCreator){
            binding.buttonMore.setVisibility(View.VISIBLE);
        }

        isTravelUpdated = false;
        enableEdit = false;

        ITravelsRepository travelsRepository =
                ServiceLocator.getInstance().getTravelsRepository(
                        TravelActivity.this.getApplication()
                );
        if (travelsRepository != null) {
            travelsViewModel = new ViewModelProvider(
                    TravelActivity.this,
                    new TravelsViewModelFactory(travelsRepository)).get(TravelsViewModel.class);
        } else {
            Snackbar.make(TravelActivity.this.findViewById(android.R.id.content),
                    getString(R.string.unexpected_error), Snackbar.LENGTH_SHORT).show();
        }

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.viewPagerTravel.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        binding.viewPagerTravel.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position));
            }
        });

        FragmentManager fragmentManager = getSupportFragmentManager();
        TravelFragmentAdapter myFragmentAdapter = new TravelFragmentAdapter(fragmentManager, getLifecycle(), travel);
        binding.viewPagerTravel.setAdapter(myFragmentAdapter);
        binding.travelTitle.setText(travel.getTitle());

        binding.buttonBack.setOnClickListener(v -> {
                    if(isTravelUpdated){
                        handle_back();
                    } else {
                        Intent intent = new Intent(TravelActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
        });

        String startDate = new SimpleDateFormat("dd/MM/yyyy", getResources().getConfiguration().getLocales().get(0))
                .format(travel.getStartDate());
        String endDate = new SimpleDateFormat("dd/MM/yyyy", getResources().getConfiguration().getLocales().get(0))
                .format(travel.getEndDate());
        this.getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(isTravelUpdated){
                    handle_back();
                } else {
                    Intent intent = new Intent(TravelActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        });

        binding.travelStartDate.setText(startDate);
        binding.travelEndDate.setText(endDate);
        binding.buttonMore.setOnClickListener(viewMore -> {
            if (isTravelCreator){
                buttonMoreHandler();
            }
        });
    }

    private void editTravel() {

        TextInputEditText travelTitle = findViewById(R.id.travel_title);
        EditText travelStartDate = findViewById(R.id.travel_start_date);
        EditText travelEndDate = findViewById(R.id.travel_end_date);

        travelTitle.setOnLongClickListener(v -> {
            travelTitle.setFocusableInTouchMode(true);
            travelTitle.setFocusable(true);
            travelTitle.requestFocus();
            travelTitle.setOnFocusChangeListener((v1, hasFocus) -> {
                if (!hasFocus) {
                    travelTitle.setFocusable(false);
                }
            });
            return true;
        });
        travelTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                travel.setTitle(s.toString());
                showEditButton();
            }
            @Override
            public void afterTextChanged(Editable s) {
                travelTitle.setFocusable(false);
            }
        });

        DatePickerDialog.OnDateSetListener date1 = (v, year, month, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR,year);
            myCalendar.set(Calendar.MONTH,month);
            myCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
            String date = updateLabel(binding.travelStartDate);
            travel.setStartDate(parseStringToDate(date + " 00:00:00"));
        };

        DatePickerDialog.OnDateSetListener date2 = (v, year, month, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR,year);
            myCalendar.set(Calendar.MONTH,month);
            myCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
            String date = updateLabel(binding.travelEndDate);
            travel.setEndDate(parseStringToDate(date + " 23:59:59"));
        };

        travelStartDate.setOnLongClickListener(v ->
        {
            new DatePickerDialog(this, date1 ,myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            showEditButton();
            return true;
        });

        travelEndDate.setOnLongClickListener(v ->
        {
            new DatePickerDialog(this, date2,myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            showEditButton();
            return true;
        });

    }
    private void deleteTravel(Travels travel) {

        Observer<Result> resultObserver = new Observer<Result>() {
            @Override
            public void onChanged(Result result) {
                if (result != null && result.isSuccess()) {
                    Result.TravelsResponseSuccess travelResponse = (Result.TravelsResponseSuccess) result;
                    Travels travelDeleted = travelResponse.getData().getTravelsList().get(0);
                    Log.d(TAG, "Travel deleted: " + travelDeleted);
                } else {
                    Result.Error error = (Result.Error) result;
                    Log.d(TAG, "Travel not deleted, Error: " + error.getMessage());
                }
                travelsViewModel.deleteTravel(travel).removeObserver(this);
            }
        };

        travelsViewModel.deleteTravel(travel).observe(this, resultObserver);

    }
    public void showEditButton(){
        isTravelUpdated = true;
        binding.buttonEdit.setVisibility(View.VISIBLE);
        binding.buttonEdit.setOnClickListener(v -> updateTravel(travel));
    }
    private Date parseStringToDate(String date){
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", getResources().getConfiguration().getLocales().get(0));
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
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, getResources().getConfiguration().getLocales().get(0));
        String s = sdf.format(myCalendar.getTime());
        editText.setText(s);
        return s;
    }
    public void updateTravel(Travels travel) {
        travelsViewModel.updateTravel(travel, oldTravel);

        travelsViewModel.getUpdateTravelsMutableLiveData().observe(this, result -> {
            if (result != null && result.isSuccess()) {
                Result.TravelsResponseSuccess travelResponse = (Result.TravelsResponseSuccess) result;
                Travels travelUpdated = travelResponse.getData().getTravelsList().get(0);
                Log.d(TAG, "Travel updated: " + travelUpdated);
                Intent intent = new Intent(TravelActivity.this, TravelActivity.class);
                intent.putExtra("travel", travelUpdated);
                intent.putExtra(TRAVEL_UPDATED, true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Result.Error error = (Result.Error) result;
                Log.d(TAG, "Travel not updated, Error: " + error.getMessage());
            }
        });

    }
    public void handle_back() {
        AlertDialog.Builder AlertBuilder = new AlertDialog.Builder(this);
        AlertBuilder.setMessage("Are you sure you want to come back? Your updates will be lost.");
        AlertBuilder.setCancelable(true);

        AlertBuilder.setPositiveButton(
                "Yes",
                (dialog, id) -> {
                    dialog.cancel();
                    Intent intent = new Intent(TravelActivity.this, MainActivity.class);
                    startActivity(intent);
                });

        AlertBuilder.setNegativeButton(
                "No",
                (dialog, id) -> dialog.cancel());

        AlertDialog alert = AlertBuilder.create();
        alert.show();
    }
    private void buttonMoreHandler(){

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(TravelActivity.this);
        @SuppressLint("InflateParams") View view1 = LayoutInflater.from(TravelActivity.this).inflate(R.layout.bottom_sheet_layout_travel, null);
        bottomSheetDialog.setContentView(view1);
        bottomSheetDialog.show();
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) MaterialButton buttonDelete = view1.findViewById(R.id.button_delete);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) MaterialButton buttonEdit = view1.findViewById(R.id.button_edit);

        AlertDialog.Builder builder = new AlertDialog.Builder(TravelActivity.this);

        buttonEdit.setOnClickListener(view2 -> {
            if (!isTravelCreator)
                return;
            builder.setMessage(R.string.edit_dialog_msg);
            builder.setCancelable(true);
            builder.setPositiveButton(
                    "Ok",
                    (dialog, id) -> dialog.cancel());
            AlertDialog alert = builder.create();
            alert.show();
            enableEdit = true;
            bottomSheetDialog.dismiss();
            editTravel();
        });

        buttonDelete.setOnClickListener(view2 -> {
            if (!isTravelCreator)
                return;
            builder.setMessage(R.string.delete_travel_dialog_msg);
            builder.setCancelable(true);

            builder.setPositiveButton(
                    "Yes",
                    (dialog, id) -> {
                        deleteTravel(travel);
                        Intent intent = new Intent(TravelActivity.this, MainActivity.class);
                        intent.putExtra(TRAVEL_DELETED, true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    });

            builder.setNegativeButton(
                    "No",
                    (dialog, id) -> dialog.cancel());

            AlertDialog alert11 = builder.create();
            alert11.show();
        });
    }
    private boolean isUserCreator() {
        DataEncryptionUtil dataEncryptionUtil = new DataEncryptionUtil(this.getApplication());
        String id;
        try{
            id = dataEncryptionUtil.readSecretDataWithEncryptedSharedPreferences(ENCRYPTED_SHARED_PREFERENCES_FILE_NAME, ID_TOKEN);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return false;
        }
        for(TravelMember member : travel.getMembers()) {
            if(member.getRole() == TravelMember.Role.CREATOR && member.getIdToken().equals(id)) {
                return true;
            }
        }
        return false;
    }
}