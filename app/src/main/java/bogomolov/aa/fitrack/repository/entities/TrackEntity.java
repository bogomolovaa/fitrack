package bogomolov.aa.fitrack.repository.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import bogomolov.aa.fitrack.core.model.Point;

@Entity
public class TrackEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long startPointId;
    private long endPointId;
    private long startSmoothedPointId;
    private long endSmoothedPointId;
    private long startTime;
    private long endTime;
    private String tag;
    private double distance;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getStartPointId() {
        return startPointId;
    }

    public void setStartPointId(long startPointId) {
        this.startPointId = startPointId;
    }

    public long getEndPointId() {
        return endPointId;
    }

    public void setEndPointId(long endPointId) {
        this.endPointId = endPointId;
    }

    public long getStartSmoothedPointId() {
        return startSmoothedPointId;
    }

    public void setStartSmoothedPointId(long startSmoothedPointId) {
        this.startSmoothedPointId = startSmoothedPointId;
    }

    public long getEndSmoothedPointId() {
        return endSmoothedPointId;
    }

    public void setEndSmoothedPointId(long endSmoothedPointId) {
        this.endSmoothedPointId = endSmoothedPointId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
