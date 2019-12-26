package bogomolov.aa.fitrack.repository;

import java.util.Date;
import java.util.List;

import bogomolov.aa.fitrack.core.model.Point;
import bogomolov.aa.fitrack.core.model.Tag;
import bogomolov.aa.fitrack.core.model.Track;

interface Repository {
    void save(Track track);

    List<Track> getFinishedTracks(Date[] datesRange);

    List<Track> getFinishedTracks(Date[] datesRange, String tag);

    List<Track> getTracks(List<Long> ids);

    List<Tag> getTags();

    void deleteTag(Tag tag);

    void deleteTrack(long id);

    void deleteTracks(List<Long> ids);

    void deleteRawPoints(Track track);

    Track getTrack(long id);

    void addTag(Tag tag);

    void addPoint(Point point);

    void addTrack(Track track);

    List<Point> getTrackPoints(Track track, int smoothed);

    Track getLastTrack();

    Track getOpenedTrack();

    Point getLastPoint();

    List<Point> getLastPoints();

    void deleteLastPoints();

    void close();
}
