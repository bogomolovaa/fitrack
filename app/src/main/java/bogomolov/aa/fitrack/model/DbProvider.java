package bogomolov.aa.fitrack.model;

import android.content.Context;
import android.util.Log;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class DbProvider {
    private Realm realm;

    private static final String DB_PROVIDER = "DbProvider";

    public DbProvider() {
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        realm = Realm.getInstance(config);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
            }
        });
    }

    public void addPoint(Point point) {
        Number maxId = realm.where(Point.class).max("id");
        long id = maxId != null ? maxId.longValue() : 0 + 1;
        point.setId(id);
        realm.beginTransaction();
        realm.copyToRealm(point);
        realm.commitTransaction();
        Log.i(DB_PROVIDER, "added point lat : " + point.getLat() + " lng : " + point.getLng());
    }

    public void close() {
        realm.close();
    }
}
