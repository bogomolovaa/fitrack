package bogomolov.aa.fitrack.core

import bogomolov.aa.fitrack.core.model.Point
import java.util.ArrayList

private fun perpendicularDistance(px: Double, py: Double, vx: Double, vy: Double, wx: Double, wy: Double): Double {
    val l2 = (vx - wx) * (vx - wx) + (vy - wy) * (vy - wy)
    if (l2 == 0.0)
        return Point.distance(Point(px, py), Point(vx, vy))
    val t = ((px - vx) * (wx - vx) + (py - vy) * (wy - vy)) / l2
    if (t < 0)
        return Point.distance(Point(px, py), Point(vx, vy))
    return if (t > 1) Point.distance(Point(px, py), Point(wx, wy)) else Point.distance(Point(px, py), Point(vx + t * (wx - vx), vy + t * (wy - vy)))
}

private fun douglasPeucker(list: List<Point>, s: Int, e: Int, epsilon: Double, resultList: MutableList<Point>){
    var dmax = 0.0;
    var index = 0;

    val start = s;
    val end = e-1;
    for(i in start+1 until end){
        val p = list[i]
        val v = list[start]
        val w = list[end]
        val d = perpendicularDistance(p.lat, p.lng, v.lat, v.lng, w.lat, w.lng)
        if(d > dmax){
            index = i
            dmax = d
        }
        if(dmax > epsilon){
            douglasPeucker(list, s, index, epsilon, resultList)
            douglasPeucker(list, index, e, epsilon, resultList)
        }else{
            resultList.add(list[start])
        }
    }
}

fun douglasPeucker(list: List<Point>, epsilon: Double): List<Point> {
    if (list.size < 3) return list
    val resultList = ArrayList<Point>()
    douglasPeucker(list, 0, list.size, epsilon, resultList)
    return resultList
}