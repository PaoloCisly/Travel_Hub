package it.unimib.travelhub.ui.welcome;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import it.unimib.travelhub.R;
import it.unimib.travelhub.data.repository.user.IUserRepository;
import it.unimib.travelhub.databinding.FragmentLoginBinding;
import it.unimib.travelhub.model.Result;
import it.unimib.travelhub.model.User;
import it.unimib.travelhub.ui.main.MainActivity;
import it.unimib.travelhub.model.IValidator;
import it.unimib.travelhub.util.ServiceLocator;
import it.unimib.travelhub.crypto_util.DataEncryptionUtil;
import it.unimib.travelhub.model.ValidationResult;

import static it.unimib.travelhub.util.Constants.EMAIL_ADDRESS;
import static it.unimib.travelhub.util.Constants.ID_TOKEN;
import static it.unimib.travelhub.util.Constants.INVALID_CREDENTIALS_ERROR;
import static it.unimib.travelhub.util.Constants.INVALID_USER_ERROR;
import static it.unimib.travelhub.util.Constants.PASSWORD;
import static it.unimib.travelhub.util.Constants.ENCRYPTED_SHARED_PREFERENCES_FILE_NAME;
import static it.unimib.travelhub.util.Constants.USERNAME;
import static it.unimib.travelhub.util.Constants.USER_BIRTHDATE;
import static it.unimib.travelhub.util.Constants.USER_NAME;
import static it.unimib.travelhub.util.Constants.USER_PROFILE_IMAGE;
import static it.unimib.travelhub.util.Constants.USER_SURNAME;

import it.unimib.travelhub.GlobalClass;

public class LoginFragment extends Fragment {

    IValidator myValidator;

    private static final String TAG = LoginFragment.class.getSimpleName();
    private FragmentLoginBinding binding;
    private DataEncryptionUtil dataEncryptionUtil;
    private static final boolean USE_NAVIGATION_COMPONENT = true;
    private ActivityResultLauncher<IntentSenderRequest> activityResultLauncher;
    private UserViewModel userViewModel;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;

    public LoginFragment() {
        // Required empty public constructor
    }
    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myValidator = ServiceLocator.getInstance().getCredentialsValidator(GlobalClass.getContext());

        IUserRepository userRepository = ServiceLocator.getInstance().getUserRepository();

        userViewModel = new ViewModelProvider(
                requireActivity(),
                new UserViewModelFactory(userRepository)).get(UserViewModel.class);

        dataEncryptionUtil = new DataEncryptionUtil(requireActivity().getApplication());

