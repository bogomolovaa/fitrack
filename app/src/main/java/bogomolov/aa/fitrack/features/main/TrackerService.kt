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
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavDeepLinkBuilder
import androidx.preference.PreferenceManager
import bogomolov.aa.fitrack.R
import bogomolov.aa.fitrack.domain.Repository
import bogomolov.aa.fitrack.domain.UseCases
import bogomolov.aa.fitrack.domain.model.Point
import bogomolov.aa.fitrack.features.settings.KEY_SERVICE_STARTED
import bogomolov.aa.fitrack.features.settings.KEY_TRACKING_ENABLED
import bogomolov.aa.fitrack.features.settings.setSetting
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
            val appName = resources.getString(R.string.app_name)
            @SuppressLint("WrongConstant") val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, appName, NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.description = appName
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null || intent.action == START_SERVICE_ACTION) {
            googleApiClient.connect()
            setSetting(KEY_SERVICE_STARTED, true, applicationContext)
        } else if (intent.action == STOP_SERVICE_ACTION) {
            stopTrackingService()
        }
        return START_STICKY
    }

    private fun stopTrackingService() {
        stopForeground(true)
        stopSelf()
    }

    override fun onConnected(bundle: Bundle?) {
        if (!hasPermission(ACCESS_FINE_LOCATION) && !hasPermission(ACCESS_COARSE_LOCATION)) {
            Toast.makeText(
                this,
                resources.getString(R.string.mandatory_permission_string),
                Toast.LENGTH_LONG
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
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = UPDATE_INTERVAL
        locationRequest.fastestInterval = FASTEST_INTERVAL
        locationCallback = object : LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult) {
                if (areNotEqual(prevLocation, locationResult.lastLocation)) {
                    val location = locationResult.lastLocation
                    prevLocation = location
                    if (location.accuracy < MAX_LOCATION_ACCURACY) {
                        val point = Point(
                            time = location.time,
                            lat = location.latitude,
                            lng = location.longitude
                        )
                        coroutineScope.launch(Dispatchers.IO) {
                            useCases.onNewPoint(point)
                        }
                    }
                }
            }
        }
        LocationServices.getFusedLocationProviderClient(this)
            .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    override fun onConnectionSuspended(i: Int) {}
    override fun onConnectionFailed(connectionResult: ConnectionResult) {}
    override fun onLocationChanged(location: Location) {}

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        stopLocationUpdates()
        runBlocking(Dispatchers.IO) {
            useCases.onStopTracking()
        }
        startActivityRecognition(applicationContext)
        setSetting(KEY_SERVICE_STARTED, false, applicationContext)
    }

    private fun stopLocationUpdates() {
        if (googleApiClient.isConnected) {
            LocationServices.getFusedLocationProviderClient(this)
                .removeLocationUpdates(locationCallback)
            googleApiClient.disconnect()
        }
    }

    override fun onBind(intent: Intent) = null
}

const val START_SERVICE_ACTION = "start"
const val STOP_SERVICE_ACTION = "stop"
private const val MAX_LOCATION_ACCURACY = 50.0
private const val UPDATE_INTERVAL = 1000L
private const val FASTEST_INTERVAL = 1000L

fun trackerService(action: String, context: Context) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    if (!prefs.getBoolean(KEY_TRACKING_ENABLED, true) && action == START_SERVICE_ACTION) return
    val intent = Intent(context, TrackerService::class.java)
    intent.action = action
    ContextCompat.startForegroundService(context, intent)
}