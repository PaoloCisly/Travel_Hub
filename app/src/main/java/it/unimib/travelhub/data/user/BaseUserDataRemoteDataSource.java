package it.unimib.travelhub.data.user;

import it.unimib.travelhub.data.repository.user.UserResponseCallback;
import it.unimib.travelhub.model.User;

/**
 * Base class to get the user data from a remote source.
 */
public abstract class BaseUserDataRemoteDataSource {
    protected UserResponseCallback userResponseCallback;

    public void setUserResponseCallback(UserResponseCallback userResponseCallback) {
        this.userResponseCallback = userResponseCallback;
    }

    public abstract void saveUserData(User user);
    public abstract void isUsernameTaken(String username, String email, String password);
    public abstract void isUserRegistered(String username, UserDataRemoteDataSource.UsernameCheckCallback callback);
    public abstract void updateUserData(User user, final UserDataRemoteDataSource.UserCallback userCallback);
    public abstract void getUserProfileImage(String id, UserRemoteFirestoreDataSource.getProfileImagesCallback getProfileImagesCallback);
}
