package bogomolov.aa.fitrack.view;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.navigation.NavigationView;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.model.DbProvider;
import bogomolov.aa.fitrack.model.GeoUtils;
import bogomolov.aa.fitrack.model.Point;
import bogomolov.aa.fitrack.model.RamerDouglasPeucker;
import bogomolov.aa.fitrack.model.Track;
import bogomolov.aa.fitrack.model.TrackerService;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {
    private TextView textDistance;
    private TextView textTime;
    private TextView textSpeed;
    private TextView textAvgSpeed;
    private TextView textDebug;
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    private DbProvider dbProvider;
    private GoogleMap googleMap;
    private Polyline trackRawPolyline;
    private Polyline trackSmoothedPolyline;
    private Marker currentPositionMarker;
    private Menu startStopMenu;

    private List<Point> tailSmoothedPoints;
    private int windowStartId;
    private static final int WINDOW_MAX_SIZE = 50;
    private static final int EPSILON = 20;

    private static final int ALL_PERMISSIONS_RESULT = 1011;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String MAIN_ACTIVITY = "TrackerService";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.nav_open_drawer,
                R.string.nav_close_drawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        textDistance = findViewById(R.id.text_distance);
        textTime = findViewById(R.id.text_time);
        textSpeed = findViewById(R.id.text_speed);
        textAvgSpeed = findViewById(R.id.text_avg_speed);
        textDebug = findViewById(R.id.text_debug);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionsToRequest = permissionsToRequest(permissions);


        if (!checkPlayServices()) finish();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0) {
                requestPermissions(permissionsToRequest.toArray(
                        new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
            } else {
                startTrackerService();
            }
        } else {
            startTrackerService();
        }

        dbProvider = new DbProvider(false);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.start_stop, menu);
        startStopMenu = menu;
        Track lastTrack = dbProvider.getLastTrack();
        if (lastTrack != null) {
            startStopMenu.findItem(R.id.menu_track_start).setVisible(!lastTrack.isOpened());
            startStopMenu.findItem(R.id.menu_track_stop).setVisible(lastTrack.isOpened());
        } else {
            startStopMenu.findItem(R.id.menu_track_start).setVisible(true);
            startStopMenu.findItem(R.id.menu_track_stop).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_track_start:
                startTrack();
                break;
            case R.id.menu_track_stop:
                stopTrack();
                break;
            default:
                break;
        }
        return true;
    }

    private void startTrack() {
        Point lastPoint = dbProvider.getLastPoint();
        if (lastPoint != null) {
            Track track = new Track();
            track.setStartPoint(lastPoint);
            track.setStartTime(lastPoint.getTime());
            dbProvider.addTrack(track);
            startStopMenu.findItem(R.id.menu_track_start).setVisible(false);
            startStopMenu.findItem(R.id.menu_track_stop).setVisible(true);
        }
    }

    private void stopTrack() {
        Track openedTrack = dbProvider.getLastTrack();
        if (openedTrack != null) {
            Point lastPoint = dbProvider.getLastPoint();
            if (lastPoint != null) {
                dbProvider.getRealm().beginTransaction();
                openedTrack.setEndPoint(lastPoint);
                openedTrack.setEndTime(lastPoint.getTime());
                dbProvider.getRealm().commitTransaction();
                startStopMenu.findItem(R.id.menu_track_start).setVisible(true);
                startStopMenu.findItem(R.id.menu_track_stop).setVisible(false);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Intent intent = null;
        switch (menuItem.getItemId()) {
            case R.id.menu_tracks:
                intent = new Intent(this, ListActivity.class);
                break;
            case R.id.menu_stats:
                intent = new Intent(this, StatsActivity.class);
                break;
            case R.id.menu_settings:
                intent = new Intent(this, SettingsActivity.class);
                break;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
        startActivity(intent);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void startTrackerService() {
        Log.i(MAIN_ACTIVITY, "startTrackerService");

        Intent notificationIntent = new Intent(this, TrackerService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(notificationIntent);
        } else {
            ContextCompat.startForegroundService(this, notificationIntent);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();


        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Track track = dbProvider.getLastTrack();
                Point point = dbProvider.getLastPoint();

                if (googleMap != null && point != null) {
                    LatLng latLng = new LatLng(point.getLat(), point.getLng());
                    if (currentPositionMarker == null) {
                        currentPositionMarker = googleMap.addMarker(new MarkerOptions().position(latLng).title("Current Location"));
                    } else {
                        currentPositionMarker.setPosition(latLng);
                    }

                    if (track != null) {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                        if (startStopMenu != null) {
                            startStopMenu.findItem(R.id.menu_track_start).setVisible(!track.isOpened());
                            startStopMenu.findItem(R.id.menu_track_stop).setVisible(track.isOpened());
                        }

                        if (trackRawPolyline == null) {
                            trackRawPolyline = googleMap.addPolyline((new PolylineOptions())
                                    .clickable(false).add(pointsToPolylineCoordinates(dbProvider.getTrackPoints(track, Point.RAW))));
                        } else {
                            trackRawPolyline.setPoints(Arrays.asList(pointsToPolylineCoordinates(dbProvider.getTrackPoints(track, Point.RAW))));
                        }

                        List<Point> points = dbProvider.getTrackPoints(track, Point.RAW);

                        //List<Point> smoothedPoints = RamerDouglasPeucker.douglasPeucker(points, EPSILON);
                        List<Point> smoothedPoints = getSmoothedPoints(points);

                        if (trackSmoothedPolyline == null) {
                            trackSmoothedPolyline = googleMap.addPolyline((new PolylineOptions()).color(0xffffff00)
                                    .clickable(false).add(pointsToPolylineCoordinates(smoothedPoints)));
                        } else {
                            trackSmoothedPolyline.setPoints(Arrays.asList(pointsToPolylineCoordinates(smoothedPoints)));
                        }
                        dbProvider.getRealm().beginTransaction();
                        track.setCurrentSpeed(getCurrentSpeed(smoothedPoints));
                        track.setDistance(GeoUtils.getTrackDistance(smoothedPoints));
                        dbProvider.getRealm().commitTransaction();

                    } else {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
                        if (trackRawPolyline != null) trackRawPolyline.remove();
                    }

                }

                if (track != null) {
                    textDistance.setText((int) track.getDistance() + " m");
                    textTime.setText(track.getTimeString());
                    textSpeed.setText(String.format("%.1f", 3.6 * track.getCurrentSpeed()) + " km/h");
                    textAvgSpeed.setText(String.format("%.1f", 3.6 * track.getSpeed()) + " km/h");
                } else {
                    textDistance.setText("");
                    textTime.setText("");
                    textSpeed.setText("");
                    textAvgSpeed.setText("");
                }
                String[] lines = TrackerService.stringBuffer.toString().split("\n");
                StringBuilder sb = new StringBuilder();
                for (int i = lines.length - 1; i >= Math.max(lines.length - 30, 0); i--)
                    sb.append(lines[i] + "\n");
                textDebug.setText(sb.toString());


                handler.postDelayed(this, 4000);
            }
        };
        runnable.run();
    }

    private double getCurrentSpeed(List<Point> points) {
        double distance = points.size() > 1 ? GeoUtils.distance(points.get(points.size() - 1), points.get(points.size() - 2)) : 0;
        double seconds = points.size() > 1 ? (points.get(points.size() - 1).getTime() - points.get(points.size() - 2).getTime()) / 1000.0 : 0;
        return seconds > 0 ? distance / seconds : 0;
    }

    private List<Point> getSmoothedPoints(List<Point> points) {
        if (points.size() < 3) {
            tailSmoothedPoints = null;
            return points;
        }
        List<Point> smoothedPoints = new ArrayList<>();
        if (tailSmoothedPoints == null) {
            tailSmoothedPoints = new ArrayList<>();
            int windowSize = WINDOW_MAX_SIZE / 2;
            windowStartId = Math.max(points.size() - windowSize, 0);
            List<Point> windowPointsRaw = getWindowPoints(points, windowSize);
            List<Point> preWindowPointsRaw = getPreWindowPoints(points, windowSize);
            tailSmoothedPoints = RamerDouglasPeucker.douglasPeucker(preWindowPointsRaw, EPSILON);
            List<Point> windowSmoothedPoints = RamerDouglasPeucker.douglasPeucker(windowPointsRaw, EPSILON);
            smoothedPoints.addAll(tailSmoothedPoints);
            smoothedPoints.addAll(windowSmoothedPoints);
        } else {
            int windowSize = points.size() - windowStartId;
            if (windowSize >= WINDOW_MAX_SIZE) {
                List<Point> windowPointsRaw = getWindowPoints(points, windowSize);
                List<Point> secondHalfWindowPointsRaw = getWindowPoints(points, windowSize / 2);
                List<Point> firstHalfWindowPointsRaw = getPreWindowPoints(windowPointsRaw, windowSize / 2);
                List<Point> secondHalfWindowSmoothedPoints = RamerDouglasPeucker.douglasPeucker(secondHalfWindowPointsRaw, EPSILON);
                List<Point> firstHalfWindowSmoothedPoints = RamerDouglasPeucker.douglasPeucker(firstHalfWindowPointsRaw, EPSILON);
                tailSmoothedPoints.addAll(firstHalfWindowSmoothedPoints);
                smoothedPoints.addAll(tailSmoothedPoints);
                smoothedPoints.addAll(secondHalfWindowSmoothedPoints);
                windowStartId = points.size() - windowSize / 2;
            } else {
                List<Point> windowPointsRaw = getWindowPoints(points, windowSize);
                List<Point> windowSmoothedPoints = RamerDouglasPeucker.douglasPeucker(windowPointsRaw, EPSILON);
                smoothedPoints.addAll(tailSmoothedPoints);
                smoothedPoints.addAll(windowSmoothedPoints);
            }
        }
        return smoothedPoints;
    }

    private List<Point> getWindowPoints(List<Point> points, int windowSize) {
        return points.size() > windowSize ? points.subList(points.size() - windowSize, points.size()) : points;
    }

    private List<Point> getPreWindowPoints(List<Point> points, int windowSize) {
        return points.size() > windowSize ? points.subList(0, points.size() - windowSize) : new ArrayList<Point>();
    }

    private LatLng[] pointsToPolylineCoordinates(List<Point> points) {
        LatLng[] latLngs = new LatLng[points.size()];
        for (int i = 0; i < points.size(); i++)
            latLngs[i] = new LatLng(points.get(i).getLat(), points.get(i).getLng());
        return latLngs;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbProvider.close();
    }

    private ArrayList<String> permissionsToRequest(ArrayList<String> wantedPermissions) {
        ArrayList<String> result = new ArrayList<>();
        for (String perm : wantedPermissions) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }
        return result;
    }


    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                finish();
            }

            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSIONS_RESULT:
                for (String perm : permissionsToRequest) {
                    if (!hasPermission(perm)) {
                        permissionsRejected.add(perm);
                    }
                }

                if (permissionsRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            new AlertDialog.Builder(MainActivity.this).
                                    setMessage("These permissions are mandatory to get your location. You need to allow them.").
                                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.
                                                        toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    }).setNegativeButton("Cancel", null).create().show();

                            return;
                        }
                    }
                } else {
                    startTrackerService();
                }

                break;
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;


    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


}
