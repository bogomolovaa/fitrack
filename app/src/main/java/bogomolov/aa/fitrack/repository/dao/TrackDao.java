package bogomolov.aa.fitrack.repository.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import bogomolov.aa.fitrack.core.model.Track;
import bogomolov.aa.fitrack.repository.entities.TagEntity;
import bogomolov.aa.fitrack.repository.entities.TrackEntity;

@Dao
public interface TrackDao {

    @Insert
    long insert(TrackEntity trackEntity);

    @Update
    void update(TrackEntity trackEntity);

    @Query("SELECT * from TrackEntity where id in (:ids)")
    List<TrackEntity> getTracks(Long... ids);

}
