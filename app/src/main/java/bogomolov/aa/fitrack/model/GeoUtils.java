package bogomolov.aa.fitrack.model;

import java.util.List;

public class GeoUtils {

    public static double distance(Point point1, Point point2) {
        double lat1 = point1.getLat();
        double lon1 = point1.getLng();
        double lat2 = point2.getLat();
        double lon2 = point2.getLng();
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        } else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            dist = dist * 1.609344 * 1000;
            return dist;
        }
    }

    public static double getTrackDistance(List<Point> points) {
        double distance = 0;
        if (points.size() > 1) for (int i = 0; i < points.size() - 1; i++)
            distance += distance(points.get(i), points.get(i + 1));
        return distance;
    }
}
