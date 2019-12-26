package bogomolov.aa.fitrack.viewmodels;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.repository.RepositoryImpl;
import bogomolov.aa.fitrack.core.model.Point;
import bogomolov.aa.fitrack.core.model.Tag;
import bogomolov.aa.fitrack.core.model.Track;
import bogomolov.aa.fitrack.view.TagResultListener;


public class TrackViewModel extends ViewModel implements TagResultListener {
    public MutableLiveData<String> distance = new MutableLiveData<>();
    public MutableLiveData<String> time = new MutableLiveData<>();
    public MutableLiveData<String> avgSpeed = new MutableLiveData<>();
    public MutableLiveData<String> selectedTag = new MutableLiveData<>();
    private RepositoryImpl dbProvider;
    private Track track;

    @Inject
    public TrackViewModel(RepositoryImpl dbProvider){
        this.dbProvider = dbProvider;
    }

    @Override
    protected void onCleared(){
        dbProvider.close();
    }

    public List<Point> getTrackPoints(){
        return dbProvider.getTrackPoints(track, Point.SMOOTHED);
    }

    public Track setTrack(long trackId, Context context){
        track = dbProvider.getTrack(trackId);
        distance.setValue((int) track.getDistance() + " m");
        time.setValue(track.getTimeString());
        avgSpeed.setValue(String.format("%.1f", 3.6 * track.getSpeed()) + " km/h");
        selectedTag.setValue(track.getTag() != null ? track.getTag() : context.getResources().getString(R.string.no_tag));
        return track;
    }

    @Override
    public void onTagSelectionResult(Tag tag) {
        if (tag != null) {
            track.setTag(tag.getName());
            dbProvider.save(track);
            selectedTag.setValue(tag.getName());
        }
    }
}
