package bogomolov.aa.fitrack.core.model;

import java.util.ArrayList;
import java.util.List;


public class Point{
    public static final int SMOOTHED = 1;

    public static final int RAW = 0;

    private long id;
    private long time;
    private double lat;
    private double lng;
    private int smoothed;

    private Point clonePoint(){
        Point point = new Point();
        point.time = time;
        point.lat = lat;
        point.lng = lng;
        point.smoothed = smoothed;
        return point;
    }

    public String toString(){
        return id+" ["+lat+";"+lng+"] smoothed "+smoothed;
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

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
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

    public static List<Point> clonePoints(List<Point> points){
        List<Point> cloned = new ArrayList<>();
        for(Point point : points) cloned.add(point.clonePoint());
        return cloned;
    }
}
