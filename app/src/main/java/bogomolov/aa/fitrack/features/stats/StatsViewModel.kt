package bogomolov.aa.fitrack.features.stats

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import bogomolov.aa.fitrack.domain.Repository
import bogomolov.aa.fitrack.domain.getWeekRange
import bogomolov.aa.fitrack.domain.model.Tag
import bogomolov.aa.fitrack.domain.model.Track
import bogomolov.aa.fitrack.domain.model.sumTracks
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class StatsViewModel @Inject
constructor(private val repository: Repository) : ViewModel() {

    var trackLiveData = MutableLiveData<Track>()
    var selectedPeriod = MutableLiveData<String>()
    var tagEntries = liveData {
        viewModelScope.launch(Dispatchers.IO) {
            emit(getTagNames(repository.getTags()))
        }
    }
    var tracksLiveData = MutableLiveData<List<Track>>()

    var selectedTagLiveData = MutableLiveData<Int>()
    var selectedTimeStepLiveData = MutableLiveData<Int>()
    var selectedParamLiveData = MutableLiveData<Int>()

    private var tracks: List<Track>? = null
    var datesRange = getWeekRange()
    private var selectedTag = NO_TAG
    var selectedTagId: Int = 0
    var selectedTimeFilter = FILTER_WEEK
    var selectedParam = PARAM_DISTANCE
    var selectedTimeStep = TIME_STEP_DAY


    init {
        selectedTagLiveData.observeForever { id ->
            if (tagEntries.value != null && id != selectedTagId) {
                selectedTag = tagEntries.value!![id]
                selectedTagId = id
                updateView(true)
            }
        }
        selectedTimeStepLiveData.observeForever { id ->
            if (id != selectedTimeStep) {
                selectedTimeStep = id
                updateView(true)
            }
        }
        selectedParamLiveData.observeForever { id ->
            if (id != selectedParam) {
                selectedParam = id
                updateView(true)
            }
        }
    }

    fun setTimeFilter(datesRange: Array<Date>, selectedTimeFilter: Int) {
        this.datesRange = datesRange
        this.selectedTimeFilter = selectedTimeFilter
        updateView(true)
    }

    fun updateView(reload: Boolean) {
        val startDateString = SimpleDateFormat("dd.MM.yyyy HH:mm").format(datesRange[0])
        val endDateString = SimpleDateFormat("dd.MM.yyyy HH:mm").format(datesRange[1])
        selectedPeriod.setValue("$startDateString - $endDateString")

        viewModelScope.launch(Dispatchers.IO) {
            val tracks =
                if (reload) {
                    repository.getFinishedTracks(
                        datesRange,
                        if (selectedTag == NO_TAG) null else selectedTag
                    )
                } else {
                    this@StatsViewModel.tracks
                }
            this@StatsViewModel.tracks = tracks
            val sumTrack = sumTracks(tracks!!)
            trackLiveData.postValue(sumTrack)
            tracksLiveData.postValue(tracks!!)
        }
    }


    private fun getTagNames(tags: List<Tag>) =
        ArrayList(tags).apply { add(0, Tag(name = NO_TAG)) }.map { it.name!! }.toTypedArray()

    companion object {
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
    }

}
