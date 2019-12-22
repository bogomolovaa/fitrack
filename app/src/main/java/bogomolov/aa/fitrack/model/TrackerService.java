package bogomolov.aa.fitrack.model;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.view.activities.SettingsActivity;


public class TrackerService extends Service
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private GoogleApiClient googleApiClient;
    private Location location;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private DbProvider dbProvider;
    public static boolean working;
    public static boolean updating = false;
    private TriggerEventListener wakeUpListener;
    private SensorManager sensorManager;
    private Sensor sMotionSensor;
    private Sensor accelerometerSensor;
    private long startLocationUpdateTime;
    private SensorEventListener accelerometerListener;


    private static final double MAX_LOCATION_ACCURACY = 50;
    private static final double WAKEUP_MOTION_A = 0.1;
    private static final double G = 9.8;
    private static final double MIN_TRACK_DISTANCE = 150;
    public static final String START_SERVICE_ACTION = "start";
    public static final String STOP_SERVICE_ACTION = "stop";
    private static final long UPDATE_INTERVAL = 1000, FASTEST_INTERVAL = 1000;

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "channel_1";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setDescription("Fitrack");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(false);
            notificationChannel.setImportance(NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        Intent resultIntent = new Intent(this, SettingsActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getResources().getString(R.string.notification_title))
                .setContentText(getResources().getString(R.string.notification_text))
                .setVibrate(new long[]{0L})
                .setContentIntent(resultPendingIntent);
        startForeground(1, notificationBuilder.build());

        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();

        dbProvider = new DbProvider(false);


        /*
        wakeUpListener = new TriggerEventListener() {
            @Override
            public void onTrigger(TriggerEvent triggerEvent) {
                Log.i("test", "WAKE UP EVENT");
                startLocationUpdates();
                updating = true;
            }
        };

        accelerometerListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];
                double mod = Math.sqrt(x * x + y * y + z * z);
                Log.i("test", "mod " + mod + " (" + x + "," + y + "," + z + ") " + new Date());
                if (mod / G > WAKEUP_MOTION_A) {
                    Log.i("test", "WAKE UP EVENT");
                    startLocationUpdates();
                    updating = true;
                    sensorManager.unregisterListener(accelerometerListener);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sMotionSensor = sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);


        pauseTracking();

         */
    }

    private void chargeWakeUpTrigger() {
        //boolean requested = sensorManager.requestTriggerSensor(wakeUpListener, sMotionSensor);
        boolean requested = sensorManager.registerListener(accelerometerListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        Log.i("test", "chargeWakeUpTrigger requested " + requested);
    }


    private void pauseTracking() {
        Log.i("test", "PAUSE TRACKING");
        stopLocationUpdates();
        chargeWakeUpTrigger();
        updating = false;
    }

    public static void startTrackerService(String action, Context context) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putBoolean(SettingsActivity.KEY_TRACKING, action.equals(START_SERVICE_ACTION));
        prefs.apply();
        Intent intent = new Intent(context, TrackerService.class);
        intent.setAction(action);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            ContextCompat.startForegroundService(context, intent);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(START_SERVICE_ACTION)) {
            if (googleApiClient != null) googleApiClient.connect();
            working = true;
            return START_STICKY;
        } else if (intent.getAction().equals(STOP_SERVICE_ACTION)) {
            stopTrackingService();
        }
        return START_STICKY;
    }

    private void stopTrackingService() {
        stopForeground(true);
        stopSelf();
        working = false;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Permissions ok, we get last location
        LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location theLocation) {
                location = theLocation;
            }
        });
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        startLocationUpdateTime = System.currentTimeMillis();
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You need to enable permissions to display location !", Toast.LENGTH_SHORT).show();
        }

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (!(locationResult.getLastLocation().getLongitude() == location.getLongitude() && locationResult.getLastLocation().getLatitude() == location.getLatitude())) {
                    location = locationResult.getLastLocation();
                    if (location.getAccuracy() < MAX_LOCATION_ACCURACY) {
                        Point point = new Point(location.getTime(), location.getLatitude(), location.getLongitude());
                        checkTrack(point);
                    }
                }
            }
        };
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void checkTrack(Point point) {
        List<Point> trackPoints = null;
        Track openedTrack = dbProvider.getOpenedTrack();
        if (openedTrack == null) {
            dbProvider.addPoint(point);
            List<Point> points = dbProvider.getLastPoints();
            if (points.size() > 1) {
                Point firstPoint = points.get(0);
                for (int i = 0; i < points.size() - 1; i++)
                    if (System.currentTimeMillis() - points.get(i).getTime() < 3 * 60 * 1000) {
                        firstPoint = points.get(i);
                        break;
                    }
                Point lastPoint = points.get(points.size() - 1);
                double distance = Point.distance(firstPoint, lastPoint);
                if (distance > 50) {
                    Track track = new Track();
                    track.setStartPoint(lastPoint);
                    track.setStartTime(lastPoint.getTime());
                    openedTrack = dbProvider.addTrack(track);
                } else if (System.currentTimeMillis() - startLocationUpdateTime > 5 * 60 * 1000) {
                    stopServiceAndStartActivityRecognition();
                }
            }
            trackPoints = points;
        } else {
            List<Point> points = new ArrayList<>(dbProvider.getTrackPoints(openedTrack, Point.RAW));
            trackPoints = points;
            Point lastPoint = points.get(points.size() - 1);
            if (!(Point.distance(point, lastPoint) > 200 || System.currentTimeMillis() - lastPoint.getTime() <= 2 * UPDATE_INTERVAL)) {
                points.add(point);
                lastPoint = dbProvider.addPoint(point);
            }
            for (int i = points.size() - 1; i >= 0; i--) {
                if (Point.distance(lastPoint, points.get(i)) <= 50) {
                    if (lastPoint.getTime() - points.get(i).getTime() > 3 * 60 * 1000) {
                        trackPoints = trackPoints.subList(0, i + 1);
                        trackPoints.add(lastPoint);
                        finishTrack(trackPoints, openedTrack, points.get(i).getTime());
                        //pauseTracking();
                        stopServiceAndStartActivityRecognition();
                        break;
                    }
                }
            }
        }
    }

    private void stopServiceAndStartActivityRecognition(){
        stopTrackingService();
        TrackingScheduler.startActivityRecognition(this);
    }

    private void finishTrack(List<Point> points, Track openedTrack, long time) {
        finishTrack(dbProvider, points, openedTrack, time);
    }

    public static void finishTrack(DbProvider dbProvider, List<Point> points, Track openedTrack, long time) {
        if (points.size() == 0) return;
        Point lastPoint = points.get(points.size() - 1);
        List<Point> smoothedPoints = RamerDouglasPeucker.douglasPeucker(points, Track.EPSILON);
        List<Point> smoothedPointsManaged = new ArrayList<>();
        for (Point point : smoothedPoints) {
            Point smoothedPoint = point.clone();
            smoothedPoint.setSmoothed(Point.SMOOTHED);
            smoothedPointsManaged.add(dbProvider.addPoint(smoothedPoint));
        }
        dbProvider.getRealm().beginTransaction();
        openedTrack.setEndPoint(lastPoint);
        openedTrack.setEndTime(time);
        openedTrack.setStartSmoothedPoint(smoothedPointsManaged.get(0));
        openedTrack.setEndSmoothedPoint(smoothedPointsManaged.get(smoothedPointsManaged.size() - 1));
        openedTrack.setDistance(Point.getTrackDistance(smoothedPointsManaged));
        dbProvider.getRealm().commitTransaction();
        dbProvider.deleteRawPoints(openedTrack);

        if (openedTrack.getDistance() < MIN_TRACK_DISTANCE) {
            dbProvider.deleteTrack(openedTrack.getId());
            dbProvider.deleteLastPoints();
        }
    }


    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Log.i("test", "cancelTriggerSensor");
        //sensorManager.cancelTriggerSensor(wakeUpListener, sMotionSensor);
        //sensorManager.unregisterListener(accelerometerListener);
        stopLocationUpdates();
        dbProvider.close();
    }

    public void stopLocationUpdates() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            if (locationCallback != null) {
                LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
                googleApiClient.disconnect();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}