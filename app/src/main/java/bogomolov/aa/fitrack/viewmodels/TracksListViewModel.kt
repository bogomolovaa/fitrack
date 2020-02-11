package bogomolov.aa.fitrack.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList

import java.util.ArrayList
import java.util.Date

import javax.inject.Inject

import bogomolov.aa.fitrack.repository.Repository
import bogomolov.aa.fitrack.core.model.Tag
import bogomolov.aa.fitrack.core.model.Track

import bogomolov.aa.fitrack.android.Rx.worker
import bogomolov.aa.fitrack.android.worker

class TracksListViewModel @Inject
constructor(private val repository: Repository) : ViewModel() {
    private val datesLiveData = MutableLiveData<Array<Date>>()
    var tracksLiveData = Transformations.switchMap(datesLiveData) { dates ->
        LivePagedListBuilder(repository.getFinishedTracksDataSource(dates), 10).build()
    }
    private var datesRange: Array<Date>? = null

    fun updateTracks(dates: Array<Date>?) {
        datesRange = dates
        datesLiveData.postValue(dates)
    }

    fun setTag(tag: Tag, selectedIds: Set<Long>) {
        worker {
            repository.updateTracks(tag.name!!, ArrayList(selectedIds))
            updateTracks(datesRange)
        }
    }

    fun deleteTracks(selectedIds: Set<Long>) {
        worker {
            val idsList = ArrayList(selectedIds)
            val ids = LongArray(selectedIds.size)
            for (i in idsList.indices) ids[i] = idsList[i]
            repository.deleteTracks(*ids)
            updateTracks(datesRange)
        }
    }

}
