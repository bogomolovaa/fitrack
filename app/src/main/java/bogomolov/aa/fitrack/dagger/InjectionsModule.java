package bogomolov.aa.fitrack.dagger;

import bogomolov.aa.fitrack.android.TrackerService;
import bogomolov.aa.fitrack.view.activities.MainActivity;
import bogomolov.aa.fitrack.view.fragments.MainFragment;
import bogomolov.aa.fitrack.view.fragments.StatsFragment;
import bogomolov.aa.fitrack.view.fragments.TrackViewFragment;
import bogomolov.aa.fitrack.view.fragments.TracksListFragment;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class InjectionsModule {
    @ContributesAndroidInjector
    public abstract TrackerService bindTrackerService();

    @ContributesAndroidInjector
    public abstract MainActivity bindMainActivity();

    @ContributesAndroidInjector
    public abstract MainFragment bindMainFragment();

    @ContributesAndroidInjector
    public abstract StatsFragment bindStatsFragment();

    @ContributesAndroidInjector
    public abstract TracksListFragment bindTracksListFragment();

    @ContributesAndroidInjector
    public abstract TrackViewFragment bindTrackViewFragment();
}
