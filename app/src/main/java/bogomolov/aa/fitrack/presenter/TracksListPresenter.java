package bogomolov.aa.fitrack.presenter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import bogomolov.aa.fitrack.model.DbProvider;
import bogomolov.aa.fitrack.model.Tag;
import bogomolov.aa.fitrack.model.Track;
import bogomolov.aa.fitrack.view.TracksListView;

public class TracksListPresenter {
    private TracksListView tracksListView;
    private DbProvider dbProvider;

    public TracksListPresenter(TracksListView tracksListView) {
        this.tracksListView = tracksListView;
        dbProvider = new DbProvider(false);
    }

    public void onTimeFilterSelect(Date[] dates){
        List<Track> tracks = dbProvider.getFinishedTracks(dates);
        tracksListView.updateTracksList(tracks);
    }

    public void setTag(Tag tag, Set<Long> selectedIds){
        if (tag != null) {
            List<Long> ids = new ArrayList<>(selectedIds);
            List<Track> tracks = dbProvider.getTracks(ids);
            dbProvider.getRealm().beginTransaction();
            for (Track track : tracks) track.setTag(tag.getName());
            dbProvider.getRealm().commitTransaction();
        }
    }

    public void deleteTracks(Set<Long> selectedIds){
        dbProvider.deleteTracks(new ArrayList<>(selectedIds));
    }

    public void onDestroy(){
        dbProvider.close();
    }

}
