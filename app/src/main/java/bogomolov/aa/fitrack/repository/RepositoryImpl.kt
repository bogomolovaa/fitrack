package bogomolov.aa.fitrack.repository

import androidx.paging.DataSource
import androidx.room.Transaction
import bogomolov.aa.fitrack.domain.Repository
import bogomolov.aa.fitrack.domain.model.Point
import bogomolov.aa.fitrack.domain.model.RAW
import bogomolov.aa.fitrack.domain.model.Tag
import bogomolov.aa.fitrack.domain.model.Track
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepositoryImpl @Inject
constructor(private val db: AppDatabase) : Repository {

    override fun getTags(): List<Tag> = entityToModel(db.tagDao().tags, Tag::class.java)

    override fun getLastTrack(): Track? = entityToModel(db.trackDao().getLastTrack())

    override fun getLastRawPoint(): Point? = entityToModel(db.pointDao().getLastPoint(RAW))

    override fun save(track: Track) = db.trackDao().update(modelToEntity(track))

    override fun updateTracks(tag: String, ids: List<Long>) = db.trackDao().updateTracks(tag, ids)

    override fun addTag(tag: Tag) {
        tag.id = db.tagDao().insert(modelToEntity(tag))
    }

    override fun addPoint(point: Point) {
        point.id = db.pointDao().insert(modelToEntity(point))
    }

    override fun addTrack(track: Track) {
        track.id = db.trackDao().insert(modelToEntity(track))
    }

    override fun getTracks(vararg ids: Long): List<Track> =
            entityToModel(db.trackDao().getTracks(*ids), Track::class.java)

    @Transaction
    override fun deleteTag(tag: Tag) {
        db.trackDao().updateTags(tag.name!!, null)
        db.tagDao().delete(modelToEntity(tag))
    }

    override fun deleteTracks(vararg ids: Long) {
        db.trackDao().deleteByIds(*ids)
    }

    override fun deleteInnerRawPoints(track: Track) {
        db.pointDao().deleteByIds(track.startPointId, track.endPointId, RAW)
    }

    override fun deletePointsInRange(startId: Long, endId: Long) {
        db.pointDao().deleteByIds(startId, endId, RAW)
    }

    override fun deletePointsAfterLastTrack(lastTrack: Track?) {
        db.pointDao().deleteByIdsGreater(lastTrack?.endPointId ?: 0, RAW)
    }

    override fun getPointsAfterLastTrack(lastTrack: Track?): List<Point> {
        val points = db.pointDao().getPointsByIdsGreater(lastTrack?.endPointId ?: 0, RAW)
        return entityToModel(points, Point::class.java)
    }

    override fun getTrackPoints(track: Track, smoothed: Int): List<Point> {
        val points = if (track.isOpened())
            db.pointDao().getPoints(track.getStartPointId(smoothed), smoothed)
        else
            db.pointDao().getPoints(track.getStartPointId(smoothed), track.getEndPointId(smoothed), smoothed)
        return entityToModel(points, Point::class.java)
    }

    override fun getFinishedTracks(datesRange: Array<Date>, tag: String?): List<Track> {
        val tracks = if (tag != null)
            db.trackDao().getFinishedTracks(datesRange[0].time, datesRange[1].time, tag)
        else
            db.trackDao().getFinishedTracks(datesRange[0].time, datesRange[1].time)
        return entityToModel(tracks, Track::class.java)
    }

    override fun getFinishedTracksDataSource(datesRange: Array<Date>): DataSource.Factory<Int, Track> {
        val factory = db.trackDao().getFinishedTracksDataSource(datesRange[0].time, datesRange[1].time)
        return factory.map { entityToModel(it) }
    }
}
