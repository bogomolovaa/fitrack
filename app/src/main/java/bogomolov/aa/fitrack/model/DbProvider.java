package bogomolov.aa.fitrack.model;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class DbProvider {
    private Realm realm;

    private static final String DB_PROVIDER = "DbProvider";

    public DbProvider(boolean delete) {
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        realm = Realm.getInstance(config);
        if (delete)
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.deleteAll();
                }
            });
    }

    public Realm getRealm() {
        return realm;
    }

    public Point addPoint(Point point) {
        realm.beginTransaction();
        Number maxId = realm.where(Point.class).max("id");
        long id = (maxId != null ? maxId.longValue() : 0) + 1;
        point.setId(id);
        point = realm.copyToRealm(point);
        realm.commitTransaction();
        return point;
    }


    public Track addTrack(Track track) {
        Number maxId = realm.where(Track.class).max("id");
        long id = maxId != null ? maxId.longValue() : 0 + 1;
        track.setId(id);
        realm.beginTransaction();
        track = realm.copyToRealm(track);
        realm.commitTransaction();
        return track;
    }


    public List<Point> getTrackPoints(Track track, int smoothed) {
        if (track.isOpened()) {
            if (track.getStartPoint(smoothed) == null) return new ArrayList<>();
            return realm.where(Point.class).equalTo("smoothed", smoothed).greaterThanOrEqualTo("id", track.getStartPoint(smoothed).getId()).findAll();
        } else {
            if (track.getStartPoint(smoothed) == null || track.getEndPoint(smoothed) == null) return new ArrayList<>();
            return realm.where(Point.class).equalTo("smoothed", smoothed).between("id", track.getStartPoint(smoothed).getId(), track.getEndPoint(smoothed).getId()).findAll();
        }
    }

    public Track getLastTrack() {
        List<Track> tracks = realm.where(Track.class).findAll();
        return tracks.size() > 0 ? tracks.get(tracks.size() - 1) : null;
    }

    public Track getOpenedTrack() {
        List<Track> tracks = realm.where(Track.class).equalTo("endTime", 0).findAll();
        return tracks.size() > 0 ? tracks.get(0) : null;
    }

    public Point getLastPoint() {
        List<Point> points = getLastPoints();
        if (points.size() > 0) return points.get(points.size() - 1);
        return null;
    }

    public List<Point> getLastPoints() {
        Track lastTrack = getLastTrack();
        if (lastTrack == null || lastTrack.getEndPoint() == null) {
            return realm.where(Point.class).equalTo("smoothed", Point.RAW).findAll();
        } else {
            return realm.where(Point.class).equalTo("smoothed", Point.RAW).greaterThan("id", lastTrack.getEndPoint().getId()).findAll();
        }
    }

    public void close() {
        realm.close();
    }
}
