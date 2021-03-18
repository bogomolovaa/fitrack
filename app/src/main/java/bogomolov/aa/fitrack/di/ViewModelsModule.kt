package bogomolov.aa.fitrack.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import bogomolov.aa.fitrack.features.main.MainViewModel
import bogomolov.aa.fitrack.features.stats.StatsViewModel
import bogomolov.aa.fitrack.features.tracks.list.TracksListViewModel
import bogomolov.aa.fitrack.features.tracks.tags.TagSelectionViewModel
import bogomolov.aa.fitrack.features.tracks.track.TrackViewModel
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import kotlin.reflect.KClass

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

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)