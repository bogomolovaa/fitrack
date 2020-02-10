package bogomolov.aa.fitrack.core

import android.content.Context
import bogomolov.aa.fitrack.android.saveUI
import bogomolov.aa.fitrack.core.model.Point
import bogomolov.aa.fitrack.core.model.Track
import bogomolov.aa.fitrack.repository.Repository
import java.util.ArrayList

const val MIN_TRACK_DISTANCE = 150;

fun onNewPoint(point: Point, repository: Repository, context: Context, startLocationUpdateTime: Long, UPDATE_INTERVAL: Long): Boolean {
    val lastTrack = repository.getLastTrack()
    val openedTrack = if (lastTrack != null && !lastTrack.isOpened()) null else lastTrack
    if (openedTrack == null) {
        repository.addPoint(point)
        val points = repository.getPointsAfterLastTrack(lastTrack)
        if (points.size > 1) {
            var firstPoint = points[0]
            for (i in 0 until points.size - 1)
                if (System.currentTimeMillis() - points[i].time < 3 * 60 * 1000) {
                    firstPoint = points[i]
                    break
                }
            val lastPoint = points[points.size - 1]
            val distance = Point.distance(firstPoint, lastPoint)
            if (distance > 50) {
                startTrack(repository, lastPoint)
            } else if (System.currentTimeMillis() - startLocationUpdateTime > 5 * 60 * 1000) {
                repository.deletePointsAfterLastTrack(lastTrack)
                return true
            }
        }
    } else {
        val points = ArrayList(repository.getTrackPoints(openedTrack, Point.RAW))
        var trackPoints : MutableList<Point> = points
        var lastPoint = points[points.size - 1]
        if (!(Point.distance(point, lastPoint) > 200 || System.currentTimeMillis() - lastPoint.time <= 2 * UPDATE_INTERVAL)) {
            points.add(point)
            lastPoint = point
            repository.addPoint(lastPoint)
        }
        for (i in points.indices.reversed()) {
            if (Point.distance(lastPoint, points[i]) <= 50) {
                if (lastPoint.time - points[i].time > 3 * 60 * 1000) {
                    trackPoints = trackPoints.subList(0, i + 1)
                    trackPoints.add(lastPoint)
                    finishTrack(repository, context, trackPoints, openedTrack, points[i].time)
                    return true
                }
            }
        }
    }
    return false
}

fun startTrack(repository: Repository, lastPoint: Point) {
    val track = Track()
    track.startPointId = lastPoint.id
    track.startTime = lastPoint.time
    repository.addTrack(track)
}

fun finishTrack(repository: Repository, context: Context, points: List<Point>, openedTrack: Track, time: Long) {
    if (points.size == 0) return
    val lastPoint = points[points.size - 1]
    val smoothedPoints = Point.clonePoints(Track.smooth(points))
    for (point in smoothedPoints) {
        point.smoothed = Point.SMOOTHED
        repository.addPoint(point)
    }
    openedTrack.endPointId = lastPoint.id
    openedTrack.endTime = time
    openedTrack.startSmoothedPointId = smoothedPoints[0].id
    openedTrack.endSmoothedPointId = smoothedPoints[smoothedPoints.size - 1].id
    openedTrack.distance = Point.getTrackDistance(smoothedPoints)
    repository.save(openedTrack)
    repository.deleteInnerRawPoints(openedTrack)

    if (openedTrack.distance < MIN_TRACK_DISTANCE) {
        repository.deleteTracks(openedTrack.id)
    } else {
        saveUI(context, openedTrack, smoothedPoints, 600, 400)
    }
}