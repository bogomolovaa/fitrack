package bogomolov.aa.fitrack.view.fragments;


import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.model.TrackerService;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    public static final String KEY_TRACKING = "tracking";
    private SettingsFragmentView settingsFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        /*
        Toolbar toolbar = view.findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        */

        settingsFragment = new SettingsFragmentView();
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, settingsFragment)
                .commit();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_TRACKING)) {
            boolean isTracking = sharedPreferences.getBoolean(key, true);
            if (isTracking) {
                TrackerService.startTrackerService(TrackerService.START_SERVICE_ACTION, getContext());
            } else {
                TrackerService.startTrackerService(TrackerService.STOP_SERVICE_ACTION, getContext());
            }
        }
    }

    private static class SettingsFragmentView extends PreferenceFragmentCompat{
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }

}
