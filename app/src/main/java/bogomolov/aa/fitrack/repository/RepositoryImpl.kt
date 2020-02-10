package bogomolov.aa.fitrack.repository

import android.util.Log

import androidx.paging.DataSource
import androidx.room.Transaction

import java.util.Date

import javax.inject.Inject
import javax.inject.Singleton

import bogomolov.aa.fitrack.core.model.Point
import bogomolov.aa.fitrack.core.model.Tag
import bogomolov.aa.fitrack.core.model.Track
import bogomolov.aa.fitrack.repository.entities.PointEntity
import bogomolov.aa.fitrack.repository.entities.TrackEntity

import bogomolov.aa.fitrack.repository.*

@Singleton
class RepositoryImpl @Inject
constructor(private val db: AppDatabase) : Repository {

    override val tags: List<Tag>
        get() = entityToModel(db.tagDao().tags, Tag::class.java)

    override val lastTrack: Track?
        get() = entityToModel(db.trackDao().lastTrack)

    override val lastRawPoint: Point?
        get() = entityToModel(db.pointDao().getLastPoint(Point.RAW))

    override fun save(track: Track) {
        db.trackDao().update(modelToEntity(track)!!)
    }

    override fun updateTracks(tag: String, ids: List<Long>) {
        db.trackDao().updateTracks(tag, ids)
    }

    override fun addTag(tag: Tag) {
        tag.id = db.tagDao().insert(modelToEntity(tag)!!)
    }

    override fun addPoint(point: Point) {
        point.id = db.pointDao().insert(modelToEntity(point)!!)
    }

    override fun addTrack(track: Track) {
        track.id = db.trackDao().insert(modelToEntity(track)!!)
    }

    override fun getTracks(vararg ids: Long): List<Track> {
        return entityToModel(db.trackDao().getTracks(*ids), Track::class.java)
    }

    @Transaction
    override fun deleteTag(tag: Tag) {
        db.trackDao().updateTags(tag.name!!, null)
        db.tagDao().delete(modelToEntity(tag)!!)
    }

    override fun deleteTracks(vararg ids: Long) {
        db.trackDao().deleteByIds(*ids)
    }

    override fun deleteInnerRawPoints(track: Track) {
        db.pointDao().deleteByIds(track.startPointId, track.endPointId, Point.RAW)
    }

    override fun deletePointsAfterLastTrack(lastTrack: Track) {
        val lastId = lastTrack.endPointId
        db.pointDao().deleteByIdsGreater(lastId, Point.RAW)
    }

    override fun close() {
        db.close()
    }

    override fun getPointsAfterLastTrack(lastTrack: Track): List<Point> {
        val lastId = lastTrack.endPointId
        val points = db.pointDao().getPointsByIdsGreater(lastId, Point.RAW)
        return entityToModel(points, Point::class.java)
    }

    override fun getTrackPoints(track: Track, smoothed: Int): List<Point> {
        var points: List<PointEntity>?
        if (track.isOpened()) {
            points = db.pointDao().getPoints(track.getStartPointId(smoothed), smoothed)
        } else {
            points = db.pointDao().getPoints(track.getStartPointId(smoothed), track.getEndPointId(smoothed), smoothed)
        }
        return entityToModel(points, Point::class.java)
    }

    override fun getFinishedTracks(datesRange: Array<Date>, tag: String?): List<Track> {
        val tracks = if (tag != null) db.trackDao().getFinishedTracks(datesRange[0].time, datesRange[1].time, tag) else db.trackDao().getFinishedTracks(datesRange[0].time, datesRange[1].time)
        return entityToModel(tracks, Track::class.java)
    }

    override fun getFinishedTracksDataSource(datesRange: Array<Date>): DataSource.Factory<Int, Track> {
        val factory = db.trackDao().getFinishedTracksDataSource(datesRange[0].time, datesRange[1].time)
        return factory.map { entityToModel(it) }
    }

}