        oneTapClient = Identity.getSignInClient(requireActivity());
        signInRequest = BeginSignInRequest.builder()
                .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                        .setSupported(true)
                        .build())
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.default_web_client_id))
                        // Only show accounts previously used to sign in.
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                // Automatically sign in when exactly one credential is retrieved.
                .setAutoSelectEnabled(true)
                .build();

        ActivityResultContracts.StartIntentSenderForResult startIntentSenderForResult = new ActivityResultContracts.StartIntentSenderForResult();

        activityResultLauncher = registerForActivityResult(startIntentSenderForResult, activityResult -> {
            if (activityResult.getResultCode() == Activity.RESULT_OK) {
                Log.d(TAG, "result.getResultCode() == Activity.RESULT_OK");
                try {
                    SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(activityResult.getData());
                    String idToken = credential.getGoogleIdToken();

                    Bundle bundle = new Bundle();
                    User user = new User();
                    user.setIdToken(idToken);
                    bundle.putSerializable("user", user);

                    dataEncryptionUtil.writeSecretDataWithEncryptedSharedPreferences(
                            ENCRYPTED_SHARED_PREFERENCES_FILE_NAME, ID_TOKEN, idToken);
                    userRepository.isGoogleUserAlreadyRegistered(user, responseCode -> {
                        if (responseCode == 1) {
                            Log.d(TAG, "Google is new, so we set a username for him");
                            UsernameChoiceFragment usernameChoiceFragment = new UsernameChoiceFragment();
                            usernameChoiceFragment.setArguments(bundle);
                            // change fragment and pass the data
                            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.replace(R.id.nav_welcome_fragment, usernameChoiceFragment);
                            fragmentTransaction.addToBackStack(null);
                            fragmentTransaction.commit();
                        }
                        if(responseCode == 2){
                            Log.d(TAG, "Google user was already registered, so we log him up");
                            User user1 = new User();
                            user1.setIdToken(idToken);
                            userViewModel.getGoogleUserMutableLiveData(user1).observe(getViewLifecycleOwner(), authenticationResult -> {
                                if (authenticationResult.isSuccess()) {
                                    User userResponse = ((Result.UserResponseSuccess) authenticationResult).getData();
                                    saveLoginData(userResponse.getEmail(), userResponse.getUsername(), null,
                                            userResponse.getIdToken(), userResponse.getName(), userResponse.getSurname(),
                                            userResponse.getBirthDate(), userResponse.getPhotoUrl());
                                    userViewModel.setAuthenticationError(false);
                                    retrieveUserInformationAndStartActivity(userResponse, R.id.action_loginFragment_to_mainActivity);
                                } else {
                                    userViewModel.setAuthenticationError(true);
                                    binding.progressBar.setVisibility(View.GONE);
                                    Snackbar.make(requireActivity().findViewById(android.R.id.content),
                                            getErrorMessage(((Result.Error) authenticationResult).getMessage()),
                                            Snackbar.LENGTH_SHORT).show();
                                }
                            });
                        }
                        if(responseCode == 0){
                            Log.d(TAG, "an error occurred");
                            Snackbar.make(requireActivity().findViewById(android.R.id.content),
                                    R.string.unexpected_error,
                                    Snackbar.LENGTH_SHORT).show();
                        }

                });
                }catch (ApiException e) {
                    Snackbar.make(requireActivity().findViewById(android.R.id.content),
                            requireActivity().getString(R.string.unexpected_error),
                            Snackbar.LENGTH_SHORT).show();
                } catch (GeneralSecurityException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inflate the layout for this fragment

        if(userViewModel. getLoggedUser() != null) {
            try {
                String mail = dataEncryptionUtil.
                        readSecretDataWithEncryptedSharedPreferences(
                                ENCRYPTED_SHARED_PREFERENCES_FILE_NAME, EMAIL_ADDRESS);
                String password = dataEncryptionUtil.
                        readSecretDataWithEncryptedSharedPreferences(
                                ENCRYPTED_SHARED_PREFERENCES_FILE_NAME, PASSWORD);
                String username = dataEncryptionUtil.
                        readSecretDataWithEncryptedSharedPreferences(
                                ENCRYPTED_SHARED_PREFERENCES_FILE_NAME, "username");
                Log.d(TAG, "Username from encrypted SharedPref: " + username);
                Log.d(TAG, "Email address from encrypted SharedPref: " + mail);
                Log.d(TAG, "Password from encrypted SharedPref: " + password);

                if (mail != null &&  username != null) {
                    Log.d(TAG, "starting main activity");
                    startActivityBasedOnCondition(
                            R.id.action_loginFragment_to_mainActivity);
                }
            } catch (GeneralSecurityException | IOException e) {
                e.printStackTrace();
            }
        }




        binding.buttonLogin.setOnClickListener(V -> {
            String email = Objects.requireNonNull(binding.txtInputEditUser.getText()).toString();
            String password = Objects.requireNonNull(binding.txtInputEditPwd.getText()).toString();

            if (isEmailOk(email) & isPasswordOk(password)) {
                Log.d(TAG, "Email and password are ok");
                if(userViewModel.isAuthenticationSuccess()){
                    binding.progressBar.setVisibility(View.VISIBLE);
                    userViewModel.getUserMutableLiveData(email, password, true).observe(
                            getViewLifecycleOwner(), result -> {
                                if (result.isSuccess()) {
                                    User user = ((Result.UserResponseSuccess) result).getData();
                                    saveLoginData(email, user.getUsername(), password, user.getIdToken(),
                                            user.getName(), user.getSurname(), user.getBirthDate(), user.getPhotoUrl());
                                    userViewModel.setAuthenticationError(false);
                                    startActivityBasedOnCondition(
                                            R.id.action_loginFragment_to_mainActivity);

                                } else {
                                    userViewModel.setAuthenticationError(true);
                                    binding.progressBar.setVisibility(View.GONE);
                                    Snackbar.make(requireActivity().findViewById(android.R.id.content),
                                            getErrorMessage(((Result.Error) result).getMessage()),
                                            Snackbar.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    userViewModel.getUser(email, password, true);
                }
            } else {
                Log.d(TAG, "Email and password are NOT ok");
            }
        });

        binding.buttonRegister.setOnClickListener(V ->
        {
            NavController navController = Navigation.findNavController(view);
            NavDirections val = LoginFragmentDirections.actionLoginFragmentToRegisterFragment();
            navController.navigate(val);
        });

        binding.buttonGoogleLogin.setOnClickListener(v -> oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(requireActivity(), result -> {
                    Log.d(TAG, "onSuccess from oneTapClient.beginSignIn(BeginSignInRequest)");
                    IntentSenderRequest intentSenderRequest =
                            new IntentSenderRequest.Builder(result.getPendingIntent()).build();
                    activityResultLauncher.launch(intentSenderRequest);
                })
                .addOnFailureListener(requireActivity(), e -> {
                    Log.d(TAG, Objects.requireNonNull(e.getLocalizedMessage()));

                    Snackbar.make(requireActivity().findViewById(android.R.id.content),
                            requireActivity().getString(R.string.error_no_google_account_found_message),
                            Snackbar.LENGTH_SHORT).show();
                }));
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

    private void saveLoginData(String email, String username, String password, String idToken, String name, String surname, Long birthdate, String profileImageURL) {
        try {
            dataEncryptionUtil.writeSecretDataWithEncryptedSharedPreferences(
                    ENCRYPTED_SHARED_PREFERENCES_FILE_NAME, USERNAME, username);
            dataEncryptionUtil.writeSecretDataWithEncryptedSharedPreferences(
                    ENCRYPTED_SHARED_PREFERENCES_FILE_NAME, EMAIL_ADDRESS, email);
            dataEncryptionUtil.writeSecretDataWithEncryptedSharedPreferences(
                    ENCRYPTED_SHARED_PREFERENCES_FILE_NAME, PASSWORD, password);
            dataEncryptionUtil.writeSecretDataWithEncryptedSharedPreferences(
                    ENCRYPTED_SHARED_PREFERENCES_FILE_NAME, ID_TOKEN, idToken);
            dataEncryptionUtil.writeSecretDataWithEncryptedSharedPreferences(
                    ENCRYPTED_SHARED_PREFERENCES_FILE_NAME, USER_NAME, name);
            dataEncryptionUtil.writeSecretDataWithEncryptedSharedPreferences(
                    ENCRYPTED_SHARED_PREFERENCES_FILE_NAME, USER_SURNAME, surname);
            dataEncryptionUtil.writeSecretDataWithEncryptedSharedPreferences(
                    ENCRYPTED_SHARED_PREFERENCES_FILE_NAME, USER_PROFILE_IMAGE, profileImageURL);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", getResources().getConfiguration().getLocales().get(0));
            if (birthdate != null)
                dataEncryptionUtil.writeSecretDataWithEncryptedSharedPreferences(
                    ENCRYPTED_SHARED_PREFERENCES_FILE_NAME, USER_BIRTHDATE, sdf.format(new Date(birthdate)));
            else dataEncryptionUtil.writeSecretDataWithEncryptedSharedPreferences(
                    ENCRYPTED_SHARED_PREFERENCES_FILE_NAME, USER_BIRTHDATE, null);


        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    private void startActivityBasedOnCondition(int destination) {
        if (USE_NAVIGATION_COMPONENT) {
            Navigation.findNavController(requireView()).navigate(destination);
        } else {
            Intent intent = new Intent(requireContext(), MainActivity.class);
            startActivity(intent);
        }
        requireActivity().finish();
    }

    private String getErrorMessage(String errorType) {
        switch (errorType) {
            case INVALID_CREDENTIALS_ERROR:
                return requireActivity().getString(R.string.error_login_password_message);
            case INVALID_USER_ERROR:
                return requireActivity().getString(R.string.error_login_user_message);
            default:
                return requireActivity().getString(R.string.unexpected_error);
        }
    }

    private void retrieveUserInformationAndStartActivity(User user, int destination) {
        binding.progressBar.setVisibility(View.VISIBLE);
        userViewModel.getUserMutableLiveData(user.getEmail(), user.getIdToken(), false).observe(
                getViewLifecycleOwner(), result -> {
                    if (result.isSuccess()) {
                        binding.progressBar.setVisibility(View.GONE);
                        startActivityBasedOnCondition(destination);
                    } else {
                        userViewModel.setAuthenticationError(true);
                        binding.progressBar.setVisibility(View.GONE);
                        Snackbar.make(requireActivity().findViewById(android.R.id.content),
                                getErrorMessage(((Result.Error) result).getMessage()),
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}