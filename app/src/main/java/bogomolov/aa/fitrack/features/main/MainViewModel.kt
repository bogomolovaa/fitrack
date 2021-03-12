package bogomolov.aa.fitrack.features.main

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bogomolov.aa.fitrack.domain.Repository
import bogomolov.aa.fitrack.domain.UseCases
import bogomolov.aa.fitrack.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class MainViewModel @Inject
constructor(
    private val repository: Repository,
    private val useCases: UseCases
) : ViewModel() {
    val distance = MutableLiveData<String>()
    val time = MutableLiveData<String>()
    val avgSpeed = MutableLiveData<String>()
    val speed = MutableLiveData<String>()
    val canStartLiveData = MutableLiveData<Boolean>().also { it.value = true }
    val lastPointLiveData = MutableLiveData<Point>()
    private var tailSmoothedPoints: MutableList<Point>? = null
    private var windowStartId: Int = 0


    var currentTrack: Track? = null
    lateinit var rawPoints: MutableList<Point>
    lateinit var smoothedPoints: MutableList<Point>
    private var updateJob: Job? = null

    fun startTrack() {
        viewModelScope.launch(Dispatchers.IO) {
            val lastPoint = repository.getLastRawPoint()
            if (lastPoint != null) {
                useCases.startTrack(lastPoint)
                canStartLiveData.postValue(false)
            }
        }
    }

    fun stopTrack() {
        viewModelScope.launch(Dispatchers.IO) {
            if (currentTrack != null && currentTrack!!.isOpened()) {
                useCases.finishTrack(openedTrack = currentTrack!!)
                canStartLiveData.postValue(true)
            }
        }
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun startUpdating() {
        updateJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                val track = repository.getLastTrack().also { currentTrack = it }
                val point = repository.getLastRawPoint()
                if (point != null) {
                    rawPoints = ArrayList()
                    smoothedPoints = ArrayList()
                    if (track != null && track.isOpened()) {
                        rawPoints.addAll(repository.getTrackPoints(track, RAW))
                        Log.i("test", "rawPoints")
                        for (point1 in rawPoints) Log.i("test", "$point1")
                        smoothedPoints.addAll(getSmoothedPoints(rawPoints))
                        track.currentSpeed = getCurrentSpeed(smoothedPoints)
                        track.currentDistance = getTrackDistance(smoothedPoints)
                        Log.i("test", "smoothedPoints")
                        for (point1 in smoothedPoints) Log.i("test", "$point1")
                    }
                    if (track != null && track.isOpened()) {
                        distance.postValue(track.currentDistance.toInt().toString() + " m")
                        time.postValue(track.getTimeString())
                        speed.postValue(String.format("%.1f", 3.6 * track.currentSpeed) + " km/h")
                        avgSpeed.postValue(
                            String.format(
                                "%.1f",
                                3.6 * track.getSpeedForCurrentDistance()
                            ) + " km/h"
                        )
                    } else {
                        canStartLiveData.postValue(true)
                        distance.postValue("")
                        time.postValue("")
                        speed.postValue("")
                        avgSpeed.postValue("")
                    }
                    lastPointLiveData.postValue(point)
                }
                delay(1000)
            }
        }
    }

    fun stopUpdating() {
        updateJob?.cancel()
    }

    private fun getSmoothedPoints(points: List<Point>): List<Point> {
        if (points.size < 3) {
            tailSmoothedPoints = null
            return points
        }
        val smoothedPoints = ArrayList<Point>()
        if (tailSmoothedPoints == null) {
            tailSmoothedPoints = ArrayList()
            val windowSize = WINDOW_MAX_SIZE / 2
            windowStartId = Math.max(points.size - windowSize, 0)
            val windowPointsRaw = getWindowPoints(points, windowSize)
            val preWindowPointsRaw = getPreWindowPoints(points, windowSize)
            tailSmoothedPoints = smooth(preWindowPointsRaw) as MutableList<Point>
            val windowSmoothedPoints = smooth(windowPointsRaw)
            smoothedPoints.addAll(tailSmoothedPoints!!)
            smoothedPoints.addAll(windowSmoothedPoints)
        } else {
            val windowSize = points.size - windowStartId
            if (windowSize >= WINDOW_MAX_SIZE) {
                val windowPointsRaw = getWindowPoints(points, windowSize)
                val secondHalfWindowPointsRaw = getWindowPoints(points, windowSize / 2)
                val firstHalfWindowPointsRaw = getPreWindowPoints(windowPointsRaw, windowSize / 2)
                val secondHalfWindowSmoothedPoints = smooth(secondHalfWindowPointsRaw)
                val firstHalfWindowSmoothedPoints = smooth(firstHalfWindowPointsRaw)
                tailSmoothedPoints!!.addAll(firstHalfWindowSmoothedPoints)
                smoothedPoints.addAll(tailSmoothedPoints!!)
                smoothedPoints.addAll(secondHalfWindowSmoothedPoints)
                windowStartId = points.size - windowSize / 2
            } else {
                val windowPointsRaw = getWindowPoints(points, windowSize)
                val windowSmoothedPoints = smooth(windowPointsRaw)
                smoothedPoints.addAll(tailSmoothedPoints!!)
                smoothedPoints.addAll(windowSmoothedPoints)
            }
        }
        return smoothedPoints
    }

    private fun getWindowPoints(points: List<Point>, windowSize: Int): List<Point> =
        if (points.size > windowSize) points.subList(points.size - windowSize, points.size)
        else points

    private fun getPreWindowPoints(points: List<Point>, windowSize: Int): List<Point> =
        if (points.size > windowSize) points.subList(0, points.size - windowSize) else ArrayList()
}

private const val WINDOW_MAX_SIZE = 50
