package bogomolov.aa.fitrack.repository.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import bogomolov.aa.fitrack.repository.entities.PointEntity;
import bogomolov.aa.fitrack.repository.entities.TrackEntity;

@Dao
public interface PointDao {

    @Insert
    long insert(PointEntity pointEntity);

    @Query("DELETE from PointEntity where id > :startId and id < :endId and smoothed = :smoothed")
    void deleteByIds(long startId, long endId, int smoothed);

    @Query("DELETE from PointEntity where id > :id and smoothed = :smoothed")
    void deleteByIdsGreater(long id, int smoothed);

    @Query("SELECT * from PointEntity where id > :id and smoothed = :smoothed")
    List<PointEntity> getPointsByIdsGreater(long id, int smoothed);

    @Query("SELECT * from PointEntity where smoothed =:smoothed and id = (SELECT max(id) from PointEntity where smoothed =:smoothed)")
    PointEntity getLastPoint(int smoothed);

    @Query("SELECT * from PointEntity where smoothed = :smoothed and id >= :startId and id <=:endId order by id asc")
    List<PointEntity> getPoints(long startId,long endId, int smoothed);

    @Query("SELECT * from PointEntity where smoothed = :smoothed and id >= :startId  order by id asc")
    List<PointEntity> getPoints(long startId, int smoothed);
}
