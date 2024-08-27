package it.unimib.travelhub.ui.profile;

import static it.unimib.travelhub.util.Constants.ENCRYPTED_SHARED_PREFERENCES_FILE_NAME;
import static it.unimib.travelhub.util.Constants.LAST_UPDATE;
import static it.unimib.travelhub.util.Constants.PICS_FOLDER;
import static it.unimib.travelhub.util.Constants.PROFILE_PICTURE_FILE_NAME;
import static it.unimib.travelhub.util.Constants.SHARED_PREFERENCES_FILE_NAME;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.io.File;

import it.unimib.travelhub.R;
import it.unimib.travelhub.crypto_util.DataEncryptionUtil;
import it.unimib.travelhub.data.repository.travels.ITravelsRepository;
import it.unimib.travelhub.databinding.FragmentProfileBinding;
import it.unimib.travelhub.model.Result;
import it.unimib.travelhub.model.Travels;
import it.unimib.travelhub.model.TravelsResponse;
import it.unimib.travelhub.ui.travels.TravelsViewModel;
import it.unimib.travelhub.ui.travels.TravelsViewModelFactory;
import it.unimib.travelhub.util.ServiceLocator;
import it.unimib.travelhub.util.SharedPreferencesUtil;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    private final String TAG = ProfileFragment.class.getSimpleName();
    private String name, surname, username;
    private FragmentProfileBinding binding;
    private DataEncryptionUtil dataEncryptionUtil;
    private SharedPreferencesUtil sharedPreferencesUtil;
    private TravelsResponse travelsResponse;
    private TravelsViewModel travelsViewModel;
    private ActivityResultLauncher<Intent> profileUpdateLauncher;

    public ProfileFragment() {
        // Required empty public constructor
    }
    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataEncryptionUtil = new DataEncryptionUtil(requireActivity().getApplication());
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
        if (sharedPreferencesUtil == null) {
            sharedPreferencesUtil = new SharedPreferencesUtil(requireActivity().getApplication());
        }

        profileUpdateLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == SettingsActivity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getBooleanExtra("profile_updated", false)) {
                            setProfileView();
                        }
                    }
                }
        );

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        setProfileView();
        return binding.getRoot();
    }

    private void setProfileView() {
        try {
            username = dataEncryptionUtil.
                    readSecretDataWithEncryptedSharedPreferences(ENCRYPTED_SHARED_PREFERENCES_FILE_NAME, "username");
            name = dataEncryptionUtil.
                    readSecretDataWithEncryptedSharedPreferences(ENCRYPTED_SHARED_PREFERENCES_FILE_NAME, "user_name");
            surname = dataEncryptionUtil.
                    readSecretDataWithEncryptedSharedPreferences(ENCRYPTED_SHARED_PREFERENCES_FILE_NAME, "user_surname");
        } catch (Exception e) {
            String TAG = "ProfileFragment";
            Log.e(TAG, "Error while reading data from encrypted shared preferences", e);
        }
        if (getContext() != null) {
            String profileImagePath = getContext().getFilesDir() + PICS_FOLDER + PROFILE_PICTURE_FILE_NAME;
            try {
                File file = new File(profileImagePath);
                if (file.exists()) {
                    Uri imageUri = Uri.fromFile(file);
                    binding.imageViewUsername.setImageURI(imageUri);
                } else {
                    Log.d(TAG, "File does not exist");
                }
            } catch (Exception e) {
                Log.d(TAG, "Error while reading profile image", e);
            }
        }
        String atUsername = "@" + username;
        binding.textViewUsername.setText(atUsername);
        binding.textViewName.setText(name);
        binding.textViewSurname.setText(surname);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String lastUpdate = "0";
        if (sharedPreferencesUtil.readStringData(SHARED_PREFERENCES_FILE_NAME, LAST_UPDATE) != null) {
            lastUpdate = sharedPreferencesUtil.readStringData(SHARED_PREFERENCES_FILE_NAME, LAST_UPDATE);
        }

        //List<Travels> runningTravelsList = getOngoingTravelsListWithGSon();

        travelsViewModel.getTravels(Long.parseLong(lastUpdate)).observe(getViewLifecycleOwner(),
                result -> {
                    if (result.isSuccess()) {
                        travelsResponse = ((Result.TravelsResponseSuccess) result).getData();
                        binding.textViewTravelNumber.setText(String.valueOf(travelsResponse.getTravelsList().size()));
                        int totDestinations = 0;
                        for(Travels travel: travelsResponse.getTravelsList()){
                            totDestinations += (travel.getDestinations().size() - 1);
                        }
                        binding.textViewDestinationsNumber.setText(String.valueOf(totDestinations));

                        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                            @Override
                            public void onTabSelected(TabLayout.Tab tab) {
                                binding.viewPagerProfile.setCurrentItem(tab.getPosition());
                            }
                            @Override
                            public void onTabUnselected(TabLayout.Tab tab) {
                            }
                            @Override
                            public void onTabReselected(TabLayout.Tab tab) {
                            }
                        });
                        FragmentManager fragmentManager = getParentFragmentManager();
                        ProfileFragmentAdapter myFragmentAdapter = new ProfileFragmentAdapter(fragmentManager, getLifecycle(), travelsResponse);
                        binding.viewPagerProfile.setAdapter(myFragmentAdapter);

                    } else {
                        binding.textViewTravelNumber.setText("0");
                        binding.textViewDestinationsNumber.setText("0");
                    }
                });

        binding.settingsButton.setOnClickListener(view1 -> {
            Intent intent = new Intent(getContext(), SettingsActivity.class);
            profileUpdateLauncher.launch(intent);
        });

    }
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}