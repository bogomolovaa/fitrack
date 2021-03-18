package bogomolov.aa.fitrack.di

import bogomolov.aa.fitrack.domain.Repository
import bogomolov.aa.fitrack.repository.RepositoryImpl
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class BindsModule {
    @Binds
    @Singleton
    abstract fun bindsRepository(repository: RepositoryImpl): Repository
}