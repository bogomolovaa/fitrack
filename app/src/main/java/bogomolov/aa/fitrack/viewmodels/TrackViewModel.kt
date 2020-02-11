package bogomolov.aa.fitrack.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import javax.inject.Inject

import bogomolov.aa.fitrack.repository.Repository
import bogomolov.aa.fitrack.core.model.Point
import bogomolov.aa.fitrack.core.model.Tag
import bogomolov.aa.fitrack.core.model.Track
import bogomolov.aa.fitrack.view.TagResultListener

import bogomolov.aa.fitrack.android.worker


class TrackViewModel @Inject
constructor(private val repository: Repository) : ViewModel(), TagResultListener {
    var trackLiveData = MutableLiveData<Track>()
    var trackPoints = MutableLiveData<List<Point>>()

    fun setTrack(trackId: Long) {
        worker{
            val track = repository.getTracks(trackId)[0]
            trackLiveData.postValue(track)
            trackPoints.postValue(repository.getTrackPoints(track, Point.SMOOTHED))
        }
    }

    override fun onTagSelectionResult(tag: Tag?) {
        val track = trackLiveData.value
        if (tag != null && track != null) {
            track.tag = tag.name
            trackLiveData.postValue(track)
            worker{ repository.save(track) }
        }
    }
}
