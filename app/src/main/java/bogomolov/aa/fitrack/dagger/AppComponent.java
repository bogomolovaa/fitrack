package bogomolov.aa.fitrack.dagger;

import bogomolov.aa.fitrack.TrackerApplication;
import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;

@Component(modules = {AndroidInjectionModule.class, ViewModelsModule.class, InjectionsModule.class, MainModule.class})
public interface AppComponent extends AndroidInjector<TrackerApplication> {

    void inject(TrackerApplication trackerApplication);
}