package bogomolov.aa.fitrack.dagger;

import android.app.Application;

import androidx.room.Room;

import bogomolov.aa.fitrack.repository.AppDatabase;
import bogomolov.aa.fitrack.repository.AppDatabaseKt;
import bogomolov.aa.fitrack.repository.Repository;
import bogomolov.aa.fitrack.repository.RepositoryImpl;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class MainModule {
    @Binds
    public abstract Repository bindsRepository(RepositoryImpl repository);

    @Provides
    public static AppDatabase providesAppDatabase(Application application){
        return Room.databaseBuilder(application, AppDatabase.class, AppDatabaseKt.DB_NAME).build();
    }
}
