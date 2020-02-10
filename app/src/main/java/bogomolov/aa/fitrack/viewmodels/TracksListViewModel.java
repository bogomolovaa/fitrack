package bogomolov.aa.fitrack.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

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
    private MutableLiveData<Date[]> datesLiveData = new MutableLiveData<>();
    public LiveData<PagedList<Track>> tracksLiveData = Transformations.switchMap(datesLiveData, dates ->
            new LivePagedListBuilder<>(repository.getFinishedTracksDataSource(dates), 10).build()
    );
    private Date[] datesRange;

    @Inject
    public TracksListViewModel(Repository repository) {
        this.repository = repository;
    }

    public void updateTracks(Date[] dates) {
        datesRange = dates;
        datesLiveData.postValue(dates);
    }

    public void setTag(@NonNull Tag tag, Set<Long> selectedIds) {
        worker(() -> {
            repository.updateTracks(tag.getName(), new ArrayList<>(selectedIds));
            updateTracks(datesRange);
        });
    }

    public void deleteTracks(Set<Long> selectedIds) {
        worker(() -> {
            List<Long> idsList = new ArrayList<>(selectedIds);
            long[] ids = new long[selectedIds.size()];
            for(int i=0;i<idsList.size();i++) ids[i] = idsList.get(i);
            repository.deleteTracks(ids);
            updateTracks(datesRange);
        });
    }

}
