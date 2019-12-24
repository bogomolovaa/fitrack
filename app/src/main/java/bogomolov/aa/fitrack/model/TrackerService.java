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
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEventListener;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.navigation.NavDeepLinkBuilder;

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
import java.util.List;

import javax.inject.Inject;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.view.activities.MainActivity;
import bogomolov.aa.fitrack.view.fragments.SettingsFragment;
import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;


public class TrackerService extends Service
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, HasAndroidInjector {
    private GoogleApiClient googleApiClient;
    private Location location;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    public static boolean working;
    public static boolean updating = false;
    private long startLocationUpdateTime;


    @Inject
    DbProvider dbProvider;

    @Inject
    DispatchingAndroidInjector<Object> androidInjector;
    @Override
    public AndroidInjector<Object> androidInjector() {
        return androidInjector;
    }


    private static final double MAX_LOCATION_ACCURACY = 50;
    private static final double MIN_TRACK_DISTANCE = 150;
    public static final String START_SERVICE_ACTION = "start";
    public static final String STOP_SERVICE_ACTION = "stop";
    private static final long UPDATE_INTERVAL = 1000, FASTEST_INTERVAL = 1000;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
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
        PendingIntent resultPendingIntent = new NavDeepLinkBuilder(this).setComponentName(MainActivity.class)
                     .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.settingsFragment)
                .createPendingIntent();


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


    }


    public static void startTrackerService(String action, Context context) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putBoolean(SettingsFragment.KEY_TRACKING, action.equals(START_SERVICE_ACTION));
        prefs.apply();
        Intent intent = new Intent(context, TrackerService.class);
        intent.setAction(action);

        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            ContextCompat.startForegroundService(context, intent);
        }
         */

        ContextCompat.startForegroundService(context, intent);
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

    private void stopServiceAndStartActivityRecognition() {
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