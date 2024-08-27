package it.unimib.travelhub.ui.welcome;

import static it.unimib.travelhub.util.Constants.EMAIL_ADDRESS;
import static it.unimib.travelhub.util.Constants.ENCRYPTED_DATA_FILE_NAME;
import static it.unimib.travelhub.util.Constants.ENCRYPTED_SHARED_PREFERENCES_FILE_NAME;
import static it.unimib.travelhub.util.Constants.ID_TOKEN;
import static it.unimib.travelhub.util.Constants.PASSWORD;
import static it.unimib.travelhub.util.Constants.USERNAME;
import static it.unimib.travelhub.util.Constants.USERNAME_NOT_AVAILABLE;
import static it.unimib.travelhub.util.Constants.USER_COLLISION_ERROR;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Objects;

import it.unimib.travelhub.GlobalClass;
import it.unimib.travelhub.R;
import it.unimib.travelhub.crypto_util.DataEncryptionUtil;
import it.unimib.travelhub.databinding.FragmentRegisterBinding;
import it.unimib.travelhub.model.IValidator;
import it.unimib.travelhub.model.Result;
import it.unimib.travelhub.model.User;
import it.unimib.travelhub.model.ValidationResult;
import it.unimib.travelhub.util.ServiceLocator;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterFragment extends Fragment {

    private static final String TAG = RegisterFragment.class.getSimpleName();

    private FragmentRegisterBinding binding;
    private UserViewModel userViewModel;

    private DataEncryptionUtil dataEncryptionUtil;

    private IValidator myValidator;


    public RegisterFragment() {
        // Required empty public constructor
    }

    public static RegisterFragment newInstance() {
        RegisterFragment fragment = new RegisterFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        userViewModel.setAuthenticationError(false);
        myValidator = ServiceLocator.getInstance().getCredentialsValidator(GlobalClass.getContext());
        dataEncryptionUtil =  new DataEncryptionUtil(requireActivity().getApplication());

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.buttonRegister.setOnClickListener(V -> {
            String email = Objects.requireNonNull(binding.txtInputEditUser.getText()).toString();
            String password = Objects.requireNonNull(binding.txtInputEditPwd.getText()).toString();
            String username = Objects.requireNonNull(binding.txtInputEditName.getText()).toString();
            Log.d(TAG, "passing mail: " + email + " password: " + password + " username: " + username);
            if(isEmailOk(email) && isPasswordOk(password) && isUsernameOk(username)){
               // binding.registerProgressBar.setVisibility(View.VISIBLE);
                if (userViewModel.isAuthenticationSuccess()) {
                    userViewModel.getUserMutableLiveData(username, email, password, false).observe(
                            getViewLifecycleOwner(), result -> {
                                if (result.isSuccess()) {
                                    User user = ((Result.UserResponseSuccess) result).getData();
                                    Log.d(TAG, "user: " + user.toString());
                                    saveLoginData(username, email, password, user.getIdToken());
                                    userViewModel.setAuthenticationError(false);
                                    Navigation.findNavController(view).navigate(
                                            R.id.action_registerFragment_to_mainActivity);
                                } else {
                                    userViewModel.setAuthenticationError(true);
                                    Snackbar.make(requireActivity().findViewById(android.R.id.content),
                                            getErrorMessage(((Result.Error) result).getMessage()),
                                            Snackbar.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    userViewModel.getUser(username, email, password, false);
                }
                //binding.registerProgressBar.setVisibility(View.GONE);
            }else {
                userViewModel.setAuthenticationError(true);
                Snackbar.make(requireActivity().findViewById(android.R.id.content),
                        R.string.check_login_data_message, Snackbar.LENGTH_SHORT).show();
            }

        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private boolean isEmailOk(String email) {
        ValidationResult validation = myValidator.validateMail(email);
        if (!validation.isSuccess()) {
            binding.txtInputEditUser.setError(validation.getMessage());
            return false;
        } else {
            binding.txtInputEditUser.setError(null);
            return true;
        }
    }

    private boolean isUsernameOk(String username) {
        if(username.isEmpty()) {
            binding.txtInputEditName.setError(requireActivity().getString(R.string.error_empty_username));
            return false;
        } else {
            binding.txtInputEditName.setError(null);
            return true;
        }
    }

    private boolean isPasswordOk(String password) {
        ValidationResult validation = myValidator.validatePassword(password);
        if(!validation.isSuccess()){
            //binding.txtInputLayoutPwd.setError(validation.getMessage().toString());
            Snackbar.make(requireActivity().findViewById(android.R.id.content),
                    validation.getMessage(), Snackbar.LENGTH_SHORT).show();
            return false;
        }else{
            //binding.txtInputLayoutPwd.setError(null);
            return true;
        }
    }

    private String getErrorMessage(String message) {
        switch(message) {
            case USER_COLLISION_ERROR:
                return requireActivity().getString(R.string.error_user_collision_message);
            case USERNAME_NOT_AVAILABLE:
                return requireActivity().getString(R.string.error_username_not_available);
            default:
                return requireActivity().getString(R.string.unexpected_error);
        }
    }

    private void saveLoginData(String username, String email, String password, String idToken) {
        try {
            dataEncryptionUtil.writeSecretDataWithEncryptedSharedPreferences(
                    ENCRYPTED_SHARED_PREFERENCES_FILE_NAME, USERNAME, username);
            dataEncryptionUtil.writeSecretDataWithEncryptedSharedPreferences(
                    ENCRYPTED_SHARED_PREFERENCES_FILE_NAME, EMAIL_ADDRESS, email);
            dataEncryptionUtil.writeSecretDataWithEncryptedSharedPreferences(
                    ENCRYPTED_SHARED_PREFERENCES_FILE_NAME, PASSWORD, password);
            dataEncryptionUtil.writeSecretDataWithEncryptedSharedPreferences(
                    ENCRYPTED_SHARED_PREFERENCES_FILE_NAME, ID_TOKEN, idToken);
            dataEncryptionUtil.writeSecreteDataOnFile(ENCRYPTED_DATA_FILE_NAME,
                    email.concat(":").concat(password));
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }
}