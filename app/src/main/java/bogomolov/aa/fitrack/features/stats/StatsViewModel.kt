package bogomolov.aa.fitrack.features.stats

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bogomolov.aa.fitrack.domain.Repository
import bogomolov.aa.fitrack.domain.getWeekRange
import bogomolov.aa.fitrack.domain.model.Tag
import bogomolov.aa.fitrack.domain.model.Track
import bogomolov.aa.fitrack.domain.model.sumTracks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class StatsViewModel(private val repository: Repository) : ViewModel() {
    val sumTrackLiveData = MutableLiveData<Track>()
    val tracksLiveData = MutableLiveData<List<Track>>()
    val tagEntries = MutableLiveData<Array<String>>()
    var datesRange = getWeekRange()
    var timeFilter = FILTER_WEEK
    var param = PARAM_DISTANCE
    var timeStep = TIME_STEP_DAY
    var tagIndex = 0

    init {
        viewModelScope.launch(Dispatchers.IO) {
            tagEntries.postValue(getTagNames(repository.getTags()))
        }
        reloadTracks()
    }

    fun setTag(id: Int) {
        if (id != tagIndex) {
            tagIndex = id
            reloadTracks()
        }
    }

    fun setTimeFilter(datesRange: Array<Date>, selectedTimeFilter: Int) {
        this.datesRange = datesRange
        this.timeFilter = selectedTimeFilter
        reloadTracks()
    }

    private fun reloadTracks() {
        viewModelScope.launch(Dispatchers.IO) {
            val tag = if (tagIndex == 0) null else tagEntries.value?.get(tagIndex)
            val tracks = repository.getFinishedTracks(datesRange, tag)
            sumTrackLiveData.postValue(sumTracks(tracks))
            tracksLiveData.postValue(tracks)
        }
    }
}

private fun getTagNames(tags: List<Tag>) =
    ArrayList(tags).apply { add(0, Tag(name = NO_TAG)) }.map { it.name }.toTypedArray()

private const val NO_TAG = "-"

const val PARAM_DISTANCE = 0
const val PARAM_SPEED = 1
const val PARAM_TIME = 2

const val TIME_STEP_DAY = 0
const val TIME_STEP_WEEK = 1

const val FILTER_TODAY = 0
const val FILTER_WEEK = 1
const val FILTER_MONTH = 2
const val FILTER_SELECT = 3