package bogomolov.aa.fitrack.domain

import android.util.Log
import bogomolov.aa.fitrack.domain.model.Point
import bogomolov.aa.fitrack.domain.model.distance
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashSet

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
        val d = perpendicularDistance(p.lat, p.lng, v.lat, v.lng, w.lat, w.lng)
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

private fun perpendicularDistance(
    px: Double,
    py: Double,
    vx: Double,
    vy: Double,
    wx: Double,
    wy: Double
): Double {
    val l2 = (vx - wx) * (vx - wx) + (vy - wy) * (vy - wy)
    if (l2 == 0.0) return distance(Point(px, py), Point(vx, vy))
    val t = ((px - vx) * (wx - vx) + (py - vy) * (wy - vy)) / l2
    if (t < 0) return distance(Point(px, py), Point(vx, vy))
    return if (t > 1) distance(Point(px, py), Point(wx, wy)) else distance(
        Point(px, py), Point(
            vx + t * (wx - vx),
            vy + t * (wy - vy)
        )
    )
}