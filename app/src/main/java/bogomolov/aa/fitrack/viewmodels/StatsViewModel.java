package bogomolov.aa.fitrack.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import bogomolov.aa.fitrack.model.DbProvider;
import bogomolov.aa.fitrack.model.Tag;
import bogomolov.aa.fitrack.model.Track;
import bogomolov.aa.fitrack.view.StatsView;

import static bogomolov.aa.fitrack.model.DateUtils.getTodayRange;
import static bogomolov.aa.fitrack.view.StatsView.FILTER_TODAY;
import static bogomolov.aa.fitrack.view.StatsView.NO_TAG;
import static bogomolov.aa.fitrack.view.StatsView.PARAM_DISTANCE;
import static bogomolov.aa.fitrack.view.StatsView.TIME_STEP_DAY;

public class StatsViewModel extends ViewModel {
    public MutableLiveData<String> distance = new MutableLiveData<>();
    public MutableLiveData<String> time = new MutableLiveData<>();
    public MutableLiveData<String> selectedPeriod = new MutableLiveData<>();
    public MutableLiveData<String> speed = new MutableLiveData<>();

    private List<Track> tracks;
    private DbProvider dbProvider;
    private Date[] datesRange;
    private String selectedTag = NO_TAG;
    private int selectedTimeFilter = FILTER_TODAY;
    private int selectedParam;
    private int selectedTimeStep;


    @Inject
    public StatsViewModel(DbProvider dbProvider) {
        this.dbProvider = dbProvider;

        datesRange = getTodayRange();
        selectedParam = PARAM_DISTANCE;
        selectedTimeStep = TIME_STEP_DAY;

        loadTracks();
        //updateView();
    }

    @Override
    protected void onCleared() {
        dbProvider.close();
    }


    public void setTimeFilter(Date[] datesRange, int selectedTimeFilter, StatsView statsView) {
        this.datesRange = datesRange;
        this.selectedTimeFilter = selectedTimeFilter;
        loadTracks();
        updateView(statsView);
    }

    public void setTagFilter(String selectedTag, StatsView statsView){
        this.selectedTag = selectedTag;
        loadTracks();
        updateView(statsView);
    }

    public void setParam(int selectedParam, StatsView statsView){
        this.selectedParam = selectedParam;
        updateView(statsView);
    }

    public void setTimeStep(int selectedTimeStep, StatsView statsView){
        this.selectedTimeStep = selectedTimeStep;
        updateView(statsView);
    }

    public void updateView(StatsView statsView){
        String startDateString = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(datesRange[0]);
        String endDateString = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(datesRange[1]);
        selectedPeriod.setValue(startDateString + " - " + endDateString);

        Track sumTrack = Track.sumTracks(tracks);

        distance.setValue((int) sumTrack.getDistance() + " m");
        time.setValue(sumTrack.getTimeString());
        speed.setValue(String.format("%.1f", 3.6 * sumTrack.getSpeed()) + " km/h");
        statsView.updateView(datesRange, tracks, selectedParam, selectedTimeStep, selectedTimeFilter);
    }

    private void loadTracks(){
        tracks = selectedTag.equals(NO_TAG) ? dbProvider.getFinishedTracks(datesRange) : dbProvider.getFinishedTracks(datesRange, selectedTag);
    }

    public String[] getTagNames() {
        List<Tag> tags = dbProvider.getTags();
        String[] tagNames = new String[tags.size() + 1];
        tagNames[0] = NO_TAG;
        for (int i = 0; i < tags.size(); i++) tagNames[i + 1] = tags.get(i).getName();
        return tagNames;
    }

}
