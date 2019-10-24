package bogomolov.aa.fitrack.view;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;


import java.util.ArrayList;
import java.util.List;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.model.DbProvider;
import bogomolov.aa.fitrack.model.Point;
import bogomolov.aa.fitrack.model.Track;
import bogomolov.aa.fitrack.model.TrackerService;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {
    private TextView textDistance;
    private TextView textTime;
    private TextView textSpeed;
    private TextView textAvgSpeed;
    private TextView textDebug;
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    private DbProvider dbProvider;

    private static final int ALL_PERMISSIONS_RESULT = 1011;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String MAIN_ACTIVITY = "TrackerService";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textDistance = findViewById(R.id.text_distance);
        textTime = findViewById(R.id.text_time);
        textSpeed = findViewById(R.id.text_speed);
        textAvgSpeed = findViewById(R.id.text_avg_speed);
        textDebug = findViewById(R.id.text_debug);
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

        dbProvider = new DbProvider(false);

        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Track track = dbProvider.getLastTrack();
                if (track != null) {
                    textDistance.setText((int) track.getDistance() + " m");
                    long time = track.isOpened() ? System.currentTimeMillis() : track.getEndTime();
                    textTime.setText(track.getTimeString(time));
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
                for (int i = lines.length - 1; i >= 0; i--) sb.append(lines[i] + "\n");
                textDebug.setText(sb.toString());

                handler.postDelayed(this, 5000);
            }
        };
        runnable.run();
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


}
