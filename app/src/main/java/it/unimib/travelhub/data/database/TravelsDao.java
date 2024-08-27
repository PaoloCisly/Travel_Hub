package it.unimib.travelhub.data.database;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import it.unimib.travelhub.model.Travels;
@Dao
public interface TravelsDao {
    @Query("SELECT * FROM travels") // ORDER BY startDate DESC
    List<Travels> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertTravelsList(List<Travels> travelsList);

    @Update
    int updateSingleTravel(Travels travels);

    @Delete
    int delete(Travels travels);

    @Query("DELETE FROM travels")
    int deleteAll();

}
