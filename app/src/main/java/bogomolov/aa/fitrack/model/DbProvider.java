package bogomolov.aa.fitrack.model;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import bogomolov.aa.fitrack.R;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.Sort;

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

    public List<Track> getFinishedTracks() {
        return realm.where(Track.class).notEqualTo("endTime", 0).findAll();
    }

    public List<Track> getFinishedTracks(Date[] datesRange) {
        return realm.where(Track.class).greaterThanOrEqualTo("startTime", datesRange[0].getTime()).lessThan("startTime", datesRange[1].getTime()).findAll();
    }

    public List<Track> getFinishedTracks(Date[] datesRange, String tag) {
        return realm.where(Track.class).equalTo("tag", tag).greaterThanOrEqualTo("startTime", datesRange[0].getTime()).lessThan("startTime", datesRange[1].getTime()).findAll();
    }

    public List<Tag> getTags() {
        return realm.where(Tag.class).findAll();
    }

    public void deleteTag(Tag tag) {
        List<Track> tracks = realm.where(Track.class).equalTo("tag", tag.getName()).findAll();
        realm.beginTransaction();
        for (Track track : tracks) track.setTag(null);
        realm.commitTransaction();
        realm.where(Tag.class).equalTo("id", tag.getId()).findAll().deleteAllFromRealm();
    }

    public List<Track> getTracks(List<Long> ids) {
        return realm.where(Track.class).in("id", ids.toArray(new Long[0])).findAll();
    }

    public void deleteTrack(long id) {
        realm.beginTransaction();
        realm.where(Track.class).equalTo("id", id).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

    public void deleteTracks(List<Long> ids) {
        realm.beginTransaction();
        realm.where(Track.class).in("id", ids.toArray(new Long[0])).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

    public void deleteRawPoints(Track track) {
        realm.beginTransaction();
        realm.where(Point.class).equalTo("smoothed", Point.RAW).greaterThan("id", track.getStartPoint().getId()).
                lessThan("id", track.getEndPoint().getId()).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

    public Track getTrack(long id) {
        return realm.where(Track.class).equalTo("id", id).findFirst();
    }

    public Tag addTag(Tag tag) {
        realm.beginTransaction();
        Number maxId = realm.where(Tag.class).max("id");
        long id = (maxId != null ? maxId.longValue() : 0) + 1;
        tag.setId(id);
        tag = realm.copyToRealm(tag);
        realm.commitTransaction();
        return tag;
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
        if (track.getId() == 0) {
            Number maxId = realm.where(Track.class).max("id");
            long id = (maxId != null ? maxId.longValue() : 0) + 1;
            track.setId(id);
        }
        realm.beginTransaction();
        track = realm.copyToRealm(track);
        realm.commitTransaction();
        return track;
    }


    public List<Point> getTrackPoints(Track track, int smoothed) {
        if (track.isOpened()) {
            if (track.getStartPoint(smoothed) == null) return new ArrayList<>();
            return realm.where(Point.class).equalTo("smoothed", smoothed).greaterThanOrEqualTo("id", track.getStartPoint(smoothed).getId()).findAll().sort("id", Sort.ASCENDING);
        } else {
            if (track.getStartPoint(smoothed) == null || track.getEndPoint(smoothed) == null)
                return new ArrayList<>();
            return realm.where(Point.class).equalTo("smoothed", smoothed).between("id", track.getStartPoint(smoothed).getId(), track.getEndPoint(smoothed).getId()).findAll().sort("id", Sort.ASCENDING);
        }
    }


    public Track getLastTrack() {
        List<Track> tracks = realm.where(Track.class).findAll().sort("id", Sort.ASCENDING);
        return tracks.size() > 0 ? tracks.get(tracks.size() - 1) : null;
    }

    public Track getOpenedTrack() {
        List<Track> tracks = realm.where(Track.class).equalTo("endTime", 0).findAll();
        return tracks.size() > 0 ? tracks.get(0) : null;
    }

    public Point getLastPoint() {
        List<Point> points = realm.where(Point.class).equalTo("smoothed", Point.RAW).sort("id", Sort.ASCENDING).findAll();
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

    public void deleteLastPoints() {
        Track lastTrack = getLastTrack();
        realm.beginTransaction();
        if (lastTrack == null || lastTrack.getEndPoint() == null) {
            realm.where(Point.class).equalTo("smoothed", Point.RAW).findAll().deleteAllFromRealm();
        } else {
            realm.where(Point.class).equalTo("smoothed", Point.RAW).greaterThan("id", lastTrack.getEndPoint().getId()).findAll().deleteAllFromRealm();
        }
        realm.commitTransaction();
    }

    public void close() {
        realm.close();
    }
}
