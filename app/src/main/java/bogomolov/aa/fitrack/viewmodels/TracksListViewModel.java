package bogomolov.aa.fitrack.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import bogomolov.aa.fitrack.repository.Repository;
import bogomolov.aa.fitrack.core.model.Tag;
import bogomolov.aa.fitrack.core.model.Track;

import static bogomolov.aa.fitrack.android.Rx.worker;

public class TracksListViewModel extends ViewModel {
    private Repository repository;
    public MutableLiveData<List<Track>> tracksLiveData = new MutableLiveData<>();
    private Date[] datesRange;

    @Inject
    public TracksListViewModel(Repository repository) {
        this.repository = repository;
    }

    public void updateTracks(Date[] dates) {
        datesRange = dates;
        worker(() -> tracksLiveData.postValue(repository.getFinishedTracks(dates, null)));
    }

    public void setTag(@NonNull Tag tag, Set<Long> selectedIds) {
        worker(() -> {
            repository.updateTracks(tag.getName(), new ArrayList<>(selectedIds));
            updateTracks(datesRange);
        });
    }

    public void deleteTracks(Set<Long> selectedIds) {
        worker(() -> {
            repository.deleteTracks(selectedIds.toArray(new Long[0]));
            updateTracks(datesRange);
        });
    }

}
