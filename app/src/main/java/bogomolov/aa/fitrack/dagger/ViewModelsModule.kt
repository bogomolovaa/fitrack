package bogomolov.aa.fitrack.dagger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import bogomolov.aa.fitrack.viewmodels.*
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelsModule {
    @Binds
    @IntoMap
    @ViewModelKey(TagSelectionViewModel::class)
    abstract fun bindTagSelectionViewModel(tagSelectionViewModel: TagSelectionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindUserViewModel(mainViewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(StatsViewModel::class)
    abstract fun bindStatsViewModel(statsViewModel: StatsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TracksListViewModel::class)
    abstract fun bindTracksListViewModel(tracksListViewModel: TracksListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TrackViewModel::class)
    abstract fun bindTrackViewModel(trackViewModel: TrackViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}