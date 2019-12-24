package bogomolov.aa.fitrack;

import android.app.Application;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;

import bogomolov.aa.fitrack.dagger.DaggerAppComponent;
import io.realm.Realm;

public class TrackerApplication extends Application implements HasAndroidInjector {

    @Inject
    DispatchingAndroidInjector<Object> androidInjector;


    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);

        DaggerAppComponent.create().inject(this);
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return androidInjector;
    }
}
