package bogomolov.aa.fitrack.core.model;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class Track {
    public static final int EPSILON = 25;
    private long id;
    private Point startPoint;
    private Point endPoint;
    private Point startSmoothedPoint;
    private Point endSmoothedPoint;
    private long startTime;
    private long endTime;
    private String tag;

    private double distance;

    private double currentSpeed;
    private double currentDistance;

    public String getTimeString() {
        long time = isOpened() ? System.currentTimeMillis() : endTime;
        long deltaTime = time - startTime;
        int hours = (int) (deltaTime / (3600 * 1000));
        deltaTime = deltaTime % (3600 * 1000);
        int minutes = (int) (deltaTime / (60 * 1000));
        deltaTime = deltaTime % (60 * 1000);
        int seconds = (int) (deltaTime / 1000);
        return hours + ":" + minutes + ":" + seconds;
    }

    public double getCurrentDistance() {
        return currentDistance;
    }

    public void setCurrentDistance(double currentDistance) {
        this.currentDistance = currentDistance;
    }

    public String getName(){
        return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(startTime));
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
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

    public double getSpeedForCurrentDistance() {
        long time = isOpened() ? System.currentTimeMillis() : endTime;
        return currentDistance / ((time-startTime) / 1000.0);
    }

    public void setCurrentSpeed(double currentSpeed) {
        this.currentSpeed = currentSpeed;
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

    public static Track sumTracks(List<Track> tracks){
        double distance = 0;
        long time = 0;
        Track sumTrack = new Track();
        for(Track track : tracks){
            distance+=track.distance;
            time+=track.getEndTime()-track.getStartTime();
        }
        sumTrack.setDistance(distance);
        sumTrack.setEndTime(System.currentTimeMillis());
        sumTrack.setStartTime(System.currentTimeMillis()-time);
        return sumTrack;
    }
}
