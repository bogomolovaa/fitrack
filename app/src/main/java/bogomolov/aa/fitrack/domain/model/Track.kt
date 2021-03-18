package bogomolov.aa.fitrack.domain.model

import android.annotation.SuppressLint
import bogomolov.aa.fitrack.domain.douglasPeucker
import java.text.SimpleDateFormat
import java.util.*

data class Track(
    var id: Long = 0,
    var startPointId: Long = 0,
    var endPointId: Long = 0,
    var startSmoothedPointId: Long = 0,
    var endSmoothedPointId: Long = 0,
    var startTime: Long = 0,
    var endTime: Long = 0,
    var tag: String? = null,
    var distance: Double = 0.0
) {

    fun getTimeString(): String {
        if (startPointId == 0L) return "0:00"
        val time = if (isOpened()) System.currentTimeMillis() else endTime
        var deltaTime = time - startTime;
        val hours = deltaTime / (3600 * 1000)
        deltaTime %= 3600 * 1000
        val minutes = deltaTime / (60 * 1000)
        deltaTime %= 60 * 1000
        val seconds = deltaTime / 1000;
        return "%02d".format(hours) + ":" + "%02d".format(minutes) + ":" + "%02d".format(seconds);
    }

    @SuppressLint("SimpleDateFormat")
    fun name() = SimpleDateFormat("dd.MM.yyyy HH:mm").format(Date(startTime))

    fun isOpened(): Boolean = endTime == 0L

    fun getSpeed(): Double {
        val time = if (isOpened()) System.currentTimeMillis() else endTime
        return 3.6 * distance / ((time - startTime) / 1000.0)
    }

    fun getSpeed(currentDistance: Double): Double {
        val time = if (isOpened()) System.currentTimeMillis() else endTime
        return currentDistance / ((time - startTime) / 1000.0)
    }

    fun getStartPointId(smoothed: Int) = if (smoothed == RAW) startPointId else startSmoothedPointId

    fun getEndPointId(smoothed: Int) = if (smoothed == RAW) endPointId else endSmoothedPointId
}

const val EPSILON = 25.0

fun smooth(rawPoints: List<Point>) = douglasPeucker(rawPoints, EPSILON)

fun sumTracks(tracks: List<Track>): Track {
    val sumTrack = Track()
    if (tracks.isEmpty()) return sumTrack
    sumTrack.distance = tracks.map { it.distance }.sum()
    sumTrack.endTime = System.currentTimeMillis()
    sumTrack.startTime =
        System.currentTimeMillis() - tracks.map { it.endTime - it.startTime }.sum();
    return sumTrack
}

fun getCurrentSpeed(points: List<Point>): Double {
    val distance = if (points.size > 1) distance(points.last(), points[points.size - 2]) else 0.0
    val seconds =
        if (points.size > 1) (points.last().time - points[points.size - 2].time) / 1000.0 else 0.0
    return if (seconds > 0) distance / seconds else 0.0
}