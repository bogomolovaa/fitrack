package bogomolov.aa.fitrack

import android.app.Application

import javax.inject.Inject

import bogomolov.aa.fitrack.dagger.AppComponent
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector

import bogomolov.aa.fitrack.dagger.DaggerAppComponent


class TrackerApplication : Application(), HasAndroidInjector {

    @Inject
    internal lateinit var androidInjector: DispatchingAndroidInjector<Any>


    override fun onCreate() {
        super.onCreate()

        DaggerAppComponent.builder().application(this).build().inject(this)
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return androidInjector
    }
}
