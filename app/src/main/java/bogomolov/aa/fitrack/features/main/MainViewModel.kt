package bogomolov.aa.fitrack.features.main

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

private const val WINDOW_MAX_SIZE = 50
private const val UPDATE_INTERVAL = 1000L

data class MainState(
    var currentTrack: Track? = null,
    var smoothedPoints: List<Point>? = null,
    var distance: String = "",
    var time: String = "",
    var avgSpeed: String = "",
    var speed: String = "",
    var lastPoint: Point? = null,
)

class MainViewModel @Inject
constructor(
    private val repository: Repository,
    private val useCases: UseCases
) : ViewModel() {
    val canStartLiveData = MutableLiveData(true)
    val stateLiveData = MutableLiveData(MainState())
    private var tailSmoothedPoints: MutableList<Point>? = null
    private var windowStartId: Int = 0
    private lateinit var rawPoints: MutableList<Point>
    private var updateJob: Job? = null

    private fun updateState(change: MainState.() -> MainState) {
        stateLiveData.postValue(stateLiveData.value?.change())
    }

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
            val currentTrack = stateLiveData.value?.currentTrack
            if (currentTrack?.isOpened() == true) {
                useCases.finishTrack(openedTrack = currentTrack)
                canStartLiveData.postValue(true)
            }
        }
    }

    fun startUpdating() {
        updateJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                val newState = MainState()
                val track = repository.getLastTrack()
                newState.currentTrack = track
                val point = repository.getLastRawPoint()
                newState.lastPoint = point
                rawPoints = ArrayList()
                val smoothedPoints = ArrayList<Point>()
                if (track != null && track.isOpened()) {
                    rawPoints.addAll(repository.getTrackPoints(track, RAW))
                    smoothedPoints.addAll(getSmoothedPoints(rawPoints))
                    val currentSpeed = getCurrentSpeed(smoothedPoints)
                    val currentDistance = sumDistance(smoothedPoints)
                    newState.distance = currentDistance.toInt().toString() + " m"
                    newState.time = track.getTimeString()
                    newState.speed = String.format("%.1f", 3.6 * currentSpeed) + " km/h"
                    newState.avgSpeed =
                        String.format("%.1f", 3.6 * track.getSpeed(currentDistance)) + " km/h"
                    if (canStartLiveData.value == true) canStartLiveData.postValue(false)
                } else {
                    if (canStartLiveData.value == false) canStartLiveData.postValue(true)
                }
                newState.smoothedPoints = smoothedPoints
                updateState { newState }
                stateLiveData.postValue(newState)
                delay(UPDATE_INTERVAL)
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