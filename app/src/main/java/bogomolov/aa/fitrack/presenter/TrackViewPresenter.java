package bogomolov.aa.fitrack.presenter;

import android.util.Log;

import java.util.List;

import bogomolov.aa.fitrack.model.DbProvider;
import bogomolov.aa.fitrack.model.Point;
import bogomolov.aa.fitrack.model.Tag;
import bogomolov.aa.fitrack.model.Track;
import bogomolov.aa.fitrack.view.TagResultListener;
import bogomolov.aa.fitrack.view.TrackViewView;

public class TrackViewPresenter implements TagResultListener {
    private DbProvider dbProvider;
    private TrackViewView trackViewView;
    private Track track;

    public TrackViewPresenter(TrackViewView trackViewView) {
        this.trackViewView = trackViewView;
        dbProvider = new DbProvider(false);
    }

    public Track setTrack(long trackId){
        track = dbProvider.getTrack(trackId);
        return track;
    }


    public void onDestroy(){
        dbProvider.close();
    }

    public List<Point> getTrackPoints(){
        return dbProvider.getTrackPoints(track, Point.SMOOTHED);
    }



    @Override
    public void onTagSelectionResult(Tag tag) {
        if (tag != null) {
            dbProvider.getRealm().beginTransaction();
            track.setTag(tag.getName());
            trackViewView.updateTag(tag);
            dbProvider.getRealm().commitTransaction();
        }
    }
}
