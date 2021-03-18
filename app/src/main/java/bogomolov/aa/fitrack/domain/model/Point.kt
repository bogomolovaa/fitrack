package bogomolov.aa.fitrack.domain.model

import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

const val SMOOTHED = 1
const val RAW = 0

data class Point(
    var id: Long = 0,
    val time: Long = 0,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val smoothed: Int = 0
)

fun distance(point1: Point, point2: Point): Double {
    val lat1 = point1.lat
    val lon1 = point1.lng
    val lat2 = point2.lat
    val lon2 = point2.lng
    if (lat1 == lat2 && lon1 == lon2) {
        return 0.0
    } else {
        val theta = lon1 - lon2
        var dist = sin(toRadians(lat1)) * sin(toRadians(lat2)) +
                cos(toRadians(lat1)) * cos(toRadians(lat2)) * cos(toRadians(theta))
        dist = acos(dist)
        dist = toDegrees(dist)
        dist *= 60 * 1.1515
        dist *= 1.609344 * 1000
        return dist
    }
}

fun sumDistance(points: List<Point>) =
    points.zipWithNext().map { distance(it.first, it.second) }.sum()