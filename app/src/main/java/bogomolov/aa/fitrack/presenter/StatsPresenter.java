package bogomolov.aa.fitrack.presenter;

import java.util.Date;
import java.util.List;

import bogomolov.aa.fitrack.model.DbProvider;
import bogomolov.aa.fitrack.model.Tag;
import bogomolov.aa.fitrack.model.Track;
import bogomolov.aa.fitrack.view.StatsView;

import static bogomolov.aa.fitrack.model.DateUtils.getTodayRange;
import static bogomolov.aa.fitrack.view.StatsView.*;

public class StatsPresenter {
    private List<Track> tracks;
    private DbProvider dbProvider;
    private StatsView statsView;
    private Date[] datesRange;
    private String selectedTag = NO_TAG;
    private int selectedTimeFilter = FILTER_TODAY;
    private int selectedParam;
    private int selectedTimeStep;

    public StatsPresenter(StatsView statsView) {
        this.statsView = statsView;
        dbProvider = new DbProvider(false);

        datesRange = getTodayRange();
        selectedParam = PARAM_DISTANCE;
        selectedTimeStep = TIME_STEP_DAY;

        loadTracks();
        updateView();
    }

    public void setTimeFilter(Date[] datesRange, int selectedTimeFilter) {
        this.datesRange = datesRange;
        this.selectedTimeFilter = selectedTimeFilter;
        loadTracks();
        updateView();
    }

    public void setTagFilter(String selectedTag){
        this.selectedTag = selectedTag;
        loadTracks();
        updateView();
    }

    public void setParam(int selectedParam){
        this.selectedParam = selectedParam;
        updateView();
    }

    public void setTimeStep(int selectedTimeStep){
        this.selectedTimeStep = selectedTimeStep;
        updateView();
    }

    private void updateView(){
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

    public void onDestroy() {
        dbProvider.close();
    }
}
