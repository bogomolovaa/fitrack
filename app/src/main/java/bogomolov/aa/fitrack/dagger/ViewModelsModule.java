package bogomolov.aa.fitrack.dagger;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import bogomolov.aa.fitrack.viewmodels.MainViewModel;
import bogomolov.aa.fitrack.viewmodels.StatsViewModel;
import bogomolov.aa.fitrack.viewmodels.TrackViewModel;
import bogomolov.aa.fitrack.viewmodels.TracksListViewModel;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class ViewModelsModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel.class)
    abstract ViewModel bindUserViewModel(MainViewModel mainViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(StatsViewModel.class)
    abstract ViewModel bindStatsViewModel(StatsViewModel statsViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(TracksListViewModel.class)
    abstract ViewModel bindTracksListViewModel(TracksListViewModel tracksListViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(TrackViewModel.class)
    abstract ViewModel bindTrackViewModel(TrackViewModel trackViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(ViewModelFactory factory);
}
