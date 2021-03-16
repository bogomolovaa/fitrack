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
    val lastPointLiveData = MutableLiveData<Point?>()
    private var tailSmoothedPoints: MutableList<Point>? = null
    private var windowStartId: Int = 0


    var currentTrack: Track? = null
    private lateinit var rawPoints: MutableList<Point>
    lateinit var smoothedPoints: MutableList<Point>
    private var updateJob: Job? = null

    fun startTrack() {
        viewModelScope.launch(Dispatchers.IO) {
            val lastPoint = repository.getLastRawPoint()
            if (lastPoint != null) {
                useCases.startTrack(lastPoint, System.currentTimeMillis())
                canStartLiveData.postValue(false)
            }
        }
    }

    fun stopTrack() {
        viewModelScope.launch(Dispatchers.IO) {
            if (currentTrack != null && currentTrack!!.isOpened()) {
                useCases.finishTrack(openedTrack = currentTrack!!)
                currentTrack = null
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
                rawPoints = ArrayList()
                smoothedPoints = ArrayList()
                if (track != null && track.isOpened()) {
                    rawPoints.addAll(repository.getTrackPoints(track, RAW))
                    smoothedPoints.addAll(getSmoothedPoints(rawPoints))
                    val currentSpeed = getCurrentSpeed(smoothedPoints)
                    val currentDistance = getTrackDistance(smoothedPoints)
                    Log.i(
                        "test",
                        "rawPoints ${rawPoints.size} smoothedPoints ${smoothedPoints.size}"
                    )

                    distance.postValue(currentDistance.toInt().toString() + " m")
                    time.postValue(track.getTimeString())
                    speed.postValue(String.format("%.1f", 3.6 * currentSpeed) + " km/h")
                    avgSpeed.postValue(
                        String.format("%.1f", 3.6 * track.getSpeed(currentDistance)) + " km/h"
                    )
                    if (canStartLiveData.value == true) canStartLiveData.postValue(false)
                } else {
                    if (canStartLiveData.value == false) canStartLiveData.postValue(true)
                    distance.postValue("")
                    time.postValue("")
                    speed.postValue("")
                    avgSpeed.postValue("")
                }
                lastPointLiveData.postValue(point)
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
            smoothedPoints.addAll(tailSmoothedPoints!!)
            smoothedPoints.addAll(smooth(windowPointsRaw))
        } else {
            val windowSize = points.size - windowStartId
            val windowPointsRaw = getWindowPoints(points, windowSize)
            if (windowSize >= WINDOW_MAX_SIZE) {
                val secondHalfWindowPointsRaw = getWindowPoints(points, windowSize / 2)
                val firstHalfWindowPointsRaw = getPreWindowPoints(windowPointsRaw, windowSize / 2)
                tailSmoothedPoints!!.addAll(smooth(firstHalfWindowPointsRaw))
                smoothedPoints.addAll(tailSmoothedPoints!!)
                smoothedPoints.addAll(smooth(secondHalfWindowPointsRaw))
                windowStartId = points.size - windowSize / 2
            } else {
                smoothedPoints.addAll(tailSmoothedPoints!!)
                smoothedPoints.addAll(smooth(windowPointsRaw))
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
