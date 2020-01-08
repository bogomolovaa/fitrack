package bogomolov.aa.fitrack.repository.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import bogomolov.aa.fitrack.repository.entities.TrackEntity;

@Dao
public interface TrackDao {

    @Insert
    long insert(TrackEntity trackEntity);

    @Update
    void update(TrackEntity trackEntity);

    @Query("DELETE from TrackEntity where id in (:ids)")
    void deleteByIds(Long... ids);

    @Query("UPDATE TrackEntity set tag = :tag where id in (:ids)")
    void updateTracks(String tag, List<Long> ids);

    @Query("SELECT * from TrackEntity where id in (:ids)")
    List<TrackEntity> getTracks(Long... ids);

    @Query("UPDATE TrackEntity SET tag = :newTag where tag =:tag ")
    void updateTags(String tag, String newTag);

    @Query("SELECT * from TrackEntity where id = (SELECT max(id) from TrackEntity)")
    TrackEntity getLastTrack();

    @Query("SELECT * from TrackEntity where startTime >= :time1 and startTime < :time2 and tag = :tag and endTime > 0")
    List<TrackEntity> getFinishedTracks(long time1, long time2, String tag);

    @Query("SELECT * from TrackEntity where startTime >= :time1 and startTime < :time2 and endTime > 0")
    List<TrackEntity> getFinishedTracks(long time1, long time2);
}
