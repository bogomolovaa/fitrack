package bogomolov.aa.fitrack.domain

import androidx.paging.DataSource
import bogomolov.aa.fitrack.domain.model.Point
import bogomolov.aa.fitrack.domain.model.Tag
import bogomolov.aa.fitrack.domain.model.Track
import java.util.*

interface Repository {

    fun getTags(): List<Tag>

    fun getLastTrack(): Track?

    fun getLastRawPoint(): Point?

    fun save(track: Track)

    fun updateTracks(tag: String, ids: List<Long>)

    fun getFinishedTracks(datesRange: Array<Date>, tag: String?): List<Track>

    fun getFinishedTracksDataSource(datesRange: Array<Date>): DataSource.Factory<Int, Track>

    fun getTracks(vararg ids: Long): List<Track>

    fun deleteTag(tag: Tag)

    fun deleteTracks(vararg ids: Long)

    fun deleteInnerRawPoints(track: Track)

    fun addTag(tag: Tag)

    fun addPoint(point: Point)

    fun addTrack(track: Track)

    fun getTrackPoints(track: Track, smoothed: Int): List<Point>

    fun getPointsAfterLastTrack(lastTrack: Track?): List<Point>

    fun deletePointsAfterLastTrack(lastTrack: Track?)

}
