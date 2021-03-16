package bogomolov.aa.fitrack.domain

import android.util.Log
import bogomolov.aa.fitrack.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

const val MIN_TRACK_DISTANCE = 150
const val MAX_STAY_RADIUS = 50
const val STAY_TIMEOUT = 3 * 60 * 1000
const val STOP_TIMEOUT = 5 * 60 * 1000

@Singleton
class UseCases @Inject constructor(
    private val repository: Repository,
    private val mapSaver: MapSaver
) {

    suspend fun onNewPoint(newPoint: Point, startTrackingTime: Long): Boolean {
        repository.addPoint(newPoint)
        val lastTrack = repository.getLastTrack()
        Log.i("test", "onNewPoint $newPoint lastTrack $lastTrack")
        var stopTracking = false
        val openedTrack = if (lastTrack?.isOpened() == true) lastTrack else null
        if (openedTrack == null) {
            val shouldStop = System.currentTimeMillis() - startTrackingTime > STOP_TIMEOUT
            if (!tryStart(lastTrack) && shouldStop) {
                repository.deletePointsAfterLastTrack(lastTrack)
                stopTracking = true
            }
        } else {
            val points = ArrayList(repository.getTrackPoints(openedTrack, RAW))
            points.add(newPoint)
            stopTracking = tryFinish(points, openedTrack)
        }
        return stopTracking
    }

    private suspend fun tryFinish(
        points: ArrayList<Point>,
        openedTrack: Track
    ): Boolean {
        for (i in points.indices.reversed()) {
            if (distance(points.last(), points[i]) <= MAX_STAY_RADIUS) {
                if (points.last().time - points[i].time > STAY_TIMEOUT) {
                    val trackPoints = points.subList(0, i + 1)
                    trackPoints.add(points.last())
                    finishTrack(trackPoints, openedTrack, points[i].time)
                    return true
                }
            }
        }
        return false
    }

    private fun tryStart(lastTrack: Track?): Boolean {
        val points = repository.getPointsAfterLastTrack(lastTrack)
        if (points.size > 1) {
            val now = System.currentTimeMillis()
            val firstPoint = points.first { now - it.time < STAY_TIMEOUT }
            val lastPoint = points.last()
            if (distance(firstPoint, lastPoint) > MAX_STAY_RADIUS) {
                startTrack(lastPoint)
                return true
            }
        }
        return false
    }

    fun startTrack(lastPoint: Point, startTime: Long? = null) {
        val track = Track()
        track.startPointId = lastPoint.id
        track.startTime = startTime ?: lastPoint.time
        repository.addTrack(track)
    }

    suspend fun finishTrack(
        points1: List<Point>? = null,
        openedTrack: Track,
        time: Long = System.currentTimeMillis()
    ) {
        val points = points1 ?: repository.getTrackPoints(openedTrack, RAW)
        if (points.isEmpty()) return
        val lastPoint = points[points.size - 1]
        Log.i("test", "finishTrack points ${points.size} track $openedTrack")
        val smoothed = smooth(points)
        Log.i("test", "smoothed ${smoothed.size}")
        val smoothedPoints = clonePoints(smoothed)
        for (point in smoothedPoints) {
            point.smoothed = SMOOTHED
            repository.addPoint(point)
        }
        openedTrack.endPointId = lastPoint.id
        openedTrack.endTime = time
        openedTrack.startSmoothedPointId = smoothedPoints[0].id
        openedTrack.endSmoothedPointId = smoothedPoints[smoothedPoints.size - 1].id
        openedTrack.distance = getTrackDistance(smoothedPoints)
        repository.save(openedTrack)
        repository.deleteInnerRawPoints(openedTrack)

        if (openedTrack.distance < MIN_TRACK_DISTANCE) {
            repository.deleteTracks(openedTrack.id)
        } else {
            mapSaver.save(openedTrack, smoothedPoints, 600, 400)
        }
    }

}