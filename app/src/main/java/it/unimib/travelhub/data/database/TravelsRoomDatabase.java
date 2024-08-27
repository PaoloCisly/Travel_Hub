package it.unimib.travelhub.data.database;

import static it.unimib.travelhub.util.Constants.TRAVELS_DATABASE_NAME;
import static it.unimib.travelhub.util.Constants.TRAVELS_DATABASE_VERSION;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.unimib.travelhub.model.Travels;

@Database(entities = {Travels.class}, version = TRAVELS_DATABASE_VERSION)
@TypeConverters({Converters.class})
public abstract class TravelsRoomDatabase extends RoomDatabase {
    public abstract TravelsDao travelsDao();
    private static volatile TravelsRoomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = Runtime.getRuntime().availableProcessors();
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static TravelsRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (TravelsRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            TravelsRoomDatabase.class, TRAVELS_DATABASE_NAME)
                            .fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}
