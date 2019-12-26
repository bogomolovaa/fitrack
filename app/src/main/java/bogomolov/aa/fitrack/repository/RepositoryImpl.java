package bogomolov.aa.fitrack.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import bogomolov.aa.fitrack.core.model.Point;
import bogomolov.aa.fitrack.core.model.Tag;
import bogomolov.aa.fitrack.core.model.Track;
import bogomolov.aa.fitrack.repository.entities.PointEntity;
import bogomolov.aa.fitrack.repository.entities.TagEntity;
import bogomolov.aa.fitrack.repository.entities.TrackEntity;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.Sort;

import static bogomolov.aa.fitrack.repository.ModelEntityMapper.*;

public class RepositoryImpl implements Repository {
    private Realm realm;

    @Inject
    public RepositoryImpl() {
        boolean delete = false;
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

    @Override
    public void save(Track track){
        realm.copyToRealmOrUpdate(modelToEntity(track));
    }

    @Override
    public List<Track> getFinishedTracks(Date[] datesRange) {
        List<TrackEntity> tracks = realm.where(TrackEntity.class).greaterThanOrEqualTo("startTime", datesRange[0].getTime()).lessThan("startTime", datesRange[1].getTime()).findAll();
        return entityToModel(tracks, Track.class);
    }

    @Override
    public List<Track> getFinishedTracks(Date[] datesRange, String tag) {
        List<TrackEntity> tracks = realm.where(TrackEntity.class).equalTo("tag", tag).greaterThanOrEqualTo("startTime", datesRange[0].getTime()).lessThan("startTime", datesRange[1].getTime()).findAll();
        return entityToModel(tracks, Track.class);
    }

    @Override
    public List<Track> getTracks(List<Long> ids) {
        List<TrackEntity> tracks = realm.where(TrackEntity.class).in("id", ids.toArray(new Long[0])).findAll();
        return entityToModel(tracks, Track.class);
    }

    @Override
    public List<Tag> getTags() {
        List<TagEntity> tags = realm.where(TagEntity.class).findAll();
        return entityToModel(tags, Tag.class);
    }

    @Override
    public void deleteTag(Tag tag) {
        List<TrackEntity> tracks = realm.where(TrackEntity.class).equalTo("tag", tag.getName()).findAll();
        realm.beginTransaction();
        for (TrackEntity track : tracks) track.setTag(null);
        realm.commitTransaction();
        realm.where(TagEntity.class).equalTo("id", tag.getId()).findAll().deleteAllFromRealm();
    }

    @Override
    public void deleteTrack(long id) {
        realm.beginTransaction();
        realm.where(TrackEntity.class).equalTo("id", id).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

    @Override
    public void deleteTracks(List<Long> ids) {
        realm.beginTransaction();
        realm.where(TrackEntity.class).in("id", ids.toArray(new Long[0])).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

    @Override
    public void deleteRawPoints(Track track) {
        realm.beginTransaction();
        realm.where(PointEntity.class).equalTo("smoothed", Point.RAW).greaterThan("id", track.getStartPoint().getId()).
                lessThan("id", track.getEndPoint().getId()).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

    @Override
    public Track getTrack(long id) {
        TrackEntity track = realm.where(TrackEntity.class).equalTo("id", id).findFirst();
        return entityToModel(track);
    }



    @Override
    public void addTag(Tag tag) {
        realm.beginTransaction();
        Number maxId = realm.where(TagEntity.class).max("id");
        long id = (maxId != null ? maxId.longValue() : 0) + 1;
        tag.setId(id);
        realm.copyToRealm(modelToEntity(tag));
        realm.commitTransaction();
    }

    @Override
    public void addPoint(Point point) {
        realm.beginTransaction();
        Number maxId = realm.where(PointEntity.class).max("id");
        long id = (maxId != null ? maxId.longValue() : 0) + 1;
        point.setId(id);
        realm.copyToRealm(modelToEntity(point));
        realm.commitTransaction();
    }


    @Override
    public void addTrack(Track track) {
        realm.beginTransaction();
        if (track.getId() == 0) {
            Number maxId = realm.where(TrackEntity.class).max("id");
            long id = (maxId != null ? maxId.longValue() : 0) + 1;
            track.setId(id);
        }
        realm.copyToRealm(modelToEntity(track));
        realm.commitTransaction();
    }


    @Override
    public List<Point> getTrackPoints(Track track, int smoothed) {
        if (track.isOpened()) {
            if (track.getStartPoint(smoothed) == null) return new ArrayList<>();
            List<PointEntity> points = realm.where(PointEntity.class).equalTo("smoothed", smoothed).greaterThanOrEqualTo("id", track.getStartPoint(smoothed).getId()).findAll().sort("id", Sort.ASCENDING);
            return entityToModel(points, Point.class);
        } else {
            if (track.getStartPoint(smoothed) == null || track.getEndPoint(smoothed) == null)
                return new ArrayList<>();
            List<PointEntity> points = realm.where(PointEntity.class).equalTo("smoothed", smoothed).between("id", track.getStartPoint(smoothed).getId(), track.getEndPoint(smoothed).getId()).findAll().sort("id", Sort.ASCENDING);
            return entityToModel(points, Point.class);
        }
    }


    @Override
    public Track getLastTrack() {
        List<TrackEntity> tracks = realm.where(TrackEntity.class).findAll().sort("id", Sort.ASCENDING);
        TrackEntity track = tracks.size() > 0 ? tracks.get(tracks.size() - 1) : null;
        return entityToModel(track);
    }

    @Override
    public Track getOpenedTrack() {
        List<TrackEntity> tracks = realm.where(TrackEntity.class).equalTo("endTime", 0).findAll();
        TrackEntity track = tracks.size() > 0 ? tracks.get(0) : null;
        return entityToModel(track);
    }

    @Override
    public Point getLastPoint() {
        List<PointEntity> points = realm.where(PointEntity.class).equalTo("smoothed", Point.RAW).sort("id", Sort.ASCENDING).findAll();
        PointEntity point = points.size() > 0 ? points.get(points.size() - 1) : null;
        return entityToModel(point);
    }

    @Override
    public List<Point> getLastPoints() {
        Track lastTrack = getLastTrack();
        List<PointEntity> points = null;
        if (lastTrack == null || lastTrack.getEndPoint() == null) {
            points = realm.where(PointEntity.class).equalTo("smoothed", Point.RAW).findAll();
        } else {
            points = realm.where(PointEntity.class).equalTo("smoothed", Point.RAW).greaterThan("id", lastTrack.getEndPoint().getId()).findAll();
        }
        return entityToModel(points, Point.class);
    }

    @Override
    public void deleteLastPoints() {
        Track lastTrack = getLastTrack();
        realm.beginTransaction();
        if (lastTrack == null || lastTrack.getEndPoint() == null) {
            realm.where(PointEntity.class).equalTo("smoothed", Point.RAW).findAll().deleteAllFromRealm();
        } else {
            realm.where(PointEntity.class).equalTo("smoothed", Point.RAW).greaterThan("id", lastTrack.getEndPoint().getId()).findAll().deleteAllFromRealm();
        }
        realm.commitTransaction();
    }

    @Override
    public void close() {
        realm.close();
    }
}
