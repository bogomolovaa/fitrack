package bogomolov.aa.fitrack.view.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
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
import androidx.preference.PreferenceManager;

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

import javax.inject.Inject;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.dagger.AppComponent;
import bogomolov.aa.fitrack.dagger.AppModule;
import bogomolov.aa.fitrack.dagger.DaggerAppComponent;
import bogomolov.aa.fitrack.model.Point;
import bogomolov.aa.fitrack.model.Track;
import bogomolov.aa.fitrack.model.TrackerService;
import bogomolov.aa.fitrack.model.TrackingScheduler;
import bogomolov.aa.fitrack.presenter.MainPresenter;
import bogomolov.aa.fitrack.view.MainView;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener, MainView {
    private TextView textDistance;
    private TextView textTime;
    private TextView textSpeed;
    private TextView textAvgSpeed;
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    private GoogleMap googleMap;
    private Polyline trackRawPolyline;
    private Polyline trackSmoothedPolyline;
    private Marker currentPositionMarker;
    private Menu startStopMenu;
    private Handler handler;
    private Runnable runnable;

    @Inject
    MainPresenter mainPresenter;


    private static final int ALL_PERMISSIONS_RESULT = 1011;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;


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

        AppComponent appComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();
        appComponent.injectsMainActivity(this);

        //TrackingScheduler.schedule(this);

    }

    private void startTrackerService() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean(SettingsActivity.KEY_TRACKING, true))
            TrackerService.startTrackerService(TrackerService.START_SERVICE_ACTION, this);
    }

    @Override
    public void showStartStopButtons(boolean canStart) {
        if (startStopMenu != null) {
            startStopMenu.findItem(R.id.menu_track_start).setVisible(canStart);
            startStopMenu.findItem(R.id.menu_track_stop).setVisible(!canStart);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.start_stop, menu);
        startStopMenu = menu;
        mainPresenter.onStartStopButtonsCreated();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_track_start:
                mainPresenter.startTrack();
                break;
            case R.id.menu_track_stop:
                mainPresenter.stopTrack();
                break;
            default:
                break;
        }
        return true;
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Intent intent = null;
        switch (menuItem.getItemId()) {
            case R.id.menu_tracks:
                intent = new Intent(this, TracksListActivity.class);
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


    public void updateView(Track track, Point point, List<Point> rawTrackPoints, List<Point> smoothedPoints) {
        if (googleMap != null) {
            if (point != null) {
                LatLng latLng = new LatLng(point.getLat(), point.getLng());
                if (currentPositionMarker == null) {
                    currentPositionMarker = googleMap.addMarker(new MarkerOptions().position(latLng).title("Current Location"));
                } else {
                    currentPositionMarker.setPosition(latLng);
                }
                if (track == null || !track.isOpened()) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
                } else {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            }

            if (track != null) showStartStopButtons(!track.isOpened());
            if (track != null && track.isOpened()) {
                if (trackRawPolyline == null) {
                    trackRawPolyline = googleMap.addPolyline((new PolylineOptions())
                            .clickable(false).add(Point.toPolylineCoordinates(rawTrackPoints)));
                } else {
                    trackRawPolyline.setPoints(Arrays.asList(Point.toPolylineCoordinates(rawTrackPoints)));
                }

                if (trackSmoothedPolyline == null) {
                    trackSmoothedPolyline = googleMap.addPolyline((new PolylineOptions()).color(0xffffff00)
                            .clickable(false).add(Point.toPolylineCoordinates(smoothedPoints)));
                } else {
                    trackSmoothedPolyline.setPoints(Arrays.asList(Point.toPolylineCoordinates(smoothedPoints)));
                }
            } else {
                if (trackRawPolyline != null) {
                    trackRawPolyline.remove();
                    trackRawPolyline = null;
                }
                if (trackSmoothedPolyline != null) {
                    trackSmoothedPolyline.remove();
                    trackSmoothedPolyline = null;
                }
            }
        }

        if (track != null && track.isOpened()) {
            textDistance.setText((int) track.getCurrentDistance() + " m");
            textTime.setText(track.getTimeString());
            textSpeed.setText(String.format("%.1f", 3.6 * track.getCurrentSpeed()) + " km/h");
            textAvgSpeed.setText(String.format("%.1f", 3.6 * track.getSpeedForCurrentDistance()) + " km/h");
        } else {
            textDistance.setText("");
            textTime.setText("");
            textSpeed.setText("");
            textAvgSpeed.setText("");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                mainPresenter.onViewUpdate();

                ((TextView) MainActivity.this.findViewById(R.id.text_view_updating)).setText("" + TrackerService.updating);


                handler.postDelayed(this, 1000);
            }
        };
        runnable.run();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        mainPresenter.onDestroy();
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
