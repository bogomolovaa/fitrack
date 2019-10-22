package bogomolov.aa.fitrack;

import android.app.Application;

import io.realm.Realm;

public class TrackerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }

}
