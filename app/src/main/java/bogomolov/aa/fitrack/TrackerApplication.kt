package bogomolov.aa.fitrack

import android.app.Application
import bogomolov.aa.fitrack.di.buildModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.Koin
import org.koin.core.context.startKoin

class TrackerApplication : Application() {

    lateinit var koin: Koin

    override fun onCreate() {
        super.onCreate()
        koin = startKoin {
            androidLogger()
            androidContext(this@TrackerApplication)
            modules(buildModules())
        }.koin
    }
}
