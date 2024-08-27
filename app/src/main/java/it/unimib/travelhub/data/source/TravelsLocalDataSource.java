package it.unimib.travelhub.data.source;

import android.util.Log;

import java.util.List;

import it.unimib.travelhub.data.database.TravelsDao;
import it.unimib.travelhub.data.database.TravelsRoomDatabase;
import it.unimib.travelhub.model.Travels;
import it.unimib.travelhub.model.TravelsResponse;

public class TravelsLocalDataSource extends BaseTravelsLocalDataSource {
    private final TravelsDao travelsDao;
    private static final String TAG = TravelsLocalDataSource.class.getSimpleName();

    public TravelsLocalDataSource(TravelsRoomDatabase travelsRoomDatabase) {
        this.travelsDao = travelsRoomDatabase.travelsDao();
    }

    @Override
    public void getTravels() {
        TravelsRoomDatabase.databaseWriteExecutor.execute(() -> {
            try {
                List<Travels> travelsList = travelsDao.getAll();
                if (travelsList.isEmpty()) {
                    travelsCallback.onFailureFromLocal(new Exception("No travels found"));
                } else {
                    travelsCallback.onSuccessFromLocal(new TravelsResponse(travelsList));
                }
            } catch (Exception e) {
                travelsCallback.onFailureFromLocal(e);
            }
        });
    }

    @Override
    public void updateTravel(Travels travel) {
        TravelsRoomDatabase.databaseWriteExecutor.execute(() -> {
            try {
                int updated = travelsDao.updateSingleTravel(travel);
                if (updated == 0) {
                    travelsCallback.onFailureFromLocal(new Exception("No travels updated"));
                } else {
                    travelsCallback.onSuccessSynchronization(travel);
                }
            } catch (Exception e) {
                travelsCallback.onFailureFromLocal(e);
            }
        });
    }

    @Override
    public void insertTravels(List<Travels> travelsList) {
        TravelsRoomDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Log.d(TAG, "Travel list size: " + travelsList.size());
                List<Long> inserted = travelsDao.insertTravelsList(travelsList);
                if (inserted.isEmpty()) {
                    Log.d(TAG, "No travels inserted");
                    travelsCallback.onFailureFromLocal(new Exception("No travels inserted"));
                } else {
                    Log.d(TAG, "Travels inserted");
                    travelsCallback.onSuccessFromLocal(new TravelsResponse(travelsList));
                }
            } catch (Exception e) {
                Log.d(TAG, "Error inserting travels:", e);
                travelsCallback.onFailureFromLocal(e);
            }
        });
    }

    @Override
    public void deleteTravel(Travels travel) {
        TravelsRoomDatabase.databaseWriteExecutor.execute(() -> {
            try {
                int deleted = travelsDao.delete(travel);
                if (deleted == 0) {
                    travelsCallback.onFailureFromLocal(new Exception("No travels deleted"));
                } else {
                    travelsCallback.onSuccessDeletionFromLocal(travel);
                }
            } catch (Exception e) {
                travelsCallback.onFailureFromLocal(e);
            }
        });
    }

    @Override
    public void deleteAll() {
        TravelsRoomDatabase.databaseWriteExecutor.execute(() -> {
            try {
                int deleted = travelsDao.deleteAll();
                if (deleted == 0) {
                    travelsCallback.onFailureFromLocal(new Exception("No travels deleted"));
                } else {
                    travelsCallback.onSuccessDeletion();
                }
            } catch (Exception e) {
                travelsCallback.onFailureFromLocal(e);
            }
        });
    }

    @Override
    public void deleteAllAfterSync(List<Travels> travelsList) {
        TravelsRoomDatabase.databaseWriteExecutor.execute(() -> {
            travelsDao.deleteAll();
            travelsCallback.onSuccessDeletionAfterSync(travelsList);
        });
    }

}
