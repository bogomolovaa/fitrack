package bogomolov.aa.fitrack.dagger;

import androidx.appcompat.app.AppCompatActivity;

import bogomolov.aa.fitrack.presenter.MainPresenter;
import bogomolov.aa.fitrack.presenter.StatsPresenter;
import bogomolov.aa.fitrack.presenter.TrackViewPresenter;
import bogomolov.aa.fitrack.presenter.TracksListPresenter;
import bogomolov.aa.fitrack.view.TracksListView;
import bogomolov.aa.fitrack.view.activities.MainActivity;
import bogomolov.aa.fitrack.view.activities.StatsActivity;
import bogomolov.aa.fitrack.view.activities.TrackViewActivity;
import bogomolov.aa.fitrack.view.activities.TracksListActivity;
import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private AppCompatActivity activity;

    public AppModule(AppCompatActivity activity) {
        this.activity = activity;
    }

    @Provides
    MainPresenter getMainPresenter() {
        return new MainPresenter((MainActivity) activity);
    }

    @Provides
    TracksListPresenter getTracksListPresenter() {
        return new TracksListPresenter((TracksListActivity) activity);
    }

    @Provides
    StatsPresenter getStatsPresenter() {
        return new StatsPresenter((StatsActivity) activity);
    }

    @Provides
    TrackViewPresenter getTrackViewPresenter() {
        return new TrackViewPresenter((TrackViewActivity) activity);
    }

}
