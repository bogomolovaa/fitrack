package bogomolov.aa.fitrack.model;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class DbProvider {
    private Realm realm;

    public DbProvider() {
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        realm = Realm.getInstance(config);
    }

    public void addPoint(Point point){
        realm.beginTransaction();
        realm.copyToRealm(point);
        realm.commitTransaction();
    }

    public void close(){
        realm.close();
    }
}
