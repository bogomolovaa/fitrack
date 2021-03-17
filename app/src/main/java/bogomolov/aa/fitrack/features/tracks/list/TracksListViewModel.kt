package bogomolov.aa.fitrack.features.tracks.list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import bogomolov.aa.fitrack.domain.Repository
import bogomolov.aa.fitrack.domain.getTodayRange
import bogomolov.aa.fitrack.domain.model.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class TracksListViewModel @Inject
constructor(private val repository: Repository) : ViewModel() {
    private val datesLiveData = MutableLiveData(getTodayRange())
    var tracksLiveData = Transformations.switchMap(datesLiveData) { dates ->
        LivePagedListBuilder(repository.getFinishedTracksDataSource(dates), 10).build()
    }

    fun updateTracks(dates: Array<Date>) {
        datesLiveData.postValue(dates)
    }

    fun setTag(tag: Tag, selectedIds: Set<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTracks(tag.name!!, ArrayList(selectedIds))
        }
    }

    fun deleteTracks(selectedIds: Set<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            val idsList = ArrayList(selectedIds)
            val ids = LongArray(selectedIds.size)
            for (i in idsList.indices) ids[i] = idsList[i]
            repository.deleteTracks(*ids)
        }
    }
}