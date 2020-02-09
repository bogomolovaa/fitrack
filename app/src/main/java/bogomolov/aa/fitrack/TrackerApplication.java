package bogomolov.aa.fitrack;

import android.app.Application;

import javax.inject.Inject;

import bogomolov.aa.fitrack.dagger.AppComponent;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;

import bogomolov.aa.fitrack.dagger.DaggerAppComponent;


public class TrackerApplication extends Application implements HasAndroidInjector {

    @Inject
    DispatchingAndroidInjector<Object> androidInjector;


    @Override
    public void onCreate() {
        super.onCreate();

        DaggerAppComponent.builder().application(this).build().inject(this);
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return androidInjector;
    }
}
