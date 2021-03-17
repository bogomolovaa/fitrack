package bogomolov.aa.fitrack.domain.model

data class Point(
    var id: Long = 0,
    var time: Long = 0,
    var lat:Double = 0.0,
    var lng:Double = 0.0,
    var smoothed: Int = 0
){
    constructor(lat: Double, lng: Double) : this() {
        this.lat = lat
        this.lng = lng
    }

    constructor(latlng: DoubleArray) : this() {
        lat = latlng[0]
        lng = latlng[1]
    }

    constructor(time: Long, lat: Double, lng: Double) : this() {
        this.time = time
        this.lat = lat
        this.lng = lng
    }
}

const val SMOOTHED = 1

const val RAW = 0

fun distance(point1: Point, point2: Point): Double {
    val lat1 = point1.lat
    val lon1 = point1.lng
    val lat2 = point2.lat
    val lon2 = point2.lng
    if (lat1 == lat2 && lon1 == lon2) {
        return 0.0
    } else {
        val theta = lon1 - lon2
        var dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta))
        dist = Math.acos(dist)
        dist = Math.toDegrees(dist)
        dist *= 60 * 1.1515
        dist *= 1.609344 * 1000
        return dist
    }
}

fun getTrackDistance(points: List<Point>): Double =
    if (points.size > 1) points.zipWithNext().map { distance(it.first, it.second) }.sum() else 0.0;