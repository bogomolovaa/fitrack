package bogomolov.aa.fitrack.repository.entities;

import bogomolov.aa.fitrack.core.model.Point;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class TrackEntity extends RealmObject {
    @PrimaryKey
    private long id;
    private Point startPoint;
    private Point endPoint;
    private Point startSmoothedPoint;
    private Point endSmoothedPoint;
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

    public Point getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(Point startPoint) {
        this.startPoint = startPoint;
    }

    public Point getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(Point endPoint) {
        this.endPoint = endPoint;
    }

    public Point getStartSmoothedPoint() {
        return startSmoothedPoint;
    }

    public void setStartSmoothedPoint(Point startSmoothedPoint) {
        this.startSmoothedPoint = startSmoothedPoint;
    }

    public Point getEndSmoothedPoint() {
        return endSmoothedPoint;
    }

    public void setEndSmoothedPoint(Point endSmoothedPoint) {
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
