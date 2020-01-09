package bogomolov.aa.fitrack.viewmodels;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.repository.Repository;
import bogomolov.aa.fitrack.core.model.Point;
import bogomolov.aa.fitrack.core.model.Tag;
import bogomolov.aa.fitrack.core.model.Track;
import bogomolov.aa.fitrack.view.TagResultListener;

import static bogomolov.aa.fitrack.android.Rx.worker;


public class TrackViewModel extends ViewModel implements TagResultListener {
    public MutableLiveData<Track> trackLiveData = new MutableLiveData<>();
    public MutableLiveData<List<Point>> trackPoints = new MutableLiveData<>();
    private Repository repository;

    @Inject
    public TrackViewModel(Repository repository) {
        this.repository = repository;
    }

    public void setTrack(long trackId) {
        worker(() -> {
            Track track = repository.getTracks(trackId).get(0);
            trackLiveData.postValue(track);
            trackPoints.postValue(repository.getTrackPoints(track, Point.SMOOTHED));
        });
    }

    @Override
    public void onTagSelectionResult(Tag tag) {
        Track track = trackLiveData.getValue();
        if (tag != null && track != null) {
            track.setTag(tag.getName());
            trackLiveData.postValue(track);
            worker(() -> repository.save(track));
        }
    }
}
