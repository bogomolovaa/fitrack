package bogomolov.aa.fitrack.domain

import android.util.Log
import bogomolov.aa.fitrack.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

const val MIN_TRACK_DISTANCE = 150f
const val STAY_DIAMETER = 50.0
const val STAY_TIMEOUT = 3 * 60 * 1000L

@Singleton
class UseCases @Inject constructor(
    private val repository: Repository,
    private val mapSaver: MapSaver
) {

    suspend fun onNewPoint(newPoint: Point) {
        repository.addPoint(newPoint)
        val lastTrack = repository.getLastTrack()
        Log.i("test", "onNewPoint $newPoint lastTrack $lastTrack")
        if (lastTrack?.isOpened() == true) {
            tryFinish(lastTrack)
        } else {
            tryStart(lastTrack)
        }
    }

    suspend fun onStopTracking() {
        val lastTrack = repository.getLastTrack()
        if (lastTrack?.isOpened() == true) finishTrack(openedTrack = lastTrack)
        repository.deletePointsAfterLastTrack(lastTrack)
    }

    private suspend fun tryFinish(openedTrack: Track): Boolean {
        val points = repository.getTrackPoints(openedTrack, RAW).toMutableList()
        getStopMotionPoint(points, STAY_DIAMETER, STAY_TIMEOUT)?.let { point ->
            val trackPoints = points.subList(0, points.indexOf(point) + 1)
            repository.deletePointsInRange(point.id, points.last().id)
            trackPoints.add(points.last())
            finishTrack(trackPoints, openedTrack, point.time)
            return true
        }
        return false
    }

    private fun tryStart(lastTrack: Track?): Boolean {
        val points = repository.getPointsAfterLastTrack(lastTrack)
        getStartMotionPoint(points, STAY_DIAMETER, STAY_TIMEOUT)?.let { point ->
            startTrack(point)
            return true
        }
        return false
    }

    private fun getStopMotionPoint(points: List<Point>, diameter: Double, timeout: Long): Point? {
        if (points.size < 2) return null
        var sumDistance = 0.0
        for (i in (points.size - 2) downTo  0) {
            sumDistance += distance(points[i + 1], points[i])
            if (points.last().time - points[i].time > timeout
                && distance(points.last(), points[i]) <= diameter
                && sumDistance < 2 * diameter
            ) return points[i]
            if (points.last().time - points[i].time > 2 * timeout) break
        }
        return null
    }

    private fun getStartMotionPoint(points: List<Point>, diameter: Double, timeout: Long) =
        kotlin.runCatching {
            points.first {
                points.last().time - it.time < timeout && distance(points.last(), it) > diameter
            }
        }.getOrNull()

    fun startTrack(lastPoint: Point, startTime: Long? = null) {
        val track = Track(startPointId = lastPoint.id, startTime = startTime ?: lastPoint.time)
        repository.addTrack(track)
    }

    suspend fun finishTrack(
        points1: List<Point>? = null,
        openedTrack: Track,
        time: Long = System.currentTimeMillis()
    ) {
        val points = points1 ?: repository.getTrackPoints(openedTrack, RAW)
        if (points.isEmpty()) return
        val smoothed = smooth(points)
        val smoothedPoints = smoothed.map { it.copy(id = 0, smoothed = SMOOTHED) }
        for (point in smoothedPoints) repository.addPoint(point)
        openedTrack.endPointId = points.last().id
        openedTrack.endTime = time
        openedTrack.startSmoothedPointId = smoothedPoints.first().id
        openedTrack.endSmoothedPointId = smoothedPoints.last().id
        openedTrack.distance = sumDistance(smoothedPoints)
        repository.save(openedTrack)
        repository.deleteInnerRawPoints(openedTrack)
        if (openedTrack.distance < MIN_TRACK_DISTANCE) {
            repository.deleteTracks(openedTrack.id)
        } else {
            mapSaver.save(openedTrack, smoothedPoints, 600, 400)
        }
    }
}