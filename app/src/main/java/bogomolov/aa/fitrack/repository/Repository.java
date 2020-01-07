package bogomolov.aa.fitrack.repository;

import java.util.Date;
import java.util.List;

import bogomolov.aa.fitrack.core.model.Point;
import bogomolov.aa.fitrack.core.model.Tag;
import bogomolov.aa.fitrack.core.model.Track;

public interface Repository {

    void save(Track track);

    void updateTracks(String tag, List<Long> ids);

    List<Track> getFinishedTracks(Date[] datesRange, String tag);

    List<Track> getTracks(Long... ids);

    List<Tag> getTags();

    void deleteTag(Tag tag);

    void deleteTracks(Long... ids);

    void deleteInnerRawPoints(Track track);

    void addTag(Tag tag);

    void addPoint(Point point);

    void addTrack(Track track);

    List<Point> getTrackPoints(Track track, int smoothed);

    Track getLastTrack();

    Point getLastRawPoint();

    List<Point> getPointsAfterLastTrack(Track lastTrack);

    void deletePointsAfterLastTrack(Track lastTrack);

}
