package it.unimib.travelhub.data.repository.user;

import it.unimib.travelhub.model.User;

public interface UserResponseCallback {
    void signUp(String username, String email, String password);

    void onSuccessFromAuthentication(User user);
    void onFailureFromAuthentication(String message);
    void onSuccessFromRemoteDatabase(User user);
    void onFailureFromRemoteDatabase(String message);
    void onSuccessLogout();
}
