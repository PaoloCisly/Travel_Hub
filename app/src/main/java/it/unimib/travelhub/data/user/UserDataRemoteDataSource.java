package it.unimib.travelhub.data.user;

import static it.unimib.travelhub.util.Constants.FIREBASE_REALTIME_DATABASE;
import static it.unimib.travelhub.util.Constants.FIREBASE_TRAVELS_COLLECTION;
import static it.unimib.travelhub.util.Constants.FIREBASE_USERNAMES_COLLECTION;
import static it.unimib.travelhub.util.Constants.FIREBASE_USERS_COLLECTION;
import static it.unimib.travelhub.util.Constants.USERNAME;
import static it.unimib.travelhub.util.Constants.USERNAME_NOT_AVAILABLE;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import it.unimib.travelhub.model.Result;
import it.unimib.travelhub.model.User;

public class UserDataRemoteDataSource extends BaseUserDataRemoteDataSource {
    private final DatabaseReference databaseReference;
    private static final String TAG = UserDataRemoteDataSource.class.getSimpleName();
    public UserDataRemoteDataSource() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(FIREBASE_REALTIME_DATABASE);
        databaseReference = firebaseDatabase.getReference().getRef();
    }

    @Override
    public void saveUserData(User user) {
        databaseReference.child(FIREBASE_USERS_COLLECTION).child(user.getIdToken()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d(TAG, "User already present in Firebase Realtime Database");
                    /*databaseReference.child(FIREBASE_USERS_COLLECTION).child(user.getIdToken()).child(USERNAME).setValue(user.getUsername())
                            .addOnSuccessListener(aVoid -> userResponseCallback.onSuccessFromRemoteDatabase(user))
                            .addOnFailureListener(e -> userResponseCallback.onFailureFromRemoteDatabase(e.getLocalizedMessage()));*/
                    user.setUsername(Objects.requireNonNull(snapshot.child(USERNAME).getValue()).toString());
                    if (snapshot.child("name").getValue() != null)
                        user.setName(Objects.requireNonNull(snapshot.child("name").getValue()).toString());
                    if (snapshot.child("surname").getValue() != null)
                        user.setSurname(Objects.requireNonNull(snapshot.child("surname").getValue()).toString());
                    if (snapshot.child("birthDate").getValue() != null)
                        user.setBirthDate((Long) snapshot.child("birthDate").getValue());
                    userResponseCallback.onSuccessFromRemoteDatabase(user);
                } else {
                    Log.d(TAG, "User not present in Firebase Realtime Database" + user);
                    databaseReference.child(FIREBASE_USERS_COLLECTION).child(user.getIdToken()).setValue(user)
                            .addOnSuccessListener(aVoid -> mapUsernameToId(user))
                            .addOnFailureListener(e -> userResponseCallback.onFailureFromRemoteDatabase(e.getLocalizedMessage()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                userResponseCallback.onFailureFromRemoteDatabase(error.getMessage());
            }
        });
    }

    public void isUsernameTaken(String username, String mail, String password) {
        Query query = databaseReference.child(FIREBASE_USERS_COLLECTION).orderByChild("username").equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange");
                if (snapshot.exists()) {
                    Log.d(TAG, "Username already taken" + Objects.requireNonNull(snapshot.getValue()));
                    userResponseCallback.onFailureFromRemoteDatabase(USERNAME_NOT_AVAILABLE);
                } else {
                    Log.d(TAG, "Username available");
                    userResponseCallback.signUp(username, mail, password);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                userResponseCallback.onFailureFromRemoteDatabase(error.getMessage());
                Log.d(TAG, error + "onCancelled");
            }
        });

    }

    public interface UsernameCheckCallback {
        void onUsernameResponse(Result result);
    }

    @Override
    public void isUserRegistered(String username, final UsernameCheckCallback usernameCheckCallback) {
        databaseReference.child(FIREBASE_USERNAMES_COLLECTION).child(username).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "checkIfUserExists: " + task.getResult().getValue());
                if(task.getResult().getValue() != null){
                    User u = new User();
                    u.setUsername(username);
                    u.setIdToken(task.getResult().getValue().toString());
                    //userResponseCallback.onSuccessFromRemoteDatabase(u);
                    usernameCheckCallback.onUsernameResponse(new Result.UserResponseSuccess(u));
                } else {
                    //userResponseCallback.onFailureFromRemoteDatabase("User not found");
                    usernameCheckCallback.onUsernameResponse(new Result.Error("User not found"));
                }
            } else {
                Log.d(TAG, "checkIfUserExists: " + Objects.requireNonNull(task.getException()).getMessage());
                //userResponseCallback.onFailureFromRemoteDatabase(task.getException().getMessage());
                usernameCheckCallback.onUsernameResponse(new Result.Error(task.getException().getMessage()));
            }});
    }

    public interface UserCallback {
        void onUserResponse(Result result);
    }

    @Override
    public void updateUserData(User user, final UserCallback userCallback) {
        ArrayList<Long> travels = new ArrayList<>();
        databaseReference.child(FIREBASE_USERS_COLLECTION).child(user.getIdToken()).child(FIREBASE_TRAVELS_COLLECTION).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    travels.add((Long) snapshot.getValue());
                }
                Map<String, Object> userMap = user.toMap(travels);
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put(FIREBASE_USERS_COLLECTION + "/" + user.getIdToken(), userMap);

                databaseReference.updateChildren(childUpdates)
                        .addOnSuccessListener(aVoid -> userCallback.onUserResponse(new Result.UserResponseSuccess(user)))
                        .addOnFailureListener(e -> userCallback.onUserResponse(new Result.Error(e.getLocalizedMessage())));
            }
        });
    }

    @Override
    public void getUserProfileImage(String id, UserRemoteFirestoreDataSource.getProfileImagesCallback getProfileImagesCallback) {

    }

    private void mapUsernameToId(User user) {
        Log.d(TAG, "Mapping username to id");
        databaseReference.child(FIREBASE_USERNAMES_COLLECTION).child(user.getUsername()).setValue(user.getIdToken())
                .addOnSuccessListener(aVoid -> userResponseCallback.onSuccessFromRemoteDatabase(user))
                .addOnFailureListener(e -> {
                    Log.d(TAG, "Error while mapping username to id" + e.getLocalizedMessage());
                    userResponseCallback.onFailureFromRemoteDatabase(e.getLocalizedMessage());
                });
    }
}
