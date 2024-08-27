package it.unimib.travelhub.data.source;

import static it.unimib.travelhub.util.Constants.ENCRYPTED_SHARED_PREFERENCES_FILE_NAME;
import static it.unimib.travelhub.util.Constants.FIREBASE_REALTIME_DATABASE;
import static it.unimib.travelhub.util.Constants.FIREBASE_TRAVELS_COLLECTION;
import static it.unimib.travelhub.util.Constants.FIREBASE_USERS_COLLECTION;
import static it.unimib.travelhub.util.Constants.ID_TOKEN;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import it.unimib.travelhub.crypto_util.DataEncryptionUtil;
import it.unimib.travelhub.model.TravelMember;
import it.unimib.travelhub.model.Travels;
import it.unimib.travelhub.model.TravelsResponse;

public class TravelsRemoteDataSource extends BaseTravelsRemoteDataSource {

    private static final String TAG = TravelsRemoteDataSource.class.getSimpleName();

    private final DatabaseReference databaseReference;

    private final DataEncryptionUtil dataEncryptionUtil;

    public TravelsRemoteDataSource(DataEncryptionUtil dataEncryptionUtil) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(FIREBASE_REALTIME_DATABASE);
        databaseReference = firebaseDatabase.getReference().getRef();
        this.dataEncryptionUtil = dataEncryptionUtil;
    }

    @Override
    public void getAllUserTravel() {
        try {
            String idToken = dataEncryptionUtil.readSecretDataWithEncryptedSharedPreferences(ENCRYPTED_SHARED_PREFERENCES_FILE_NAME, ID_TOKEN);
            databaseReference.child(FIREBASE_USERS_COLLECTION).child(idToken).child("travels").get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.d(TAG, "Error getting data", task.getException());
                } else {
                    Log.d(TAG, "Successful read: " + task.getResult().getValue());

                    List<Integer> travelsIdList = new ArrayList<>();
                    for(DataSnapshot ds : task.getResult().getChildren()) {
                        Integer id = ds.getValue(Integer.class);
                        travelsIdList.add(id);
                    }

                    Log.d(TAG, "Travels id list: " + travelsIdList);

                    TravelsResponse travelsResponse = new TravelsResponse(travelsIdList.size(), travelsCallback);

                    for (Integer id : travelsIdList) {
                        databaseReference.child("travels").child(id.toString()).get().addOnCompleteListener(task1 -> {
                            if (!task1.isSuccessful()) {
                                Log.d(TAG, "Error getting data", task1.getException());
                            } else {
                                Log.d(TAG, "Successful read: " + task1.getResult().getValue());
                                Travels travels = task1.getResult().getValue(Travels.class);
                                travelsResponse.addTravel(travels);
                            }
                        });
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addTravel(Travels travel) {
        try {
            databaseReference.child("travels").child(Long.toString(travel.getId())).setValue(travel)
                    .addOnSuccessListener(aVoid -> {
                        List<String> usersId = new ArrayList<>();
                        for(TravelMember s : travel.getMembers()){
                            usersId.add(s.getIdToken());
                        }
                        Log.d(TAG, usersId.toString());
                        //addTravelIdToUser(idToken, travel.getId(), travel);
                        addTravelIdToUsers(usersId, travel.getId(), travel);
                        Log.d(TAG, "Travel added successfully");
                    })
                    .addOnFailureListener(e -> {
                        travelsCallback.onFailureFromRemote(e);
                        Log.d(TAG, "Error adding travel", e);
                    });
        } catch (Exception e) {
            e.printStackTrace();
            travelsCallback.onFailureFromRemote(e);
        }
    }

    private void addTravelIdToUser(String userId, long travelId, Travels travel) {
        try {
            databaseReference.child(FIREBASE_USERS_COLLECTION).child(userId).child("travels").get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.d(TAG, "Error getting data", task.getException());
                    travelsCallback.onFailureFromRemote(task.getException());
                } else {
                    List<Long> travelsIdList = new ArrayList<>();
                    for (DataSnapshot ds : task.getResult().getChildren()) {
                        Long id = ds.getValue(Long.class);
                        travelsIdList.add(id);
                    }
                    travelsIdList.add(travelId);

                    databaseReference.child(FIREBASE_USERS_COLLECTION).child(userId).child("travels").setValue(travelsIdList)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Travel id added to user successfully");
                                travelsCallback.onSuccessFromCloudWriting(travel);
                            })
                            .addOnFailureListener(e -> {
                                Log.d(TAG, "Error adding travel id to user", e);
                                travelsCallback.onFailureFromRemote(e);
                            });
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            travelsCallback.onFailureFromRemote(e);
        }
    }

    private void addTravelIdToUsers(List<String> usersId, long travelId, Travels travel) {
        AtomicInteger count = new AtomicInteger(0);
        for(int i=0;i<usersId.size();i++){
            String userId = usersId.get(i);
            try {
                databaseReference.child(FIREBASE_USERS_COLLECTION).child(userId).child("travels").get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.d(TAG, "Error getting data", task.getException());
                    } else {
                        List<Long> travelsIdList = new ArrayList<>();
                        for (DataSnapshot ds : task.getResult().getChildren()) {
                            Long id = ds.getValue(Long.class);
                            travelsIdList.add(id);
                        }
                        travelsIdList.add(travelId);

                        databaseReference.child(FIREBASE_USERS_COLLECTION).child(userId).child("travels").setValue(travelsIdList)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Travel id added to all users successfully");
                                    count.set(count.get() + 1);
                                    if(count.get() == usersId.size()){
                                        travelsCallback.onSuccessFromCloudWriting(travel);
                                    }
                                    else{
                                        travelsCallback.onFailureFromRemote(new Exception("travel id not added to all users"));
                                        Log.d(TAG, "travel id not added to all users");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    travelsCallback.onFailureFromRemote(new Exception("something went wrong: " + e.getMessage()));
                                    Log.d(TAG, "Error adding travel id to user", e);
                                });
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void updateTravel(Travels newTravel, Travels oldTravel) {
        try {
            databaseReference.child(FIREBASE_TRAVELS_COLLECTION).child(String.valueOf(newTravel.getId())).setValue(newTravel)
                    .addOnSuccessListener(aVoid -> {
                        //travelsCallback.onUpdateSuccess(travel);
                        Log.d(TAG, "updating users collection");
                        updateTravelInUsers(newTravel, oldTravel);
                    })
                    .addOnFailureListener(e -> travelsCallback.onFailureFromRemote(e));
        } catch (Exception e) {
            e.printStackTrace();
            travelsCallback.onFailureFromRemote(e);
        }

    }

    private void updateTravelInUsers(Travels newTravel, Travels oldTravel){
        List<TravelMember> addedMembers = new ArrayList<>(newTravel.getMembers());
        addedMembers.removeAll(oldTravel.getMembers());

        List<TravelMember> removedMembers = new ArrayList<>(oldTravel.getMembers());
        removedMembers.removeAll(newTravel.getMembers());

        Log.d(TAG, "addedMembers: " + addedMembers);
        Log.d(TAG, "removedMembers: " + removedMembers);

        updateTravelInUsersRemoveThenAdd(newTravel, removedMembers, addedMembers);
    }

    private void updateTravelInUsersRemoveThenAdd(Travels newTravel, List<TravelMember> removedMembers, List<TravelMember> addedMembers){
        AtomicInteger countRemoved = new AtomicInteger(0);
        for(TravelMember travelMember : removedMembers){
            try {
                databaseReference.child(FIREBASE_USERS_COLLECTION).child(travelMember.getIdToken()).child(FIREBASE_TRAVELS_COLLECTION).get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.d(TAG, "Error getting data", task.getException());
                        travelsCallback.onFailureFromRemote(task.getException());
                    } else {
                        List<Long> travelsIdList = new ArrayList<>();
                        for (DataSnapshot ds : task.getResult().getChildren()) {
                            Long id = ds.getValue(Long.class);
                            travelsIdList.add(id);
                        }
                        travelsIdList.remove(newTravel.getId());

                        databaseReference.child(FIREBASE_USERS_COLLECTION).child(travelMember.getIdToken()).child("travels").setValue(travelsIdList)
                                .addOnSuccessListener(aVoid -> {
                                    countRemoved.set(countRemoved.get() + 1);
                                    if(countRemoved.get() == removedMembers.size()){
                                        updateTravelInUsersAdd(newTravel, addedMembers);
                                    }
                                    else{
                                        travelsCallback.onFailureFromRemote(new Exception("travel id not removed from all users"));
                                        Log.d(TAG, "travel id not removed from all users");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    travelsCallback.onFailureFromRemote(new Exception("something went wrong: " + e.getMessage()));
                                    Log.d(TAG, "Error removing travel id from user", e);
                                });
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateTravelInUsersAdd(Travels newTravel, List<TravelMember> addedMembers){
        AtomicInteger countAdded = new AtomicInteger(0);
        for(TravelMember travelMember : addedMembers){
            try {
                databaseReference.child(FIREBASE_USERS_COLLECTION).child(travelMember.getIdToken()).child(FIREBASE_TRAVELS_COLLECTION).get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.d(TAG, "Error getting data", task.getException());
                        travelsCallback.onFailureFromRemote(task.getException());
                    } else {
                        List<Long> travelsIdList = new ArrayList<>();
                        for (DataSnapshot ds : task.getResult().getChildren()) {
                            Long id = ds.getValue(Long.class);
                            travelsIdList.add(id);
                        }
                        travelsIdList.add(newTravel.getId());

                        databaseReference.child(FIREBASE_USERS_COLLECTION).child(travelMember.getIdToken()).child("travels").setValue(travelsIdList)
                                .addOnSuccessListener(aVoid -> {
                                    countAdded.set(countAdded.get() + 1);
                                    if(countAdded.get() == addedMembers.size()){
                                        //travelsCallback.onSuccessUpdateFromRemote(newTravel);
                                        travelsCallback.onUpdateSuccess(newTravel);
                                    }
                                    else{
                                        travelsCallback.onFailureFromRemote(new Exception("travel id not added to all users"));
                                        Log.d(TAG, "travel id not added to all users");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    travelsCallback.onFailureFromRemote(new Exception("something went wrong: " + e.getMessage()));
                                    Log.d(TAG, "Error adding travel id to user", e);
                                });
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void deleteTravel(Travels travel) {
        databaseReference.child("travels").child(String.valueOf(travel.getId())).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                deleteTravelFromUsers(travel);
            } else {
                Log.d(TAG, "Error deleting travel", task.getException());
                travelsCallback.onFailureFromRemote(task.getException());
            }
        });
    }

    private void deleteTravelFromUsers(Travels travel){
        AtomicInteger count = new AtomicInteger(0);
        for(TravelMember member : travel.getMembers()){
            try {
                databaseReference.child(FIREBASE_USERS_COLLECTION).child(member.getIdToken()).child(FIREBASE_TRAVELS_COLLECTION).get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.d(TAG, "Error getting data", task.getException());
                        travelsCallback.onFailureFromRemote(task.getException());
                    } else {
                        List<Long> travelsIdList = new ArrayList<>();
                        for (DataSnapshot ds : task.getResult().getChildren()) {
                            Long id = ds.getValue(Long.class);
                            travelsIdList.add(id);
                        }
                        travelsIdList.remove(travel.getId());

                        databaseReference.child(FIREBASE_USERS_COLLECTION).child(member.getIdToken()).child("travels").setValue(travelsIdList)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Travel id removed from user successfully");
                                    count.set(count.get() + 1);
                                    if(count.get() == travel.getMembers().size()){
                                        travelsCallback.onSuccessDeletionFromRemote(travel);
                                    }
                                    else{
                                        travelsCallback.onFailureFromRemote(new Exception("travel id not removed from all users"));
                                        Log.d(TAG, "travel id not removed from all users");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    travelsCallback.onFailureFromRemote(new Exception("something went wrong: " + e.getMessage()));
                                    Log.d(TAG, "Error removing travel id from user", e);
                                });
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
