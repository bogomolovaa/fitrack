package bogomolov.aa.fitrack.dagger;

import android.app.Application;

import com.google.android.gms.common.SignInButton;

import javax.inject.Singleton;

import bogomolov.aa.fitrack.TrackerApplication;
import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;

@Singleton
@Component(modules = {AndroidInjectionModule.class, ViewModelsModule.class, InjectionsModule.class, MainModule.class})
public interface AppComponent extends AndroidInjector<TrackerApplication> {

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        AppComponent build();
    }

    void inject(TrackerApplication trackerApplication);
}