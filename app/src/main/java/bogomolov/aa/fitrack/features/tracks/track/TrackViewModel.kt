package bogomolov.aa.fitrack.features.tracks.track

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bogomolov.aa.fitrack.domain.Repository
import bogomolov.aa.fitrack.domain.model.Point
import bogomolov.aa.fitrack.domain.model.SMOOTHED
import bogomolov.aa.fitrack.domain.model.Tag
import bogomolov.aa.fitrack.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class TrackViewModel @Inject
constructor(private val repository: Repository) : ViewModel() {
    var trackLiveData = MutableLiveData<Track>()
    var trackPoints = MutableLiveData<List<Point>>()

    fun setTrack(trackId: Long) {
        viewModelScope.launch(Dispatchers.IO){
            val track = repository.getTracks(trackId)[0]
            trackLiveData.postValue(track)
            trackPoints.postValue(repository.getTrackPoints(track, SMOOTHED))
        }
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun setTag(tag: Tag?) {
        val track = trackLiveData.value
        if (tag != null && track != null) {
            track.tag = tag.name
            trackLiveData.postValue(track)
            viewModelScope.launch(Dispatchers.IO){
                repository.save(track)
            }
        }
    }
}