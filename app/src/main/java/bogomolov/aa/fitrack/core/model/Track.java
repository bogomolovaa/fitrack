package bogomolov.aa.fitrack.core.model;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import bogomolov.aa.fitrack.core.RamerDouglasPeucker;


public class Track {
    public static final int EPSILON = 25;
    private long id;
    private long startPointId;
    private long endPointId;
    private long startSmoothedPointId;
    private long endSmoothedPointId;
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
        return String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
    }

    public double getCurrentDistance() {
        return currentDistance;
    }

    public void setCurrentDistance(double currentDistance) {
        this.currentDistance = currentDistance;
    }

    public String getName() {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(startTime));
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
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
        return distance / ((time - startTime) / 1000.0);
    }

    public double getSpeedForCurrentDistance() {
        long time = isOpened() ? System.currentTimeMillis() : endTime;
        return currentDistance / ((time - startTime) / 1000.0);
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

    public long getStartPointId() {
        return startPointId;
    }

    public long getStartPointId(int smoothed) {
        return smoothed == Point.RAW ? startPointId : startSmoothedPointId;
    }

    public void setStartPointId(long startPointId) {
        this.startPointId = startPointId;
    }

    public long getEndPointId() {
        return endPointId;
    }

    public long getEndPointId(int smoothed) {
        return smoothed == Point.RAW ? endPointId : endSmoothedPointId;
    }

    public void setEndPointId(long endPointId) {
        this.endPointId = endPointId;
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

    public static Track sumTracks(List<Track> tracks) {
        double distance = 0;
        long time = 0;
        Track sumTrack = new Track();
        for (Track track : tracks) {
            distance += track.distance;
            time += track.getEndTime() - track.getStartTime();
        }
        sumTrack.setDistance(distance);
        sumTrack.setEndTime(System.currentTimeMillis());
        sumTrack.setStartTime(System.currentTimeMillis() - time);
        return sumTrack;
    }

    public static List<Point> smooth(List<Point> rawPoints) {
        return RamerDouglasPeucker.douglasPeucker(rawPoints, Track.EPSILON);
    }

    public static double getCurrentSpeed(List<Point> points) {
        double distance = points.size() > 1 ? Point.distance(points.get(points.size() - 1), points.get(points.size() - 2)) : 0;
        double seconds = points.size() > 1 ? (points.get(points.size() - 1).getTime() - points.get(points.size() - 2).getTime()) / 1000.0 : 0;
        return seconds > 0 ? distance / seconds : 0;
    }
}
