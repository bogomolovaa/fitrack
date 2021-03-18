package bogomolov.aa.fitrack.domain

import bogomolov.aa.fitrack.domain.model.Point
import bogomolov.aa.fitrack.domain.model.distance

fun douglasPeucker(list: List<Point>, epsilon: Double): List<Point> {
    if (list.size < 3) return list
    val result = LinkedHashSet<Point>()
    douglasPeucker(list, 0, list.size - 1, epsilon, result)
    result.add(list.last())
    return ArrayList(result)
}

private fun douglasPeucker(
    list: List<Point>,
    s: Int,
    e: Int,
    epsilon: Double,
    result: LinkedHashSet<Point>
) {
    var dmax = 0.0
    var index = 0
    val end = e - 1
    for (i in s + 1 until end) {
        val p = list[i]
        val v = list[s]
        val w = list[end]
        val d = perpendicularDistance(p, v, w)
        if (d > dmax) {
            index = i
            dmax = d
        }
    }
    if (dmax > epsilon) {
        douglasPeucker(list, s, index, epsilon, result)
        douglasPeucker(list, index, e, epsilon, result)
    } else {
        if (end - s > 0) {
            result.add(list[s])
            result.add(list[end])
        } else {
            result.add(list[s])
        }
    }
}

private fun perpendicularDistance(p: Point, v: Point, w: Point): Double {
    val l2 = (v.lat - w.lat) * (v.lat - w.lat) + (v.lng - w.lng) * (v.lng - w.lng)
    if (l2 == 0.0) return distance(point(p.lat, p.lng), point(v.lat, v.lng))
    val t = ((p.lat - v.lat) * (w.lat - v.lat) + (p.lng - v.lng) * (w.lng - v.lng)) / l2
    if (t < 0) return distance(point(p.lat, p.lng), point(v.lat, v.lng))
    return if (t > 1) distance(point(p.lat, p.lng), point(w.lat, w.lng)) else distance(
        point(p.lat, p.lng), point(v.lat + t * (w.lat - v.lat), v.lng + t * (w.lng - v.lng))
    )
}

private fun point(lat: Double, lng: Double) = Point(lat = lat, lng = lng)