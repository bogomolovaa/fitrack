package bogomolov.aa.fitrack.viewmodels

import android.content.Context
import android.os.Handler
import android.os.HandlerThread

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import java.util.ArrayList

import javax.inject.Inject

import bogomolov.aa.fitrack.core.*
import bogomolov.aa.fitrack.repository.Repository
import bogomolov.aa.fitrack.core.model.Point
import bogomolov.aa.fitrack.core.model.Track
import bogomolov.aa.fitrack.android.TrackerService

import bogomolov.aa.fitrack.android.worker

class MainViewModel @Inject
constructor(private val repository: Repository) : ViewModel() {
    var distance = MutableLiveData<String>()
    var time = MutableLiveData<String>()
    var avgSpeed = MutableLiveData<String>()
    var speed = MutableLiveData<String>()
    var startStop = MutableLiveData<Boolean>()
    var lastPointLiveData = MutableLiveData<Point>()
    private var backgroundHandler: Handler
    private val handlerThread = HandlerThread("MainViewModel background")

    private var tailSmoothedPoints: MutableList<Point>? = null
    private var windowStartId: Int = 0

    private var updateRunnable: Runnable? = null

    var track: Track? = null
    lateinit var rawPoints: MutableList<Point>
    lateinit var smoothedPoints: MutableList<Point>

    init {
        handlerThread.start()
        backgroundHandler = Handler(handlerThread.looper)
    }

    override fun onCleared() {
        super.onCleared()
        handlerThread.quitSafely()
    }

    fun startTrack(context: Context) {
        if (TrackerService.working) {
            worker{
                val lastPoint = repository.getLastRawPoint()
                if (lastPoint != null) {
                    startTrack(repository, lastPoint)
                    startStop.postValue(false)
                }
            }
        } else {
            TrackerService.startTrackerService(TrackerService.START_SERVICE_ACTION, context)
        }
    }

    fun stopTrack(context: Context) {
        worker{
            val lastTrack = repository.getLastTrack()
            if (lastTrack != null && lastTrack.isOpened()) {
                finishTrack(repository, context, repository.getTrackPoints(lastTrack, Point.RAW), lastTrack, System.currentTimeMillis())
                startStop.postValue(true)
            }
        }
    }

    fun startUpdating() {
        updateRunnable = object : Runnable {
            override fun run() {
                track = repository.getLastTrack()
                val point = repository.getLastRawPoint()
                rawPoints = ArrayList()
                smoothedPoints = ArrayList()

                if (track != null && track!!.isOpened()) {
                    rawPoints.addAll(repository.getTrackPoints(track!!, Point.RAW))
                    smoothedPoints.addAll(getSmoothedPoints(track!!, rawPoints))
                }

                if (track != null && track!!.isOpened()) {
                    distance.postValue(track!!.currentDistance.toInt().toString() + " m")
                    time.postValue(track!!.getTimeString())
                    speed.postValue(String.format("%.1f", 3.6 * track!!.currentSpeed) + " km/h")
                    avgSpeed.postValue(String.format("%.1f", 3.6 * track!!.getSpeedForCurrentDistance()) + " km/h")
                } else {
                    distance.postValue("")
                    time.postValue("")
                    speed.postValue("")
                    avgSpeed.postValue("")
                }
                lastPointLiveData.postValue(point)

                backgroundHandler.postDelayed(this, 1000)
            }
        }
        backgroundHandler.post(updateRunnable!!)
    }

    fun stopUpdating() {
        backgroundHandler.removeCallbacks(updateRunnable!!)
        updateRunnable = null
    }

    private fun getSmoothedPoints(track: Track, points: List<Point>): List<Point> {
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
            tailSmoothedPoints = Track.smooth(preWindowPointsRaw) as MutableList<Point>
            val windowSmoothedPoints = Track.smooth(windowPointsRaw)
            smoothedPoints.addAll(tailSmoothedPoints!!)
            smoothedPoints.addAll(windowSmoothedPoints)
        } else {
            val windowSize = points.size - windowStartId
            if (windowSize >= WINDOW_MAX_SIZE) {
                val windowPointsRaw = getWindowPoints(points, windowSize)
                val secondHalfWindowPointsRaw = getWindowPoints(points, windowSize / 2)
                val firstHalfWindowPointsRaw = getPreWindowPoints(windowPointsRaw, windowSize / 2)
                val secondHalfWindowSmoothedPoints = Track.smooth(secondHalfWindowPointsRaw)
                val firstHalfWindowSmoothedPoints = Track.smooth(firstHalfWindowPointsRaw)
                tailSmoothedPoints!!.addAll(firstHalfWindowSmoothedPoints)
                smoothedPoints.addAll(tailSmoothedPoints!!)
                smoothedPoints.addAll(secondHalfWindowSmoothedPoints)
                windowStartId = points.size - windowSize / 2
            } else {
                val windowPointsRaw = getWindowPoints(points, windowSize)
                val windowSmoothedPoints = Track.smooth(windowPointsRaw)
                smoothedPoints.addAll(tailSmoothedPoints!!)
                smoothedPoints.addAll(windowSmoothedPoints)
            }
        }
        track.currentSpeed = Track.getCurrentSpeed(smoothedPoints)
        track.currentDistance = Point.getTrackDistance(smoothedPoints)
        return smoothedPoints
    }

    private fun getWindowPoints(points: List<Point>, windowSize: Int): List<Point> =
        if (points.size > windowSize) points.subList(points.size - windowSize, points.size) else points

    private fun getPreWindowPoints(points: List<Point>, windowSize: Int): List<Point> =
        if (points.size > windowSize) points.subList(0, points.size - windowSize) else ArrayList()

    companion object {
        private val WINDOW_MAX_SIZE = 50
    }


}
