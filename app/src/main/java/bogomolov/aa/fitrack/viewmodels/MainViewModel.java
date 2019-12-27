package bogomolov.aa.fitrack.viewmodels;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import bogomolov.aa.fitrack.TrackerApplication;
import bogomolov.aa.fitrack.repository.Repository;
import bogomolov.aa.fitrack.repository.RepositoryImpl;
import bogomolov.aa.fitrack.core.model.Point;
import bogomolov.aa.fitrack.core.RamerDouglasPeucker;
import bogomolov.aa.fitrack.core.model.Track;
import bogomolov.aa.fitrack.android.TrackerService;
import bogomolov.aa.fitrack.view.MainView;

public class MainViewModel extends ViewModel {
    public MutableLiveData<String> distance = new MutableLiveData<>();
    public MutableLiveData<String> time = new MutableLiveData<>();
    public MutableLiveData<String> avgSpeed = new MutableLiveData<>();
    public MutableLiveData<String> speed = new MutableLiveData<>();

    private Repository repository;
    private Handler handler;
    private Handler backgroundHandler;
    private HandlerThread handlerThread;

    private List<Point> tailSmoothedPoints;
    private int windowStartId;

    private Runnable updateRunnable;

    private static final int WINDOW_MAX_SIZE = 50;

    @Inject
    public MainViewModel(Repository repository) {
        super();
        this.repository = repository;

        handler = new Handler();
        handlerThread = new HandlerThread("MainViewModel background");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        handlerThread.quitSafely();
        repository.close();
    }

    public void startTrack(MainView mainView) {

        if (TrackerService.working) {
            Point lastPoint = repository.getLastRawPoint();
            if (lastPoint != null) {
                TrackerService.startTrack(repository,lastPoint);
                mainView.showStartStopButtons(false);
            }
        } else {
            TrackerService.startTrackerService(TrackerService.START_SERVICE_ACTION, mainView.getViewContext());
        }
    }

    public void stopTrack(MainView mainView) {
        Track lastTrack = repository.getLastTrack();
        if (lastTrack != null&&lastTrack.isOpened()) {
            TrackerService.finishTrack(repository, repository.getTrackPoints(lastTrack, Point.RAW), lastTrack, System.currentTimeMillis());
        }
        mainView.showStartStopButtons(true);
    }

    public void onStartStopButtonsCreated(MainView mainView) {
        Track lastTrack = repository.getLastTrack();
        if (lastTrack != null) {
            mainView.showStartStopButtons(!lastTrack.isOpened());
        } else {
            mainView.showStartStopButtons(true);
        }
    }

    public void startUpdating(MainView mainView) {
        updateRunnable = new Runnable() {

            @Override
            public void run() {
                Repository repository = new RepositoryImpl();
                Track track = repository.getLastTrack();
                Point point = repository.getLastRawPoint();
                List<Point> rawPoints = new ArrayList<>();
                List<Point> smoothedPoints = new ArrayList<>();

                if (track != null && track.isOpened()) {
                    rawPoints.addAll(repository.getTrackPoints(track, Point.RAW));
                    smoothedPoints.addAll(getSmoothedPoints(track, rawPoints));
                }
                Point updatePoint = point;
                Track updateTrack = track;
                handler.post(() -> {
                    if (updateTrack != null && updateTrack.isOpened()) {
                        distance.setValue((int) updateTrack.getCurrentDistance() + " m");
                        time.setValue(updateTrack.getTimeString());
                        speed.setValue(String.format("%.1f", 3.6 * updateTrack.getCurrentSpeed()) + " km/h");
                        avgSpeed.setValue(String.format("%.1f", 3.6 * updateTrack.getSpeedForCurrentDistance()) + " km/h");
                    } else {
                        distance.setValue("");
                        time.setValue("");
                        speed.setValue("");
                        avgSpeed.setValue("");
                    }
                    mainView.updateView(updateTrack, updatePoint, rawPoints, smoothedPoints);
                });

                backgroundHandler.postDelayed(this, 1000);
                repository.close();
            }
        };
        backgroundHandler.post(updateRunnable);
    }

    public void stopUpdating() {
        backgroundHandler.removeCallbacks(updateRunnable);
        updateRunnable = null;
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
