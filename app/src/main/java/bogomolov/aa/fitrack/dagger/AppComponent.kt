package bogomolov.aa.fitrack.dagger

import android.app.Application
import bogomolov.aa.fitrack.TrackerApplication
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidInjectionModule::class, ViewModelsModule::class, InjectionsModule::class, MainModule::class])
interface AppComponent : AndroidInjector<TrackerApplication> {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    override fun inject(trackerApplication: TrackerApplication)
}