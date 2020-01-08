package bogomolov.aa.fitrack.repository;

import androidx.room.Transaction;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import bogomolov.aa.fitrack.core.model.Point;
import bogomolov.aa.fitrack.core.model.Tag;
import bogomolov.aa.fitrack.core.model.Track;
import bogomolov.aa.fitrack.repository.entities.PointEntity;
import bogomolov.aa.fitrack.repository.entities.TrackEntity;

import static bogomolov.aa.fitrack.repository.ModelEntityMapper.*;

@Singleton
public class RepositoryImpl implements Repository {
    private AppDatabase db;

    @Inject
    public RepositoryImpl(AppDatabase db) {
        this.db = db;
    }

    @Override
    public void save(Track track) {
        db.trackDao().update(modelToEntity(track));
    }

    public void updateTracks(String tag, List<Long> ids) {
        db.trackDao().updateTracks(tag, ids);
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

    @Transaction
    @Override
    public void deleteTag(Tag tag) {
        db.trackDao().updateTags(tag.getName(), null);
        db.tagDao().delete(modelToEntity(tag));
    }

    @Override
    public void deleteTracks(Long... ids) {
        db.trackDao().deleteByIds(ids);
    }

    @Override
    public void deleteInnerRawPoints(Track track) {
        db.pointDao().deleteByIds(track.getStartPointId(), track.getEndPointId(), Point.RAW);
    }

    @Override
    public void deletePointsAfterLastTrack(Track lastTrack) {
        long lastId = lastTrack != null ? lastTrack.getEndPointId() : 0;
        db.pointDao().deleteByIdsGreater(lastId, Point.RAW);
    }

    @Override
    public void close() {
        db.close();
    }

    @Override
    public List<Point> getPointsAfterLastTrack(Track lastTrack) {
        long lastId = lastTrack != null ? lastTrack.getEndPointId() : 0;
        List<PointEntity> points = db.pointDao().getPointsByIdsGreater(lastId, Point.RAW);
        return entityToModel(points, Point.class);
    }

    @Override
    public Track getLastTrack() {
        return entityToModel(db.trackDao().getLastTrack());
    }

    @Override
    public Point getLastRawPoint() {
        return entityToModel(db.pointDao().getLastPoint(Point.RAW));
    }

    @Override
    public List<Point> getTrackPoints(Track track, int smoothed) {
        List<PointEntity> points = null;
        if (track.isOpened()) {
            points = db.pointDao().getPoints(track.getStartPointId(smoothed), smoothed);
        } else {
            points = db.pointDao().getPoints(track.getStartPointId(smoothed), track.getEndPointId(smoothed), smoothed);
        }
        return entityToModel(points, Point.class);
    }

    @Override
    public List<Track> getFinishedTracks(Date[] datesRange, String tag) {
        List<TrackEntity> tracks = tag != null ? db.trackDao().getFinishedTracks(datesRange[0].getTime(), datesRange[1].getTime(), tag) : db.trackDao().getFinishedTracks(datesRange[0].getTime(), datesRange[1].getTime());
        return entityToModel(tracks, Track.class);
    }

}
