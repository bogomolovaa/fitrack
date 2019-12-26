package bogomolov.aa.fitrack.viewmodels;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import bogomolov.aa.fitrack.repository.RepositoryImpl;
import bogomolov.aa.fitrack.core.model.Tag;
import bogomolov.aa.fitrack.core.model.Track;
import bogomolov.aa.fitrack.view.TracksListView;

public class TracksListViewModel extends ViewModel {
    private RepositoryImpl dbProvider;

    @Inject
    public TracksListViewModel(RepositoryImpl dbProvider) {
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
            for (Track track : tracks){
                track.setTag(tag.getName());
                dbProvider.save(track);
            }
        }
    }

    public void deleteTracks(Set<Long> selectedIds){
        dbProvider.deleteTracks(new ArrayList<>(selectedIds));
    }



}
