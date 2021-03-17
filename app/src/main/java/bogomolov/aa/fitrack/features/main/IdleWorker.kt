package bogomolov.aa.fitrack.features.main

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.PowerManager
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.math.abs
import kotlin.math.max

class IdleWorker(private val appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    private lateinit var sensorManager: SensorManager
    private var maxAcceleration = 0f


    override fun doWork(): Result {
        Log.i("test", "doWork")
        maxAcceleration = 0f
        startAccelerometer()
        runBlocking {
            delay(60 * 1000)
        }
        release()
        if (maxAcceleration < 0.1) {
            Log.i("test", "maxAcceleration $maxAcceleration stop tracking service")
            trackerService(STOP_SERVICE_ACTION, appContext)
        }
        return Result.success()
    }

    private fun release() {
        sensorManager.unregisterListener(accelerometerListener)
    }

    private fun startAccelerometer() {
        sensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)?.let {
            Log.i("test", "accelerometer registered")
            sensorManager.registerListener(
                accelerometerListener, it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    private val accelerometerListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null) {
                val ax = event.values[0]
                val ay = event.values[1]
                val az = event.values[2]

                val acceleration = abs(ax) + abs(ay) + abs(az)
                maxAcceleration = max(acceleration, maxAcceleration)
                Log.i("test", "accelerometer [$ax,$ay,$az] acceleration $acceleration")
            } else {
                Log.i("test", "accelerometer null event")
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }
}

