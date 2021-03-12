package bogomolov.aa.fitrack.features.settings


import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import bogomolov.aa.fitrack.R
import bogomolov.aa.fitrack.features.main.START_SERVICE_ACTION
import bogomolov.aa.fitrack.features.main.STOP_SERVICE_ACTION
import bogomolov.aa.fitrack.features.main.startTrackerService


class SettingsFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_settings)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        val navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
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
        val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        preferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == KEY_TRACKING) {
            val isTracking = sharedPreferences.getBoolean(key, true)
            val action = if (isTracking) START_SERVICE_ACTION else STOP_SERVICE_ACTION
            startTrackerService(action, requireContext())
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
