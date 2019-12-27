package bogomolov.aa.fitrack.repository;

import android.app.Application;
import android.content.Context;

import androidx.room.Query;
import androidx.room.Room;

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
import io.realm.RealmQuery;
import io.realm.Sort;

import static bogomolov.aa.fitrack.repository.ModelEntityMapper.*;

public class RepositoryImpl implements Repository {
    private AppDatabase db;

    @Inject
    public RepositoryImpl(Application application) {
        db = Room.databaseBuilder(application, AppDatabase.class, "tracker_db").build();
    }

    @Override
    public void close() {
        db.close();
    }

    @Override
    public void save(Track track) {
        db.trackDao().update(modelToEntity(track));
    }

    @Override
    public void addTag(Tag tag) {
        tag.setId(db.tagDao().insert(modelToEntity(tag)));
    }

    @Override
    public void addPoint(Point point) {
        point.setId(db.pointDao().insert(modelToEntity(point)));
    }

    @Override
    public void addTrack(Track track) {
        track.setId(db.trackDao().insert(modelToEntity(track)));
    }

    @Override
    public List<Track> getTracks(Long... ids) {
        return entityToModel(db.trackDao().getTracks(ids), Track.class);
    }

    @Override
    public List<Tag> getTags() {
        return entityToModel(db.tagDao().getTags(), Tag.class);
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
    public void deleteTracks(Long... ids) {
        realm.beginTransaction();
        realm.where(TrackEntity.class).in("id", ids).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

    @Override
    public void deleteInnerRawPoints(Track track) {
        realm.beginTransaction();
        realm.where(PointEntity.class).equalTo("smoothed", Point.RAW).greaterThan("id", track.getStartPointId().getId()).
                lessThan("id", track.getEndPointId().getId()).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

    @Override
    public void deletePointsAfterLastTrack(Track lastTrack) {
        realm.beginTransaction();
        long lastId = lastTrack != null ? lastTrack.getEndPointId().getId() : 0;
        realm.where(PointEntity.class).equalTo("smoothed", Point.RAW).greaterThan("id", lastId).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

    @Override
    public List<Point> getPointsAfterLastTrack(Track lastTrack) {
        long lastId = lastTrack != null ? lastTrack.getEndPointId().getId() : 0;
        List<PointEntity> points = realm.where(PointEntity.class).equalTo("smoothed", Point.RAW).greaterThan("id", lastId).findAll();
        return entityToModel(points, Point.class);
    }

    @Override
    public Track getLastTrack() {
        List<TrackEntity> tracks = realm.where(TrackEntity.class).findAll().sort("id", Sort.ASCENDING);
        TrackEntity track = tracks.size() > 0 ? tracks.get(tracks.size() - 1) : null;
        return entityToModel(track);
    }

    @Override
    public Point getLastRawPoint() {
        List<PointEntity> points = realm.where(PointEntity.class).equalTo("smoothed", Point.RAW).sort("id", Sort.ASCENDING).findAll();
        PointEntity point = points.size() > 0 ? points.get(points.size() - 1) : null;
        return entityToModel(point);
    }

    @Override
    public List<Point> getTrackPoints(Track track, int smoothed) {
        List<PointEntity> points = null;
        if (track.isOpened()) {
            points = realm.where(PointEntity.class).equalTo("smoothed", smoothed).greaterThanOrEqualTo("id", track.getStartPointId(smoothed).getId()).findAll().sort("id", Sort.ASCENDING);
        } else {
            points = realm.where(PointEntity.class).equalTo("smoothed", smoothed).between("id", track.getStartPointId(smoothed).getId(), track.getEndPointId(smoothed).getId()).findAll().sort("id", Sort.ASCENDING);
        }
        return entityToModel(points, Point.class);
    }

    @Override
    public List<Track> getFinishedTracks(Date[] datesRange, String tag) {
        RealmQuery<TrackEntity> query = realm.where(TrackEntity.class).greaterThanOrEqualTo("startTime", datesRange[0].getTime()).lessThan("startTime", datesRange[1].getTime()).greaterThan("endTime", 0);
        if (tag != null) query = query.equalTo("tag", tag);
        return entityToModel(query.findAll(), Track.class);
    }

}
