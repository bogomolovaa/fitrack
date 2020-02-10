package bogomolov.aa.fitrack.repository.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

import bogomolov.aa.fitrack.repository.entities.TrackEntity

@Dao
interface TrackDao {

    @get:Query("SELECT * from TrackEntity where id = (SELECT max(id) from TrackEntity)")
    val lastTrack: TrackEntity

    @Insert
    fun insert(trackEntity: TrackEntity): Long

    @Update
    fun update(trackEntity: TrackEntity)

    @Query("DELETE from TrackEntity where id in (:ids)")
    fun deleteByIds(vararg ids: Long)

    @Query("UPDATE TrackEntity set tag = :tag where id in (:ids)")
    fun updateTracks(tag: String, ids: List<Long>)

    @Query("SELECT * from TrackEntity where id in (:ids)")
    fun getTracks(vararg ids: Long): List<TrackEntity>

    @Query("UPDATE TrackEntity SET tag = :newTag where tag =:tag ")
    fun updateTags(tag: String, newTag: String?)

    @Query("SELECT * from TrackEntity where startTime >= :time1 and startTime < :time2 and endTime > 0")
    fun getFinishedTracksDataSource(time1: Long, time2: Long): DataSource.Factory<Int, TrackEntity>

    @Query("SELECT * from TrackEntity where startTime >= :time1 and startTime < :time2 and tag = :tag and endTime > 0")
    fun getFinishedTracks(time1: Long, time2: Long, tag: String): List<TrackEntity>

    @Query("SELECT * from TrackEntity where startTime >= :time1 and startTime < :time2 and endTime > 0")
    fun getFinishedTracks(time1: Long, time2: Long): List<TrackEntity>
}
