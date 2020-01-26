package bogomolov.aa.fitrack.viewmodels;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import bogomolov.aa.fitrack.core.TrackActions;
import bogomolov.aa.fitrack.repository.Repository;
import bogomolov.aa.fitrack.core.model.Point;
import bogomolov.aa.fitrack.core.model.Track;
import bogomolov.aa.fitrack.android.TrackerService;

import static bogomolov.aa.fitrack.android.Rx.worker;

public class MainViewModel extends ViewModel {
    public MutableLiveData<String> distance = new MutableLiveData<>();
    public MutableLiveData<String> time = new MutableLiveData<>();
    public MutableLiveData<String> avgSpeed = new MutableLiveData<>();
    public MutableLiveData<String> speed = new MutableLiveData<>();
    public MutableLiveData<Boolean> startStop = new MutableLiveData<>();
    public MutableLiveData<Point> lastPointLiveData = new MutableLiveData<>();

    private Repository repository;
    private Handler backgroundHandler;
    private HandlerThread handlerThread;

    private List<Point> tailSmoothedPoints;
    private int windowStartId;

    private Runnable updateRunnable;

    public Track track;
    public List<Point> rawPoints;
    public List<Point> smoothedPoints;


    private static final int WINDOW_MAX_SIZE = 50;

    @Inject
    public MainViewModel(Repository repository) {
        super();
        this.repository = repository;

        handlerThread = new HandlerThread("MainViewModel background");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        handlerThread.quitSafely();
    }

    public void startTrack(Context context) {
        if (TrackerService.working) {
            worker(() -> {
                Point lastPoint = repository.getLastRawPoint();
                if (lastPoint != null) {
                    TrackActions.startTrack(repository, lastPoint);
                    startStop.postValue(false);
                }
            });
        } else {
            TrackerService.startTrackerService(TrackerService.START_SERVICE_ACTION, context);
        }
    }

    public void stopTrack(Context context) {
        worker(() -> {
            Track lastTrack = repository.getLastTrack();
            if (lastTrack != null && lastTrack.isOpened()) {
                TrackActions.finishTrack(repository,context, repository.getTrackPoints(lastTrack, Point.RAW), lastTrack, System.currentTimeMillis());
                startStop.postValue(true);
            }
        });
    }

    public void startUpdating() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                track = repository.getLastTrack();
                Point point = repository.getLastRawPoint();
                rawPoints = new ArrayList<>();
                smoothedPoints = new ArrayList<>();

                if (track != null && track.isOpened()) {
                    rawPoints.addAll(repository.getTrackPoints(track, Point.RAW));
                    smoothedPoints.addAll(getSmoothedPoints(track, rawPoints));
                }

                if (track != null && track.isOpened()) {
                    distance.postValue((int) track.getCurrentDistance() + " m");
                    time.postValue(track.getTimeString());
                    speed.postValue(String.format("%.1f", 3.6 * track.getCurrentSpeed()) + " km/h");
                    avgSpeed.postValue(String.format("%.1f", 3.6 * track.getSpeedForCurrentDistance()) + " km/h");
                } else {
                    distance.postValue("");
                    time.postValue("");
                    speed.postValue("");
                    avgSpeed.postValue("");
                }
                lastPointLiveData.postValue(point);

                backgroundHandler.postDelayed(this, 1000);
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
            tailSmoothedPoints = Track.smooth(preWindowPointsRaw);
            List<Point> windowSmoothedPoints = Track.smooth(windowPointsRaw);
            smoothedPoints.addAll(tailSmoothedPoints);
            smoothedPoints.addAll(windowSmoothedPoints);
        } else {
            int windowSize = points.size() - windowStartId;
            if (windowSize >= WINDOW_MAX_SIZE) {
                List<Point> windowPointsRaw = getWindowPoints(points, windowSize);
                List<Point> secondHalfWindowPointsRaw = getWindowPoints(points, windowSize / 2);
                List<Point> firstHalfWindowPointsRaw = getPreWindowPoints(windowPointsRaw, windowSize / 2);
                List<Point> secondHalfWindowSmoothedPoints = Track.smooth(secondHalfWindowPointsRaw);
                List<Point> firstHalfWindowSmoothedPoints = Track.smooth(firstHalfWindowPointsRaw);
                tailSmoothedPoints.addAll(firstHalfWindowSmoothedPoints);
                smoothedPoints.addAll(tailSmoothedPoints);
                smoothedPoints.addAll(secondHalfWindowSmoothedPoints);
                windowStartId = points.size() - windowSize / 2;
            } else {
                List<Point> windowPointsRaw = getWindowPoints(points, windowSize);
                List<Point> windowSmoothedPoints = Track.smooth(windowPointsRaw);
                smoothedPoints.addAll(tailSmoothedPoints);
                smoothedPoints.addAll(windowSmoothedPoints);
            }
        }
        track.setCurrentSpeed(Track.getCurrentSpeed(smoothedPoints));
        track.setCurrentDistance(Point.getTrackDistance(smoothedPoints));
        return smoothedPoints;
    }

    private List<Point> getWindowPoints(List<Point> points, int windowSize) {
        return points.size() > windowSize ? points.subList(points.size() - windowSize, points.size()) : points;
    }

    private List<Point> getPreWindowPoints(List<Point> points, int windowSize) {
        return points.size() > windowSize ? points.subList(0, points.size() - windowSize) : new ArrayList<Point>();
    }



}
