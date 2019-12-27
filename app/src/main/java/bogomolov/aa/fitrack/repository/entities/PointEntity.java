package bogomolov.aa.fitrack.repository.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity
public class PointEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long time;
    private double lat;
    private double lng;
    private int smoothed;

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
}
