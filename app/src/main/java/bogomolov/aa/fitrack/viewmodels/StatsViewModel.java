package bogomolov.aa.fitrack.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import bogomolov.aa.fitrack.repository.Repository;
import bogomolov.aa.fitrack.core.model.Tag;
import bogomolov.aa.fitrack.core.model.Track;

import static bogomolov.aa.fitrack.core.DateUtils.getTodayRange;
import static bogomolov.aa.fitrack.core.Rx.worker;

public class StatsViewModel extends ViewModel {
    public static final String NO_TAG = "-";

    public static final int PARAM_DISTANCE = 0;
    public static final int PARAM_SPEED = 1;
    public static final int PARAM_TIME = 2;

    public static final int TIME_STEP_DAY = 0;
    public static final int TIME_STEP_WEEK = 1;

    public static final int FILTER_TODAY = 0;
    public static final int FILTER_WEEK = 1;
    public static final int FILTER_MONTH = 2;
    public static final int FILTER_SELECT = 3;

    public MutableLiveData<String> distance = new MutableLiveData<>();
    public MutableLiveData<String> time = new MutableLiveData<>();
    public MutableLiveData<String> selectedPeriod = new MutableLiveData<>();
    public MutableLiveData<String> speed = new MutableLiveData<>();
    public MutableLiveData<String[]> tagEntries = new MutableLiveData<>();
    public MutableLiveData<List<Track>> tracksLiveData = new MutableLiveData<>();

    private List<Track> tracks;
    private Repository repository;
    public Date[] datesRange;
    private String selectedTag = NO_TAG;
    public int selectedTimeFilter = FILTER_TODAY;
    public int selectedParam;
    public int selectedTimeStep;


    @Inject
    public StatsViewModel(Repository repository) {
        this.repository = repository;
        datesRange = getTodayRange();
        selectedParam = PARAM_DISTANCE;
        selectedTimeStep = TIME_STEP_DAY;

        worker(() -> tagEntries.postValue(getTagNames(repository.getTags())));
    }

    public void setTimeFilter(Date[] datesRange, int selectedTimeFilter) {
        this.datesRange = datesRange;
        this.selectedTimeFilter = selectedTimeFilter;
        updateView(true);
    }

    public void setTagFilter(String selectedTag) {
        this.selectedTag = selectedTag;
        updateView(true);
    }

    public void setParam(int selectedParam) {
        this.selectedParam = selectedParam;
        updateView(false);
    }

    public void setTimeStep(int selectedTimeStep) {
        this.selectedTimeStep = selectedTimeStep;
        updateView(false);
    }

    public void updateView(boolean reload) {
        String startDateString = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(datesRange[0]);
        String endDateString = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(datesRange[1]);
        selectedPeriod.setValue(startDateString + " - " + endDateString);

        worker(() -> {
            List<Track> tracks = reload ? repository.getFinishedTracks(datesRange, selectedTag.equals(NO_TAG) ? null : selectedTag) : StatsViewModel.this.tracks;
            StatsViewModel.this.tracks = tracks;
            Track sumTrack = Track.sumTracks(tracks);
            distance.postValue((int) sumTrack.getDistance() + " m");
            time.postValue(sumTrack.getTimeString());
            speed.postValue(String.format("%.1f", 3.6 * sumTrack.getSpeed()) + " km/h");
            tracksLiveData.postValue(tracks);
        });
    }


    private String[] getTagNames(List<Tag> tags) {
        String[] tagNames = new String[tags.size() + 1];
        tagNames[0] = NO_TAG;
        for (int i = 0; i < tags.size(); i++) tagNames[i + 1] = tags.get(i).getName();
        return tagNames;
    }

}
