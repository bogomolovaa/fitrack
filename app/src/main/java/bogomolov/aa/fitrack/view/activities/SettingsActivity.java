package bogomolov.aa.fitrack.view.activities;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.Calendar;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.model.TrackerService;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String KEY_TRACKING = "tracking";
    public static final String KEY_TRACKING_TIME_ENABLED = "trackingTimeEnabled";
    public static final String KEY_START_TRACKING_TIME = "startTrackingTime";
    public static final String KEY_END_TRACKING_TIME = "endTrackingTime";

    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        settingsFragment = new SettingsFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, settingsFragment)
                .commit();

        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_tracks);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        setTimePreference(KEY_START_TRACKING_TIME);
        setTimePreference(KEY_END_TRACKING_TIME);
    }

    private void setTimePreference(final String KEY){
        final Preference trackingTimePreference = settingsFragment.findPreference(KEY);
        trackingTimePreference.setSummary(trackingTimePreference.getSharedPreferences().getString(KEY, "00:00"));
        trackingTimePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public boolean onPreferenceClick(Preference preference) {
                int[] hm = parseHoursMinutes(trackingTimePreference.getSharedPreferences().getString(KEY, "00:00"));
                new TimePickerDialog(SettingsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
                        trackingTimePreference.setSummary(hoursMinutesToString(hours,minutes));
                        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).edit();
                        prefs.putString(KEY, hoursMinutesToString(hours,minutes));
                        prefs.apply();

                        //PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, trackingServiceIntent(START_SERVICE_ACTION), 0);
                        //AlarmManager alarmManager = (AlarmManager) SettingsActivity.this.getSystemService(ALARM_SERVICE);
                        //alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 10000, 5000, pendingIntent);
                    }
                }, hm[0], hm[1], true).show();
                return false;
            }
        });
    }

    private int[] parseHoursMinutes(String value) {
        String[] strArray = value.split(":");
        return new int[]{Integer.parseInt(strArray[0]), Integer.parseInt(strArray[1])};
    }

    private String hoursMinutesToString(int hours, int minutes) {
        return String.format("%02d", hours) + ":" + String.format("%02d", minutes);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_TRACKING)) {
            boolean isTracking = sharedPreferences.getBoolean(key, true);
            Intent intent = new Intent(this, TrackerService.class);
            if (isTracking) {
                intent.setAction(TrackerService.START_SERVICE_ACTION);
            } else {
                intent.setAction(TrackerService.STOP_SERVICE_ACTION);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                ContextCompat.startForegroundService(this, intent);
            }
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }
}