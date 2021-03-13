package bogomolov.aa.fitrack.domain

import android.util.Log
import bogomolov.aa.fitrack.domain.model.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

const val MIN_TRACK_DISTANCE = 150

@Singleton
class UseCases @Inject constructor(
    private val repository: Repository,
    private val mapSaver: MapSaver
) {

    suspend fun onNewPoint(point: Point, startTime: Long, updateInterval: Long): Boolean {
        val lastTrack = repository.getLastTrack()
        Log.i("test", "onNewPoint $point lastTrack $lastTrack")
        val openedTrack = if (lastTrack?.isOpened() == true) lastTrack else null
        if (openedTrack == null) {
            repository.addPoint(point)
            val points = repository.getPointsAfterLastTrack(lastTrack)
            if (points.size > 1) {
                var firstPoint = points[0]
                for (point1 in points)
                    if (System.currentTimeMillis() - point1.time < 3 * 60 * 1000) {
                        firstPoint = point1
                        break
                    }
                val lastPoint = points[points.size - 1]
                val distance = distance(firstPoint, lastPoint)
                if (distance > 50) {
                    startTrack(lastPoint)
                } else if (System.currentTimeMillis() - startTime > 5 * 60 * 1000) {
                    repository.deletePointsAfterLastTrack(lastTrack)
                    return true
                }
            }
        } else {
            val points = ArrayList(repository.getTrackPoints(openedTrack, RAW))
            var trackPoints: MutableList<Point> = points
            var lastPoint = points[points.size - 1]
            if (!(distance(point, lastPoint) > 200
                        || System.currentTimeMillis() - lastPoint.time <= 2 * updateInterval)
            ) {
                points.add(point)
                lastPoint = point
                repository.addPoint(lastPoint)
            }
            for (i in points.indices.reversed()) {
                if (distance(lastPoint, points[i]) <= 50) {
                    if (lastPoint.time - points[i].time > 3 * 60 * 1000) {
                        trackPoints = trackPoints.subList(0, i + 1)
                        trackPoints.add(lastPoint)
                        finishTrack(trackPoints, openedTrack, points[i].time)
                        return true
                    }
                }
            }
        }
        return false
    }

    fun startTrack(lastPoint: Point) {
        val track = Track()
        track.startPointId = lastPoint.id
        track.startTime = lastPoint.time
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
        Log.i("test","finishTrack points ${points.size}")
        val smoothed = smooth(points)
        Log.i("test","smoothed ${smoothed.size}")
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