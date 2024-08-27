package it.unimib.travelhub.data.source;

import static it.unimib.travelhub.util.Constants.ENCRYPTED_SHARED_PREFERENCES_FILE_NAME;
import static it.unimib.travelhub.util.Constants.FIREBASE_TRAVELS_COLLECTION;
import static it.unimib.travelhub.util.Constants.FIREBASE_USERS_COLLECTION;
import static it.unimib.travelhub.util.Constants.ID_TOKEN;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import it.unimib.travelhub.crypto_util.DataEncryptionUtil;
import it.unimib.travelhub.model.TravelMember;
import it.unimib.travelhub.model.Travels;
import it.unimib.travelhub.model.TravelsResponse;

public class TravelsRemoteFirestoreDataSource extends BaseTravelsRemoteDataSource{

    private final DataEncryptionUtil dataEncryptionUtil;
    private final FirebaseFirestore db;

    private static final String TAG = TravelsRemoteFirestoreDataSource.class.getSimpleName();

    public TravelsRemoteFirestoreDataSource(DataEncryptionUtil dataEncryptionUtil) {
        this.dataEncryptionUtil = dataEncryptionUtil;
        this.db = FirebaseFirestore.getInstance();

    }

    @Override
    public void getAllUserTravel() {
        String idToken = null;
        try {
            idToken = dataEncryptionUtil.
                    readSecretDataWithEncryptedSharedPreferences(ENCRYPTED_SHARED_PREFERENCES_FILE_NAME, ID_TOKEN);
        }
        catch(Exception e){
            Log.e(TAG, "Error reading idToken from SharedPreferences", e);
            travelsCallback.onFailureFromRemote(e);
        }
        if (idToken == null) {
            Log.e(TAG, "idToken is null");
            travelsCallback.onFailureFromRemote(new Exception("idToken is null"));
            return;
        }
        db.collection(FIREBASE_USERS_COLLECTION).document(idToken)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "get: " + Objects.requireNonNull(task.getResult().getData()).keySet());
                        @SuppressWarnings("unchecked")
                        List<Long> travelIds = (List<Long>) task.getResult().getData().get("travels");

                        if(travelIds == null || travelIds.isEmpty()) {
                            Log.d(TAG, "No travels found");
                            travelsCallback.onFailureFromRemote(new Exception("No travels found"));
                            return;
                        }

                        TravelsResponse travelsResponse = new TravelsResponse(travelIds.size(), travelsCallback);

                        for(long travelId : travelIds) {
                            db.collection(FIREBASE_TRAVELS_COLLECTION).document(String.valueOf(travelId))
                                    .get().addOnCompleteListener(travelTask -> {
                                        if (travelTask.isSuccessful()) {
                                            DocumentSnapshot document = travelTask.getResult();
                                            if (document.exists()) {
                                                Travels travel = document.toObject(Travels.class);
                                                travelsResponse.addTravel(travel);
                                            } else {
                                                Log.d(TAG, "No such document");
                                                travelsCallback.onFailureFromRemote(new Exception("No such document"));
                                            }
                                        } else {
                                            Log.d(TAG, "get failed with ", travelTask.getException());
                                        }
                                    });
                        }
                    } else {
                        Log.d(TAG, "Error adding document", task.getException());
                    }
                });

    }

    @Override
    public void addTravel(Travels travel) {
        Log.d(TAG, "Adding travel: " + travel);
        Map<String, Object> travelMap = travel.toMap();
        db.collection(FIREBASE_TRAVELS_COLLECTION).document(String.valueOf(travel.getId()))
                .set(travelMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Travel added successfully");
                    addToUsers(travel);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding travel", e);
                    travelsCallback.onFailureFromRemote(e);
                });
    }

    private void addToUsers(Travels travel) {
        Log.d(TAG, "Adding to users collection: " + travel);
        List<TravelMember> members = travel.getMembers();
        WriteBatch batch = db.batch();

        for(TravelMember member : members) {
            String userId = member.getIdToken();
            DocumentReference userTravelRef = db.collection(FIREBASE_USERS_COLLECTION)
                    .document(userId);

            Map<String, Object> userTravelMap = new HashMap<>();
            userTravelMap.put("travels", FieldValue.arrayUnion(travel.getId()));
            batch.update(userTravelRef, userTravelMap);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Travel added to users successfully");
                    travelsCallback.onSuccessFromCloudWriting(travel);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding travel to users", e);
                    travelsCallback.onFailureFromRemote(e);
                });
    }

    @Override
    public void updateTravel(Travels newTravel, Travels oldTravel) {
        WriteBatch batch = db.batch();
        DocumentReference travelRef = db.collection(FIREBASE_TRAVELS_COLLECTION)
                .document(String.valueOf(oldTravel.getId()));

        Map<String, Object> travelMap = newTravel.toMap();
        batch.update(travelRef, travelMap);

        List<TravelMember> toAddMembers = new ArrayList<>(newTravel.getMembers());
        toAddMembers.removeAll(oldTravel.getMembers());

        List<TravelMember> toRemoveMembers = new ArrayList<>(oldTravel.getMembers());
        toRemoveMembers.removeAll(newTravel.getMembers());

        Log.d(TAG, "toAddMembers: " + toAddMembers);
        Log.d(TAG, "toRemoveMembers: " + toRemoveMembers);

        for(TravelMember member : toRemoveMembers) {
            String userId = member.getIdToken();
            DocumentReference userTravelRef = db.collection(FIREBASE_USERS_COLLECTION)
                    .document(userId);

            Map<String, Object> userTravelMap = new HashMap<>();
            userTravelMap.put("travels", FieldValue.arrayRemove(oldTravel.getId()));
            batch.update(userTravelRef, userTravelMap);
        }

        for(TravelMember member : toAddMembers) {
            String userId = member.getIdToken();
            DocumentReference userTravelRef = db.collection(FIREBASE_USERS_COLLECTION)
                    .document(userId);

            Map<String, Object> userTravelMap = new HashMap<>();
            userTravelMap.put("travels", FieldValue.arrayUnion(newTravel.getId()));
            batch.update(userTravelRef, userTravelMap);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Travel updated successfully");
                    travelsCallback.onUpdateSuccess(newTravel);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating travel", e);
                    travelsCallback.onFailureFromRemote(e);
                });
    }

    @Override
    public void deleteTravel(Travels travels) {
        WriteBatch batch = db.batch();
        DocumentReference travelRef = db.collection(FIREBASE_TRAVELS_COLLECTION)
                .document(String.valueOf(travels.getId()));

        batch.delete(travelRef);

        List<TravelMember> members = travels.getMembers();
        for(TravelMember member : members) {
            String userId = member.getIdToken();
            DocumentReference userTravelRef = db.collection(FIREBASE_USERS_COLLECTION)
                    .document(userId);

            Map<String, Object> userTravelMap = new HashMap<>();
            userTravelMap.put("travels", FieldValue.arrayRemove(travels.getId()));
            batch.update(userTravelRef, userTravelMap);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Travel deleted successfully");
                    travelsCallback.onSuccessDeletionFromRemote(travels);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting travel", e);
                    travelsCallback.onFailureFromRemote(e);
                });
    }
}
