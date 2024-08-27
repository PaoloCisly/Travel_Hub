package it.unimib.travelhub.data.user;

import it.unimib.travelhub.data.repository.user.UserResponseCallback;
import it.unimib.travelhub.model.User;

/**
 * Base class to manage the user authentication.
 */
public abstract class BaseUserAuthenticationRemoteDataSource {
    protected UserResponseCallback userResponseCallback;

    public void setUserResponseCallback(UserResponseCallback userResponseCallback) {
        this.userResponseCallback = userResponseCallback;
    }
    public abstract User getLoggedUser();
    public abstract void logout();
    public abstract void signUp(String email, String password);

    public abstract void signUp(String username, String email, String password);
    public abstract void signIn(String email, String password);
    public abstract void signInWithGoogle(User user);

    public abstract void isGoogleUserRegistered(User user, UserAuthenticationRemoteDataSource.GoogleUserCallback callback);
}
