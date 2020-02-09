package bogomolov.aa.fitrack.core;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import bogomolov.aa.fitrack.android.MapSaver;
import bogomolov.aa.fitrack.core.model.Point;
import bogomolov.aa.fitrack.core.model.Track;
import bogomolov.aa.fitrack.repository.Repository;

public class TrackActions {
    private static final double MIN_TRACK_DISTANCE = 150;

    public static boolean onNewPoint(Point point, Repository repository, Context context, long startLocationUpdateTime, long UPDATE_INTERVAL) {
        List<Point> trackPoints = null;
        Track lastTrack = repository.getLastTrack();
        Track openedTrack = lastTrack != null && !lastTrack.isOpened() ? null : lastTrack;
        if (openedTrack == null) {
            repository.addPoint(point);
            List<Point> points = repository.getPointsAfterLastTrack(lastTrack);
            if (points.size() > 1) {
                Point firstPoint = points.get(0);
                for (int i = 0; i < points.size() - 1; i++)
                    if (System.currentTimeMillis() - points.get(i).getTime() < 3 * 60 * 1000) {
                        firstPoint = points.get(i);
                        break;
                    }
                Point lastPoint = points.get(points.size() - 1);
                double distance = Point.Companion.distance(firstPoint, lastPoint);
                if (distance > 50) {
                    startTrack(repository, lastPoint);
                } else if (System.currentTimeMillis() - startLocationUpdateTime > 5 * 60 * 1000) {
                    repository.deletePointsAfterLastTrack(lastTrack);
                    return true;
                }
            }
        } else {
            List<Point> points = new ArrayList<>(repository.getTrackPoints(openedTrack, Point.RAW));
            trackPoints = points;
            Point lastPoint = points.get(points.size() - 1);
            if (!(Point.Companion.distance(point, lastPoint) > 200 || System.currentTimeMillis() - lastPoint.getTime() <= 2 * UPDATE_INTERVAL)) {
                points.add(point);
                lastPoint = point;
                repository.addPoint(lastPoint);
            }
            for (int i = points.size() - 1; i >= 0; i--) {
                if (Point.Companion.distance(lastPoint, points.get(i)) <= 50) {
                    if (lastPoint.getTime() - points.get(i).getTime() > 3 * 60 * 1000) {
                        trackPoints = trackPoints.subList(0, i + 1);
                        trackPoints.add(lastPoint);
                        finishTrack(repository,context, trackPoints, openedTrack, points.get(i).getTime());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void startTrack(Repository repository, Point lastPoint) {
        Track track = new Track();
        track.setStartPointId(lastPoint.getId());
        track.setStartTime(lastPoint.getTime());
        repository.addTrack(track);
    }

    public static void finishTrack(Repository repository,Context context, List<Point> points, Track openedTrack, long time) {
        if (points.size() == 0) return;
        Point lastPoint = points.get(points.size() - 1);
        List<Point> smoothedPoints = Point.Companion.clonePoints(Track.Companion.smooth(points));
        for (Point point : smoothedPoints) {
            point.setSmoothed(Point.SMOOTHED);
            repository.addPoint(point);
        }
        openedTrack.setEndPointId(lastPoint.getId());
        openedTrack.setEndTime(time);
        openedTrack.setStartSmoothedPointId(smoothedPoints.get(0).getId());
        openedTrack.setEndSmoothedPointId(smoothedPoints.get(smoothedPoints.size() - 1).getId());
        openedTrack.setDistance(Point.Companion.getTrackDistance(smoothedPoints));
        repository.save(openedTrack);
        repository.deleteInnerRawPoints(openedTrack);

        if (openedTrack.getDistance() < MIN_TRACK_DISTANCE) {
            repository.deleteTracks(openedTrack.getId());
        }else{
            MapSaver.saveUI(context, openedTrack, smoothedPoints, 600, 400);
        }
    }
}
