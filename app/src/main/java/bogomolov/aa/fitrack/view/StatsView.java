package bogomolov.aa.fitrack.view;

import java.util.Date;
import java.util.List;

import bogomolov.aa.fitrack.model.Track;

public interface StatsView {
    String NO_TAG = "-";

    int PARAM_DISTANCE = 0;
    int PARAM_SPEED = 1;
    int PARAM_TIME = 2;

    int TIME_STEP_DAY = 0;
    int TIME_STEP_WEEK = 1;

    int FILTER_TODAY = 0;
    int FILTER_WEEK = 1;
    int FILTER_MONTH = 2;
    int FILTER_SELECT = 3;

    void updateView(Date[] datesRange, List<Track> tracks, int selectedParam, int selectedTimeStep, int selectedTimeFilter);
}
