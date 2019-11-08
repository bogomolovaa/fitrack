package bogomolov.aa.fitrack.presenter;

import android.app.Activity;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.model.DbProvider;
import bogomolov.aa.fitrack.model.Point;
import bogomolov.aa.fitrack.model.RamerDouglasPeucker;
import bogomolov.aa.fitrack.model.Track;
import bogomolov.aa.fitrack.model.TrackerService;
import bogomolov.aa.fitrack.view.MainView;

public class MainPresenter {
    private MainView mainView;
    private DbProvider dbProvider;

    private List<Point> tailSmoothedPoints;
    private int windowStartId;

    private static final int WINDOW_MAX_SIZE = 50;

    public MainPresenter(MainView mainView) {
        this.mainView = mainView;
        dbProvider = new DbProvider(false);

    }

    public void onDestroy() {
        dbProvider.close();
    }


    public void startTrack() {
        if (TrackerService.working) {
            Point lastPoint = dbProvider.getLastPoint();
            if (lastPoint != null) {
                Track track = new Track();
                track.setStartPoint(lastPoint);
                track.setStartTime(lastPoint.getTime());
                dbProvider.addTrack(track);
                mainView.showStartStopButtons(false);
            }
        } else {
            TrackerService.startTrackerService(TrackerService.START_SERVICE_ACTION, (Activity) mainView);
        }
    }

    public void stopTrack() {
        Track openedTrack = dbProvider.getLastTrack();
        if (openedTrack != null) {
            TrackerService.finishTrack(dbProvider, dbProvider.getTrackPoints(openedTrack, Point.RAW), openedTrack, System.currentTimeMillis());
        }
        mainView.showStartStopButtons(true);
    }

    public void onStartStopButtonsCreated() {
        Track lastTrack = dbProvider.getLastTrack();
        if (lastTrack != null) {
            mainView.showStartStopButtons(!lastTrack.isOpened());
        } else {
            mainView.showStartStopButtons(true);
        }
    }

    public void onViewUpdate() {
        Track track = dbProvider.getLastTrack();
        Point point = dbProvider.getLastPoint();
        List<Point> rawPoints = null;
        List<Point> smoothedPoints = null;

        if (track != null && track.isOpened()) {
            rawPoints = dbProvider.getTrackPoints(track, Point.RAW);
            smoothedPoints = getSmoothedPoints(track, rawPoints);
        }
        mainView.updateView(track, point, rawPoints, smoothedPoints);
    }

    private List<Point> getSmoothedPoints(Track track, List<Point> points) {
        if (points.size() < 3) {
            tailSmoothedPoints = null;
            return points;
        }
        List<Point> smoothedPoints = new ArrayList<>();
        if (tailSmoothedPoints == null) {
            tailSmoothedPoints = new ArrayList<>();
            int windowSize = WINDOW_MAX_SIZE / 2;
            windowStartId = Math.max(points.size() - windowSize, 0);
            List<Point> windowPointsRaw = getWindowPoints(points, windowSize);
            List<Point> preWindowPointsRaw = getPreWindowPoints(points, windowSize);
            tailSmoothedPoints = RamerDouglasPeucker.douglasPeucker(preWindowPointsRaw, Track.EPSILON);
            List<Point> windowSmoothedPoints = RamerDouglasPeucker.douglasPeucker(windowPointsRaw, Track.EPSILON);
            smoothedPoints.addAll(tailSmoothedPoints);
            smoothedPoints.addAll(windowSmoothedPoints);
        } else {
            int windowSize = points.size() - windowStartId;
            if (windowSize >= WINDOW_MAX_SIZE) {
                List<Point> windowPointsRaw = getWindowPoints(points, windowSize);
                List<Point> secondHalfWindowPointsRaw = getWindowPoints(points, windowSize / 2);
                List<Point> firstHalfWindowPointsRaw = getPreWindowPoints(windowPointsRaw, windowSize / 2);
                List<Point> secondHalfWindowSmoothedPoints = RamerDouglasPeucker.douglasPeucker(secondHalfWindowPointsRaw, Track.EPSILON);
                List<Point> firstHalfWindowSmoothedPoints = RamerDouglasPeucker.douglasPeucker(firstHalfWindowPointsRaw, Track.EPSILON);
                tailSmoothedPoints.addAll(firstHalfWindowSmoothedPoints);
                smoothedPoints.addAll(tailSmoothedPoints);
                smoothedPoints.addAll(secondHalfWindowSmoothedPoints);
                windowStartId = points.size() - windowSize / 2;
            } else {
                List<Point> windowPointsRaw = getWindowPoints(points, windowSize);
                List<Point> windowSmoothedPoints = RamerDouglasPeucker.douglasPeucker(windowPointsRaw, Track.EPSILON);
                smoothedPoints.addAll(tailSmoothedPoints);
                smoothedPoints.addAll(windowSmoothedPoints);
            }
        }
        track.setCurrentSpeed(getCurrentSpeed(smoothedPoints));
        track.setCurrentDistance(Point.getTrackDistance(smoothedPoints));
        return smoothedPoints;
    }

    private List<Point> getWindowPoints(List<Point> points, int windowSize) {
        return points.size() > windowSize ? points.subList(points.size() - windowSize, points.size()) : points;
    }

    private List<Point> getPreWindowPoints(List<Point> points, int windowSize) {
        return points.size() > windowSize ? points.subList(0, points.size() - windowSize) : new ArrayList<Point>();
    }

    private double getCurrentSpeed(List<Point> points) {
        double distance = points.size() > 1 ? Point.distance(points.get(points.size() - 1), points.get(points.size() - 2)) : 0;
        double seconds = points.size() > 1 ? (points.get(points.size() - 1).getTime() - points.get(points.size() - 2).getTime()) / 1000.0 : 0;
        return seconds > 0 ? distance / seconds : 0;
    }

}
