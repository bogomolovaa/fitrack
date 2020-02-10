package bogomolov.aa.fitrack.dagger

import bogomolov.aa.fitrack.android.TrackerService
import bogomolov.aa.fitrack.android.WidgetProvider
import bogomolov.aa.fitrack.view.activities.MainActivity
import bogomolov.aa.fitrack.view.fragments.*
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class InjectionsModule {
    @ContributesAndroidInjector
    abstract fun bindWidgetProvider(): WidgetProvider

    @ContributesAndroidInjector
    abstract fun bindTrackerService(): TrackerService

    @ContributesAndroidInjector
    abstract fun bindMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun bindMainFragment(): MainFragment

    @ContributesAndroidInjector
    abstract fun bindStatsFragment(): StatsFragment

    @ContributesAndroidInjector
    abstract fun bindTracksListFragment(): TracksListFragment

    @ContributesAndroidInjector
    abstract fun bindTrackViewFragment(): TrackViewFragment

    @ContributesAndroidInjector
    abstract fun bindFiltersBottomSheetDialogFragment(): FiltersBottomSheetDialogFragment

    @ContributesAndroidInjector
    abstract fun bindTagSelectionDialog(): TagSelectionDialog
}