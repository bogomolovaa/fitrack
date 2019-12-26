package bogomolov.aa.fitrack.viewmodels;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import bogomolov.aa.fitrack.repository.Repository;
import bogomolov.aa.fitrack.repository.RepositoryImpl;
import bogomolov.aa.fitrack.core.model.Tag;
import bogomolov.aa.fitrack.core.model.Track;
import bogomolov.aa.fitrack.view.TracksListView;

public class TracksListViewModel extends ViewModel {
    private Repository repository;

    @Inject
    public TracksListViewModel(Repository repository) {
        this.repository = repository;
    }

    @Override
    protected void onCleared(){
        repository.close();
    }

    public void onTimeFilterSelect(Date[] dates, TracksListView tracksListView){
        List<Track> tracks = repository.getFinishedTracks(dates,null);
        tracksListView.updateTracksList(tracks);
    }

    public void setTag(Tag tag, Set<Long> selectedIds){
        if (tag != null) {
            List<Long> ids = new ArrayList<>(selectedIds);
            List<Track> tracks = repository.getTracks(ids.toArray(new Long[0]));
            for (Track track : tracks){
                track.setTag(tag.getName());
                repository.save(track);
            }
        }
    }

    public void deleteTracks(Set<Long> selectedIds){
        repository.deleteTracks(selectedIds.toArray(new Long[0]));
    }



}
