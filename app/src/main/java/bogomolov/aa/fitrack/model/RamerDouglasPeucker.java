package bogomolov.aa.fitrack.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class RamerDouglasPeucker {


    private static double perpendicularDistance(double px, double py, double vx, double vy, double wx, double wy) {
        final double l2 = (vx - wx) * (vx - wx) + (vy - wy) * (vy - wy);
        if (l2 == 0)
            return Point.distance(new Point(px, py), new Point(vx, vy));
        final double t = ((px - vx) * (wx - vx) + (py - vy) * (wy - vy)) / l2;
        if (t < 0)
            return Point.distance(new Point(px, py), new Point(vx, vy));
        if (t > 1)
            return Point.distance(new Point(px, py), new Point(wx, wy));
        return Point.distance(new Point(px, py), new Point((vx + t * (wx - vx)), (vy + t * (wy - vy))));
    }

    private static void douglasPeucker(List<Point> list, int s, int e, double epsilon, List<Point> resultList) {
        double dmax = 0;
        int index = 0;

        final int start = s;
        final int end = e - 1;
        for (int i = start + 1; i < end; i++) {
            Point p = list.get(i);
            Point v = list.get(start);
            Point w = list.get(end);
            final double d = perpendicularDistance(p.getLat(), p.getLng(), v.getLat(), v.getLng(), w.getLat(), w.getLng());
            if (d > dmax) {
                index = i;
                dmax = d;
            }
        }
        //if (e - s < 5) TrackerService.stringBuffer.append("perpendicularDistance " + dmax + " epsilon " + epsilon + "\n");
        if (dmax > epsilon) {
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

    public static List<Point> douglasPeucker(List<Point> list, double epsilon) {
        if (list.size() < 3) return list;
        List<Point> resultList = new ArrayList<>();
        douglasPeucker(list, 0, list.size(), epsilon, resultList);
        return resultList;
    }
}
