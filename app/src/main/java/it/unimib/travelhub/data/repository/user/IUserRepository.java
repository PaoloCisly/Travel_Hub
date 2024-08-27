package it.unimib.travelhub.data.repository.user;

import android.net.Uri;

import androidx.lifecycle.MutableLiveData;

import java.io.File;

import it.unimib.travelhub.data.source.RemoteFileStorageSource;
import it.unimib.travelhub.data.user.UserAuthenticationRemoteDataSource;
import it.unimib.travelhub.data.user.UserDataRemoteDataSource;
import it.unimib.travelhub.data.user.UserRemoteFirestoreDataSource;
import it.unimib.travelhub.model.Result;
import it.unimib.travelhub.model.User;

public interface IUserRepository {
    MutableLiveData<Result> getUser(String email, String password, boolean isUserRegistered);
    MutableLiveData<Result> getUser(String username, String email, String password, boolean isUserRegistered);
    MutableLiveData<Result> getGoogleUser(User user);
    MutableLiveData<Result> logout();
    User getLoggedUser();
    void signUp(String email, String password);
    void signIn(String email, String password);
    void signInWithGoogle(User user);

    void isUserRegistered(String username, UserDataRemoteDataSource.UsernameCheckCallback callback);

    void updateUserData(User user, UserDataRemoteDataSource.UserCallback userCallback);
    void getUserProfileImage(String id, UserRemoteFirestoreDataSource.getProfileImagesCallback callback);
    void uploadProfileImage(String remotePath, Uri imageUri, RemoteFileStorageSource.uploadCallback uploadCallback);
    void downloadProfileImage(String url, File file, RemoteFileStorageSource.downloadCallback downloadCallback);

    void isGoogleUserAlreadyRegistered(User user, UserAuthenticationRemoteDataSource.GoogleUserCallback callback);
}
