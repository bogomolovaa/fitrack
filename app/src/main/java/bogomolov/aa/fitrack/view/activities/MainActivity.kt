package bogomolov.aa.fitrack.view.activities

import android.Manifest
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.navigation.NavigationView

import java.util.ArrayList

import javax.inject.Inject

import bogomolov.aa.fitrack.R
import bogomolov.aa.fitrack.android.TrackerService
import bogomolov.aa.fitrack.android.*
import bogomolov.aa.fitrack.databinding.ActivityMainBinding
import bogomolov.aa.fitrack.view.fragments.SettingsFragment
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector


class MainActivity : AppCompatActivity(), HasAndroidInjector {
    private lateinit var permissionsToRequest: List<String>
    private val permissionsRejected: MutableList<String> = ArrayList()
    private val permissions: MutableList<String> = ArrayList()

    @Inject
    internal lateinit var androidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> = androidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        AppBarConfiguration.Builder(navController.getGraph())
                .setDrawerLayout(binding.drawerLayout)
                .build()

        NavigationUI.setupWithNavController(binding.navView, navController)

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        permissionsToRequest = permissionsToRequest(permissions)


        if (!checkPlayServices()) finish()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size > 0) {
                requestPermissions(permissionsToRequest.toTypedArray(), ALL_PERMISSIONS_RESULT)
            } else {
                startTrackerService()
            }
        } else {
            startTrackerService()
        }


        if (!isAutostartRequested(this)) {
            AlertDialog.Builder(this).setMessage(R.string.need_autostart_string)
                    .setPositiveButton("OK") { d, i -> autostart(this) }
                    .setNegativeButton("DISMISS") { d, i -> d.dismiss() }
                    .show()
        }

        startActivityRecognition(this)


    }

    private fun startTrackerService() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getBoolean(SettingsFragment.KEY_TRACKING, true))
            TrackerService.startTrackerService(TrackerService.START_SERVICE_ACTION, this)
    }

    private fun permissionsToRequest(wantedPermissions: List<String>): List<String> {
        val result = ArrayList<String>()
        for (perm in wantedPermissions) if (!hasPermission(perm)) result.add(perm)
        return result
    }

    private fun hasPermission(permission: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    private fun checkPlayServices(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(this)

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
            } else {
                finish()
            }
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == ALL_PERMISSIONS_RESULT) {
            for (perm in permissionsToRequest) if (!hasPermission(perm)) permissionsRejected.add(perm)
            if (permissionsRejected.size > 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(permissionsRejected[0])) {
                        AlertDialog.Builder(this).setMessage("These permissions are mandatory to get your location. You need to allow them.")
                                .setPositiveButton("OK") { d, i ->
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                        requestPermissions(permissionsRejected.toTypedArray(), ALL_PERMISSIONS_RESULT)
                                }.setNegativeButton("Cancel", null).create().show()
                    }
                }
            } else {
                startTrackerService()
            }
        }
    }

    companion object {
        private val ALL_PERMISSIONS_RESULT = 1011
        private val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
    }

}
