package bogomolov.aa.fitrack.model;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

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

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.model.kalman.KalmanFilter;
import bogomolov.aa.fitrack.model.kalman.KalmanUtils;
import io.realm.Realm;


public class TrackerService extends Service
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private GoogleApiClient googleApiClient;
    private Location location;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private DbProvider dbProvider;
    private KalmanFilter kalmanFilter;
    private long lastTime;
    private double[] lastLatLng;

    public static StringBuffer stringBuffer = new StringBuffer();

    public static final int STARTED_MODE = 1;
    public static final int ENDED_MODE = 2;

    private static final long UPDATE_INTERVAL = 2000, FASTEST_INTERVAL = 2000;
    private static final String TRACKER_SERVICE = "TrackerService";

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "channel_1";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_MAX);
            // Configure the notification channel.
            notificationChannel.setDescription("Sample Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(false);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Tutorialspoint")
                .setContentTitle("sample notification")
                .setContentText("This is sample notification")
                .setContentInfo("Information");
        startForeground(1, notificationBuilder.build());

        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();

        dbProvider = new DbProvider(true);

        Log.i(TRACKER_SERVICE, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TRACKER_SERVICE, "onStartCommand");

        if (googleApiClient != null) googleApiClient.connect();

        return START_STICKY;
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
                   /*
                if (location != null) {
                    Point point = new Point(location.getTime(), location.getLatitude(), location.getLongitude());
                    dbProvider.addPoint(point);
                    stringBuffer.append("point lat "+point.getLat()+" lng "+point.getLng()+"\n");
                    checkTrack();
                    lastTime = location.getTime();
                }
                */
            }
        });
        startLocationUpdates();
    }

    private void startLocationUpdates() {
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
                    Point point = new Point(location.getTime(), location.getLatitude(), location.getLongitude());

                    checkTrack(point);
                    lastTime = location.getTime();
                } else {
                    //stringBuffer.append("same location\n");
                }
            }
        };
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void checkTrack(Point point) {

        List<Point> trackPoints = null;
        Track openedTrack = dbProvider.getOpenedTrack();
        int mode = 0;
        if (openedTrack == null) {
            dbProvider.addPoint(point);
            List<Point> points = dbProvider.getLastPoints();
            if (points.size() > 1) {
                Point firstPoint = points.get(0);
                Point lastPoint = points.get(points.size() - 1);
                double distance = GeoUtils.distance(firstPoint, lastPoint);
                stringBuffer.append("distance " + distance + "\n");
                if (distance > 50) {
                    stringBuffer.append("track created\n");
                    Track track = new Track();
                    mode = STARTED_MODE;
                    track.setStartPoint(lastPoint);
                    track.setStartTime(lastPoint.getTime());
                    openedTrack = dbProvider.addTrack(track);
                }
            }
            trackPoints = points;
        } else {
            List<Point> points = new ArrayList<>(dbProvider.getTrackPoints(openedTrack, Point.RAW));
            trackPoints = points;
            Point lastPoint = points.get(points.size() - 1);
            if (!(GeoUtils.distance(point, lastPoint) > 200 || point.getTime() - lastPoint.getTime() <= 2 * UPDATE_INTERVAL)) {
                points.add(point);
                lastPoint = point;
                dbProvider.addPoint(point);
            }

            for (int i = points.size() - 1; i >= 0; i--) {
                //stringBuffer.append(i + " distance " + GeoUtils.distance(lastPoint, points.get(i)) + " time " + (lastPoint.getTime() - points.get(i).getTime()) / 1000 + "\n");
                if (GeoUtils.distance(lastPoint, points.get(i)) <= 50) {
                    if (lastPoint.getTime() - points.get(i).getTime() > 3 * 60 * 1000) {
                        dbProvider.getRealm().beginTransaction();
                        openedTrack.setEndPoint(points.get(i));
                        openedTrack.setEndTime(points.get(i).getTime());
                        mode = ENDED_MODE;
                        dbProvider.getRealm().commitTransaction();
                        stringBuffer.append("track finished\n");
                    }
                }
            }
        }
        if (openedTrack != null) applyKalmanFilter(openedTrack, trackPoints, mode);
    }

    private void applyKalmanFilter(Track openedTrack, List<Point> trackPoints, int mode) {
        Point point = trackPoints.get(trackPoints.size() - 1);
        if (kalmanFilter == null) {
            lastLatLng = new double[2];
            lastTime = point.getTime();
            kalmanFilter = KalmanUtils.alloc_filter_velocity2d(1000);
            for (int i = 0; i < trackPoints.size() - 1; i++) {
                KalmanUtils.update_velocity2d(kalmanFilter, trackPoints.get(i).getLat(), trackPoints.get(i).getLng(), i == 0 ? 0 : (trackPoints.get(i).getTime() - trackPoints.get(i - 1).getTime()) / 1000.0);
                KalmanUtils.get_lat_long(kalmanFilter, lastLatLng);
                lastTime = trackPoints.get(i).getTime();
            }
        }
        double secondsSinceLastPoint = (point.getTime() - lastTime) / 1000.0;
        KalmanUtils.update_velocity2d(kalmanFilter, point.getLat(), point.getLng(), secondsSinceLastPoint);
        double[] latLonOut = new double[2];
        KalmanUtils.get_lat_long(kalmanFilter, latLonOut);
        Point smoothedPoint = new Point(latLonOut);
        smoothedPoint.setTime(point.getTime());
        smoothedPoint.setSmoothed(Point.SMOOTHED);
        smoothedPoint = dbProvider.addPoint(smoothedPoint);
        dbProvider.getRealm().beginTransaction();
        if (mode == STARTED_MODE)
            openedTrack.setStartSmoothedPoint(smoothedPoint);
        if (mode == ENDED_MODE)
            openedTrack.setEndSmoothedPoint(smoothedPoint);
        if (secondsSinceLastPoint > 0) {
            double distanceSinceLastPoint = GeoUtils.distance(new Point(lastLatLng), new Point(latLonOut));
            double velocity = distanceSinceLastPoint / secondsSinceLastPoint;
            double bearing = KalmanUtils.get_bearing(kalmanFilter);
            openedTrack.setCurrentSpeed(velocity);
            openedTrack.setBearing(bearing);
            openedTrack.addDistance(distanceSinceLastPoint);
        }
        dbProvider.getRealm().commitTransaction();
        lastLatLng = latLonOut;
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.i(TRACKER_SERVICE, "Latitude : " + location.getLatitude() + "\nLongitude : " + location.getLongitude());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        dbProvider.close();
    }

    public void stopLocationUpdates() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
            googleApiClient.disconnect();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}