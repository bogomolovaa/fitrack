package bogomolov.aa.fitrack.di

import android.app.Application
import androidx.room.Room
import bogomolov.aa.fitrack.domain.Repository
import bogomolov.aa.fitrack.repository.AppDatabase
import bogomolov.aa.fitrack.repository.DB_NAME
import bogomolov.aa.fitrack.repository.MapSaver
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object ProvidesModule {
    @Singleton
    @Provides
    fun providesAppDatabase(application: Application): AppDatabase =
        Room.databaseBuilder(application, AppDatabase::class.java, DB_NAME)
            .fallbackToDestructiveMigration().build()

    @Singleton
    @Provides
    fun providesMapSaver(application: Application, repository: Repository): MapSaver =
        MapSaver(application, repository)
}