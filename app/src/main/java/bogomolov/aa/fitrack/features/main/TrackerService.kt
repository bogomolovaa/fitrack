package bogomolov.aa.fitrack.features.main

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavDeepLinkBuilder
import androidx.preference.PreferenceManager
import bogomolov.aa.fitrack.R
import bogomolov.aa.fitrack.android.startActivityRecognition
import bogomolov.aa.fitrack.domain.Repository
import bogomolov.aa.fitrack.domain.UseCases
import bogomolov.aa.fitrack.domain.model.Point
import bogomolov.aa.fitrack.features.settings.SettingsFragment
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
import com.google.android.gms.location.*
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.coroutines.*
import javax.inject.Inject

private const val NOTIFICATION_CHANNEL_ID = "fitrack_channel"

class TrackerService : Service(), ConnectionCallbacks, OnConnectionFailedListener, LocationListener,
    HasAndroidInjector {
    private lateinit var googleApiClient: GoogleApiClient
    private var prevLocation: Location? = null
    private lateinit var locationCallback: LocationCallback
    private var startTime: Long = 0
    private val coroutineScope = CoroutineScope(Dispatchers.IO + Job())

    @Inject
    lateinit var useCases: UseCases

    @Inject
    lateinit var repository: Repository

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector() = androidInjector

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "My Notifications",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.description = "Fitrack"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(false)
            notificationChannel.importance = NotificationManager.IMPORTANCE_LOW
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val resultPendingIntent =
            NavDeepLinkBuilder(this).setComponentName(MainActivity::class.java)
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.settingsFragment)
                .createPendingIntent()
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        notificationBuilder.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_SOUND)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(resources.getString(R.string.notification_title))
            .setContentText(resources.getString(R.string.notification_text))
            .setVibrate(longArrayOf(0L))
            .setContentIntent(resultPendingIntent)
        startForeground(1, notificationBuilder.build())
        googleApiClient = GoogleApiClient.Builder(applicationContext).addApi(LocationServices.API)
            .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action == START_SERVICE_ACTION) {
            googleApiClient.connect()
            working = true
        } else if (intent.action == STOP_SERVICE_ACTION) {
            stopTrackingService()
        }
        return START_NOT_STICKY
    }

    private fun stopTrackingService() {
        stopForeground(true)
        stopSelf()
        working = false
    }

    override fun onConnected(bundle: Bundle?) {
        if (!hasPermission(ACCESS_FINE_LOCATION) && !hasPermission(ACCESS_COARSE_LOCATION)) {
            Toast.makeText(
                this,
                "You need to enable permissions to display location !",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        startLocationUpdates()
    }

    private fun hasPermission(permission: String) =
        ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    private fun areNotEqual(location1: Location?, location2: Location): Boolean {
        if (location1 == null) return true
        return location1.longitude != location2.longitude || location1.latitude != location2.latitude
    }

    private fun startWidgetUpdating() {
        coroutineScope.launch(Dispatchers.IO) {
            while (true) {
                delay(60 * 1000)
                withContext(Dispatchers.Main) {
                    updateWidget(applicationContext)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        startWidgetUpdating()
        startTime = System.currentTimeMillis()
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = UPDATE_INTERVAL
        locationRequest.fastestInterval = FASTEST_INTERVAL

        locationCallback = object : LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult) {
                Log.i(
                    "test",
                    "onLocationResult [${locationResult.lastLocation.latitude}, ${locationResult.lastLocation.longitude}] accuracy${locationResult.lastLocation.accuracy}"
                )
                if (areNotEqual(prevLocation, locationResult.lastLocation)) {
                    val location = locationResult.lastLocation
                    prevLocation = location
                    if (location.accuracy < MAX_LOCATION_ACCURACY) {
                        val point = Point(location.time, location.latitude, location.longitude)
                        coroutineScope.launch(Dispatchers.IO) {
                            if (useCases.onNewPoint(point, startTime))
                                stopServiceAndStartActivityRecognition()
                        }
                    }
                }
            }
        }
        LocationServices.getFusedLocationProviderClient(this)
            .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun stopServiceAndStartActivityRecognition() {
        stopTrackingService()
        startActivityRecognition(this)
    }

    override fun onConnectionSuspended(i: Int) {}
    override fun onConnectionFailed(connectionResult: ConnectionResult) {}
    override fun onLocationChanged(location: Location) {}

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        if (googleApiClient.isConnected) {
            LocationServices.getFusedLocationProviderClient(this)
                .removeLocationUpdates(locationCallback)
            googleApiClient.disconnect()
        }
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object {
        var working = false
    }
}

const val START_SERVICE_ACTION = "start"
const val STOP_SERVICE_ACTION = "stop"
private const val MAX_LOCATION_ACCURACY = 50.0
const val UPDATE_INTERVAL = 1000L
private const val FASTEST_INTERVAL = 1000L

fun startTrackerService(action: String, context: Context) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context).edit()
    prefs.putBoolean(SettingsFragment.KEY_TRACKING, action == START_SERVICE_ACTION)
    prefs.apply()
    val intent = Intent(context, TrackerService::class.java)
    intent.action = action
    ContextCompat.startForegroundService(context, intent)
}