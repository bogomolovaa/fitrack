package bogomolov.aa.fitrack.view.fragments


import android.content.SharedPreferences
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import bogomolov.aa.fitrack.R
import bogomolov.aa.fitrack.android.TrackerService


class SettingsFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_settings)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        val navController = Navigation.findNavController(activity!!, R.id.nav_host_fragment)
        NavigationUI.setupWithNavController(toolbar, navController)

        val settingsFragment = SettingsFragmentView()
        childFragmentManager
                .beginTransaction()
                .replace(R.id.settings, settingsFragment)
                .commit()

        return view
    }

    override fun onResume() {
        super.onResume()
        val preferences = PreferenceManager.getDefaultSharedPreferences(context!!)
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        val preferences = PreferenceManager.getDefaultSharedPreferences(context!!)
        preferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == KEY_TRACKING) {
            val isTracking = sharedPreferences.getBoolean(key, true)
            val action = if (isTracking) TrackerService.START_SERVICE_ACTION else TrackerService.STOP_SERVICE_ACTION
            TrackerService.startTrackerService(action, context!!)
        }
    }

    class SettingsFragmentView : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle, rootKey: String) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }

    companion object {
        const val KEY_TRACKING = "tracking"
    }

}
