package bogomolov.aa.fitrack.model;

import android.content.Context;
import android.util.Log;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class DbProvider {
    private Realm realm;

    private static final String DB_PROVIDER = "DbProvider";

    public DbProvider() {
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        realm = Realm.getInstance(config);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
            }
        });
    }

    public void addPoint(Point point) {
        Number maxId = realm.where(Point.class).max("id");
        long id = maxId != null ? maxId.longValue() : 0 + 1;
        point.setId(id);
        realm.beginTransaction();
        realm.copyToRealm(point);
        realm.commitTransaction();
        Log.i(DB_PROVIDER, "added point lat : " + point.getLat() + " lng : " + point.getLng());
    }


    public void addTrack(Track track) {
        Number maxId = realm.where(Track.class).max("id");
        long id = maxId != null ? maxId.longValue() : 0 + 1;
        track.setId(id);
        realm.beginTransaction();
        realm.copyToRealm(track);
        realm.commitTransaction();
        Log.i(DB_PROVIDER, "added track " + id);
    }

    public void saveTrack(Track track) {
        realm.beginTransaction();
        realm.copyToRealm(track);
        realm.commitTransaction();
    }


    public List<Point> getTrackPoints(Track track, int smoothed) {
        if (track.isOpened()) {
            return realm.where(Point.class).equalTo("smoothed", smoothed).greaterThanOrEqualTo("id", track.getStartPoint(smoothed).getId()).findAll();
        } else {
            return realm.where(Point.class).equalTo("smoothed", smoothed).between("id", track.getStartPoint(smoothed).getId(), track.getEndPoint(smoothed).getId()).findAll();
        }
    }

    public Track getLastTrack() {
        return realm.where(Track.class).findAll().last();
    }

    public Track getOpenedTrack() {
        return realm.where(Track.class).equalTo("endTime", 0).findFirst();
    }

    public List<Point> getLastPoints() {
        Track lastTrack = getLastTrack();
        if (lastTrack == null) {
            return realm.where(Point.class).equalTo("smoothed", Point.RAW).findAll();
        } else {
            if (lastTrack.getEndPoint() == null)
                throw new IllegalStateException("getLastPoints with opened track");
            return realm.where(Point.class).equalTo("smoothed", Point.RAW).greaterThan("id", lastTrack.getEndPoint().getId()).findAll();
        }
    }

    public void close() {
        realm.close();
    }
}
