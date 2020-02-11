package bogomolov.aa.fitrack.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import java.text.SimpleDateFormat
import java.util.Date

import javax.inject.Inject

import bogomolov.aa.fitrack.repository.Repository
import bogomolov.aa.fitrack.core.model.Tag
import bogomolov.aa.fitrack.core.model.Track
import io.reactivex.Observable

import bogomolov.aa.fitrack.android.worker
import bogomolov.aa.fitrack.core.getWeekRange

class StatsViewModel @Inject
constructor(private val repository: Repository) : ViewModel() {

    var trackLiveData = MutableLiveData<Track>()
    var selectedPeriod = MutableLiveData<String>()
    var tagEntries = MutableLiveData<Array<String>>()
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
        worker { tagEntries.postValue(getTagNames(repository.getTags())) }

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

        worker {
            val tracks =
                    if (reload)
                        repository.getFinishedTracks(datesRange, if (selectedTag == NO_TAG) null else selectedTag)
                    else this@StatsViewModel.tracks
            this@StatsViewModel.tracks = tracks
            val sumTrack = Track.sumTracks(tracks!!)
            trackLiveData.postValue(sumTrack)
            tracksLiveData.postValue(tracks)
        }
    }

    private fun getTagNames(tags: List<Tag>): Array<String> =
            Observable.fromIterable(tags).startWith(Tag(NO_TAG)).map<String> { it.name }.toList().blockingGet().toTypedArray()

    companion object {
        private val NO_TAG = "-"

        val PARAM_DISTANCE = 0
        val PARAM_SPEED = 1
        val PARAM_TIME = 2

        val TIME_STEP_DAY = 0
        val TIME_STEP_WEEK = 1

        val FILTER_TODAY = 0
        val FILTER_WEEK = 1
        val FILTER_MONTH = 2
        val FILTER_SELECT = 3
    }

}
