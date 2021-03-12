package bogomolov.aa.fitrack.di

import bogomolov.aa.fitrack.features.main.MainActivity
import bogomolov.aa.fitrack.features.main.MainFragment
import bogomolov.aa.fitrack.features.main.TrackerService
import bogomolov.aa.fitrack.features.main.WidgetProvider
import bogomolov.aa.fitrack.features.stats.FiltersBottomSheetDialogFragment
import bogomolov.aa.fitrack.features.stats.StatsFragment
import bogomolov.aa.fitrack.features.tracks.list.TracksListFragment
import bogomolov.aa.fitrack.features.tracks.tags.TagSelectionDialog
import bogomolov.aa.fitrack.features.tracks.track.TrackViewFragment
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