package bogomolov.aa.fitrack.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import bogomolov.aa.fitrack.repository.entities.PointEntity

@Dao
interface PointDao {

    @Insert
    fun insert(pointEntity: PointEntity): Long

    @Query("DELETE from PointEntity where id > :startId and id < :endId and smoothed = :smoothed")
    fun deleteByIds(startId: Long, endId: Long, smoothed: Int)

    @Query("DELETE from PointEntity where id > :id and smoothed = :smoothed")
    fun deleteByIdsGreater(id: Long, smoothed: Int)

    @Query("SELECT * from PointEntity where id > :id and smoothed = :smoothed")
    fun getPointsByIdsGreater(id: Long, smoothed: Int): List<PointEntity>

    @Query("SELECT * from PointEntity where smoothed =:smoothed and id = (SELECT max(id) from PointEntity where smoothed =:smoothed)")
    fun getLastPoint(smoothed: Int): PointEntity

    @Query("SELECT * from PointEntity where smoothed = :smoothed and id >= :startId and id <=:endId order by id asc")
    fun getPoints(startId: Long, endId: Long, smoothed: Int): List<PointEntity>

    @Query("SELECT * from PointEntity where smoothed = :smoothed and id >= :startId  order by id asc")
    fun getPoints(startId: Long, smoothed: Int): List<PointEntity>
}
