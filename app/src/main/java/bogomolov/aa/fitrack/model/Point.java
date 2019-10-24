package bogomolov.aa.fitrack.model;

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

    public Point() {
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
}
