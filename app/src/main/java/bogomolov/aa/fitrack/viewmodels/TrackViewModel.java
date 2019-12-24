package bogomolov.aa.fitrack.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.model.DbProvider;
import bogomolov.aa.fitrack.model.Point;
import bogomolov.aa.fitrack.model.Tag;
import bogomolov.aa.fitrack.model.Track;
import bogomolov.aa.fitrack.view.TagResultListener;


public class TrackViewModel extends AndroidViewModel implements TagResultListener {
    public MutableLiveData<String> distance = new MutableLiveData<>();
    public MutableLiveData<String> time = new MutableLiveData<>();
    public MutableLiveData<String> avgSpeed = new MutableLiveData<>();
    public MutableLiveData<String> selectedTag = new MutableLiveData<>();
    private DbProvider dbProvider;
    private Track track;

    public TrackViewModel(Application application){
        super(application);
        dbProvider = new DbProvider(false);
    }

    @Override
    protected void onCleared(){
        dbProvider.close();
    }

    public List<Point> getTrackPoints(){
        return dbProvider.getTrackPoints(track, Point.SMOOTHED);
    }

    public Track setTrack(long trackId){
        track = dbProvider.getTrack(trackId);
        distance.setValue((int) track.getDistance() + " m");
        time.setValue(track.getTimeString());
        avgSpeed.setValue(String.format("%.1f", 3.6 * track.getSpeed()) + " km/h");
        selectedTag.setValue(track.getTag() != null ? track.getTag() : getApplication().getApplicationContext().getResources().getString(R.string.no_tag));
        return track;
    }

    @Override
    public void onTagSelectionResult(Tag tag) {
        if (tag != null) {
            dbProvider.getRealm().beginTransaction();
            track.setTag(tag.getName());
            dbProvider.getRealm().commitTransaction();
            selectedTag.setValue(tag.getName());
        }
    }
}
