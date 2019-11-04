package bogomolov.aa.fitrack.dagger;

import bogomolov.aa.fitrack.view.activities.MainActivity;
import bogomolov.aa.fitrack.view.activities.StatsActivity;
import bogomolov.aa.fitrack.view.activities.TrackViewActivity;
import bogomolov.aa.fitrack.view.activities.TracksListActivity;
import dagger.Component;

@Component(modules = AppModule.class)
public interface AppComponent {

    void injectsMainActivity(MainActivity mainActivity);

    void injectsTracksListActivity(TracksListActivity tracksListActivity);

    void injectsStatsActivity(StatsActivity statsActivity);

    void injectsTrackViewActivity(TrackViewActivity trackViewActivity);

}