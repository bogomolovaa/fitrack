package bogomolov.aa.fitrack.di

import android.app.Application
import androidx.room.Room
import bogomolov.aa.fitrack.domain.MapSaver
import bogomolov.aa.fitrack.domain.Repository
import bogomolov.aa.fitrack.repository.AppDatabase
import bogomolov.aa.fitrack.repository.DB_NAME
import bogomolov.aa.fitrack.repository.MapSaverImpl
import bogomolov.aa.fitrack.repository.RepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
abstract class MainModule {
    @Binds
    @Singleton
    abstract fun bindsRepository(repository: RepositoryImpl): Repository

    @Module
    companion object {
        @JvmStatic
        @Singleton
        @Provides
        fun providesAppDatabase(application: Application): AppDatabase =
            Room.databaseBuilder(application, AppDatabase::class.java, DB_NAME)
                .fallbackToDestructiveMigration().build()

        @JvmStatic
        @Singleton
        @Provides
        fun providesMapSaver(application: Application): MapSaver = MapSaverImpl(application)
    }
}