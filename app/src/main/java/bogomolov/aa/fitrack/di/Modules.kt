package bogomolov.aa.fitrack.di

import androidx.room.Room
import bogomolov.aa.fitrack.domain.Repository
import bogomolov.aa.fitrack.domain.UseCases
import bogomolov.aa.fitrack.features.main.MainViewModel
import bogomolov.aa.fitrack.features.stats.StatsViewModel
import bogomolov.aa.fitrack.features.tracks.list.TracksListViewModel
import bogomolov.aa.fitrack.features.tracks.tags.TagSelectionViewModel
import bogomolov.aa.fitrack.features.tracks.track.TrackViewModel
import bogomolov.aa.fitrack.repository.AppDatabase
import bogomolov.aa.fitrack.repository.DB_NAME
import bogomolov.aa.fitrack.repository.MapSaver
import bogomolov.aa.fitrack.repository.RepositoryImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

fun buildModules(): List<Module> = listOf(
    module {
        single<Repository> { RepositoryImpl(get()) }
        single {
            Room.databaseBuilder(get(), AppDatabase::class.java, DB_NAME)
                .fallbackToDestructiveMigration().build()
        }
        single {
            MapSaver(get(), get())
        }
        single {
            UseCases(get())
        }
        viewModel { MainViewModel(get(), get()) }
        viewModel { StatsViewModel(get()) }
        viewModel { TracksListViewModel(get()) }
        viewModel { TagSelectionViewModel(get()) }
        viewModel { TrackViewModel(get()) }
    }
)