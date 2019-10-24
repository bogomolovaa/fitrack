package bogomolov.aa.fitrack.model;


import io.realm.RealmObject;
import io.realm.annotations.Ignore;

public class Track extends RealmObject {
    public static final int STARTED_MODE = 1;
    public static final int ENDED_MODE = 2;

    private long id;
    private Point startPoint;
    private Point endPoint;
    private Point startSmoothedPoint;
    private Point endSmoothedPoint;
    private long startTime;
    private long endTime;

    private double distance;
    private double currentSpeed;
    private double bearing;

    @Ignore
    private int mode;


    public String getTimeString(long time) {
        long deltaTime = time - startTime;
        int hours = (int) (deltaTime / (3600 * 1000));
        deltaTime = deltaTime % (3600 * 1000);
        int minutes = (int) (deltaTime / (60 * 1000));
        deltaTime = deltaTime % (60 * 1000);
        int seconds = (int) (deltaTime / 1000);
        return hours + ":" + minutes + ":" + seconds;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
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

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getCurrentSpeed() {
        return currentSpeed;
    }

    public double getSpeed() {
        long time = isOpened() ? System.currentTimeMillis() : endTime;
        return distance / ((time-startTime) / 1000.0);
    }

    public void setCurrentSpeed(double currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public double getBearing() {
        return bearing;
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Point getStartPoint() {
        return startPoint;
    }

    public Point getStartPoint(int smoothed) {
        return smoothed == Point.RAW ? startPoint : startSmoothedPoint;
    }

    public void setStartPoint(Point startPoint) {
        this.startPoint = startPoint;
    }

    public Point getEndPoint() {
        return endPoint;
    }

    public Point getEndPoint(int smoothed) {
        return smoothed == Point.RAW ? endPoint : endSmoothedPoint;
    }

    public void setEndPoint(Point endPoint) {
        this.endPoint = endPoint;
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

    public boolean isOpened() {
        return endTime == 0;
    }

    public void addDistance(double add) {
        distance += add;
    }
}
