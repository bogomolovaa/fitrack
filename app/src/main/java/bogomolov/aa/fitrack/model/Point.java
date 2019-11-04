package bogomolov.aa.fitrack.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Point extends RealmObject {
    public static final int SMOOTHED = 1;
    public static final int RAW = 0;

    @PrimaryKey
    private long id;
    private long time;
    private double lat;
    private double lng;
    private int smoothed;

    public Point clone(){
        Point point = new Point();
        point.time = time;
        point.lat = lat;
        point.lng = lng;
        point.smoothed = smoothed;
        return point;
    }

    public Point() {
    }

    public Point(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public Point(double[] latlng) {
        lat = latlng[0];
        lng = latlng[1];
    }

    public Point(long time, double lat, double lng) {
        this.time = time;
        this.lat = lat;
        this.lng = lng;
    }

    public int getSmoothed() {
        return smoothed;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setSmoothed(int smoothed) {
        this.smoothed = smoothed;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public long getTime() {
        return time;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public static LatLng[] toPolylineCoordinates(List<Point> points) {
        LatLng[] latLngs = new LatLng[points.size()];
        for (int i = 0; i < points.size(); i++)
            latLngs[i] = new LatLng(points.get(i).getLat(), points.get(i).getLng());
        return latLngs;
    }

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
