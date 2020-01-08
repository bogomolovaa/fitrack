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
    public MutableLiveData<String> distance = new MutableLiveData<>();
    public MutableLiveData<String> time = new MutableLiveData<>();
    public MutableLiveData<String> avgSpeed = new MutableLiveData<>();
    public MutableLiveData<String> selectedTag = new MutableLiveData<>();
    public MutableLiveData<String> trackName = new MutableLiveData<>();
    public MutableLiveData<List<Point>> trackPoints = new MutableLiveData<>();
    private Repository repository;
    private Track track;

    @Inject
    public TrackViewModel(Repository repository) {
        this.repository = repository;
    }

    public void setTrack(long trackId, Context context) {
        worker(() -> {
            track = repository.getTracks(trackId).get(0);
            distance.postValue((int) track.getDistance() + " m");
            time.postValue(track.getTimeString());
            avgSpeed.postValue(String.format("%.1f", 3.6 * track.getSpeed()) + " km/h");
            selectedTag.postValue(track.getTag() != null ? track.getTag() : context.getResources().getString(R.string.no_tag));
            trackName.postValue(track.getName());
            trackPoints.postValue(repository.getTrackPoints(track, Point.SMOOTHED));
        });
    }

    @Override
    public void onTagSelectionResult(Tag tag) {
        if (tag != null) {
            track.setTag(tag.getName());
            selectedTag.postValue(tag.getName());
            worker(() -> repository.save(track));
        }
    }
}
