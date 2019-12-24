package bogomolov.aa.fitrack.viewmodels;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import bogomolov.aa.fitrack.model.DbProvider;
import bogomolov.aa.fitrack.model.Tag;
import bogomolov.aa.fitrack.model.Track;
import bogomolov.aa.fitrack.view.TracksListView;

public class TracksListViewModel extends ViewModel {
    private DbProvider dbProvider;

    @Inject
    public TracksListViewModel(DbProvider dbProvider) {
        this.dbProvider = dbProvider;
    }

    @Override
    protected void onCleared(){
        dbProvider.close();
    }

    public void onTimeFilterSelect(Date[] dates, TracksListView tracksListView){
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



}
