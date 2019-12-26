package bogomolov.aa.fitrack.repository.entities;

import bogomolov.aa.fitrack.core.model.Point;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class TrackEntity extends RealmObject {
    @PrimaryKey
    private long id;
    private PointEntity startPoint;
    private PointEntity endPoint;
    private PointEntity startSmoothedPoint;
    private PointEntity endSmoothedPoint;
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

    public PointEntity getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(PointEntity startPoint) {
        this.startPoint = startPoint;
    }

    public PointEntity getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(PointEntity endPoint) {
        this.endPoint = endPoint;
    }

    public PointEntity getStartSmoothedPoint() {
        return startSmoothedPoint;
    }

    public void setStartSmoothedPoint(PointEntity startSmoothedPoint) {
        this.startSmoothedPoint = startSmoothedPoint;
    }

    public PointEntity getEndSmoothedPoint() {
        return endSmoothedPoint;
    }

    public void setEndSmoothedPoint(PointEntity endSmoothedPoint) {
        this.endSmoothedPoint = endSmoothedPoint;
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
