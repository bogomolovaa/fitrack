package bogomolov.aa.fitrack.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Point extends RealmObject {
    @PrimaryKey
    private long id;
    private long time;
    private double lat;
    private double lng;

    public Point(long time, double lat, double lng) {
        this.time = time;
        this.lat = lat;
        this.lng = lng;
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
