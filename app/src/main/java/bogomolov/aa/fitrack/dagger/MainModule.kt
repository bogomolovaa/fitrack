package bogomolov.aa.fitrack.dagger

import android.app.Application
import androidx.room.Room
import bogomolov.aa.fitrack.repository.AppDatabase
import bogomolov.aa.fitrack.repository.DB_NAME
import bogomolov.aa.fitrack.repository.Repository
import bogomolov.aa.fitrack.repository.RepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
abstract class MainModule {
    @Binds
    abstract fun bindsRepository(repository: RepositoryImpl): Repository

    @Module
    companion object {
        @JvmStatic
        @Provides
        fun providesAppDatabase(application: Application): AppDatabase =
                Room.databaseBuilder(application, AppDatabase::class.java, DB_NAME).build()
    }
}