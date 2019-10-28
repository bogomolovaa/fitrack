package bogomolov.aa.fitrack.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class RamerDouglasPeucker {

    private static double distanceBetweenPoints(Point v, Point w) {
        return GeoUtils.distance(v, w);
    }

    private static double perpendicularDistance(Point p, Point v, Point w) {
        final double l2 = distanceBetweenPoints(v, w);
        if (l2 == 0)
            return distanceBetweenPoints(p, v);
        final double t = ((p.getLat() - v.getLat()) * (w.getLat() - v.getLat()) + (p.getLng() - v.getLng()) * (w.getLng() - v.getLng())) / l2;
        if (t < 0)
            return distanceBetweenPoints(p, v);
        if (t > 1)
            return distanceBetweenPoints(p, w);
        return distanceBetweenPoints(p, new Point(v.getTime(), (v.getLat() + t * (w.getLat() - v.getLat())), (v.getLng() + t * (w.getLng() - v.getLng()))));
    }

    private static void douglasPeucker(List<Point> list, int s, int e, double epsilon, List<Point> resultList) {
        // Find the point with the maximum distance
        double dmax = 0;
        int index = 0;

        final int start = s;
        final int end = e - 1;
        for (int i = start + 1; i < end; i++) {
            // Point
            Point p = list.get(i);
            // Start
            Point v = list.get(start);
            // End
            Point w = list.get(end);
            final double d = perpendicularDistance(p, v, w);

            if (d > dmax) {
                index = i;
                dmax = d;
            }
        }
        Log.i("RamerDouglasPeucker","perpendicularDistance "+dmax+" epsilon "+epsilon);
        // If max distance is greater than epsilon, recursively simplify
        if (dmax > epsilon) {
            // Recursive call
            douglasPeucker(list, s, index, epsilon, resultList);
            douglasPeucker(list, index, e, epsilon, resultList);
        } else {
            if ((end - start) > 0) {
                resultList.add(list.get(start));
                resultList.add(list.get(end));
            } else {
                resultList.add(list.get(start));
            }
        }
    }

    /**
     * Given a curve composed of line segments find a similar curve with fewer points.
     *
     * @param list    List of Double[] points (x,y)
     * @param epsilon Distance dimension
     * @return Similar curve with fewer points
     */
    public static List<Point> douglasPeucker(List<Point> list, double epsilon) {
        List<Point> resultList = new ArrayList<>();
        Log.i("start","start points "+list.size());
        douglasPeucker(list, 0, list.size(), epsilon, resultList);
        return resultList;
    }
}
