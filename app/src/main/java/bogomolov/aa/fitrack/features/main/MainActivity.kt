package bogomolov.aa.fitrack.features.main

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import bogomolov.aa.fitrack.R
import bogomolov.aa.fitrack.databinding.ActivityMainBinding
import bogomolov.aa.fitrack.features.settings.KEY_AUTOSTART
import bogomolov.aa.fitrack.features.settings.getSetting
import bogomolov.aa.fitrack.features.settings.setSetting
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

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

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        AppBarConfiguration.Builder(navController.graph).setOpenableLayout(binding.drawerLayout)
            .build()
        NavigationUI.setupWithNavController(binding.navView, navController)

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
        permissionsToRequest = permissionsToRequest(permissions)

        if (!checkPlayServices()) finish()



        if (!isAutostartRequested()) {
            AlertDialog.Builder(this).setMessage(R.string.need_autostart_string)
                .setPositiveButton("OK") { _, _ -> autostart() }
                .setNegativeButton("DISMISS") { d, _ -> d.dismiss() }
                .show()
        }

        startWatching()
    }

    private fun startService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.isNotEmpty()) {
                requestPermissions(permissionsToRequest.toTypedArray(), ALL_PERMISSIONS_RESULT)
            } else {
                trackerService(START_SERVICE_ACTION, this)
            }
        } else {
            trackerService(START_SERVICE_ACTION, this)
        }
    }

    override fun onStart() {
        super.onStart()
        startService()
    }


    private fun startWatching() {
        startActivityRecognition(this)
        val workRequest = PeriodicWorkRequestBuilder<IdleWorker>(15, TimeUnit.MINUTES)
            .setInitialDelay(5, TimeUnit.MINUTES).build()
        WorkManager.getInstance(application).enqueueUniquePeriodicWork(
            "IdleWorker",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == ALL_PERMISSIONS_RESULT) {
            for (perm in permissionsToRequest) if (!hasPermission(perm)) permissionsRejected.add(
                perm
            )
            if (permissionsRejected.size > 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(permissionsRejected[0])) {
                        AlertDialog.Builder(this)
                            .setMessage("These permissions are mandatory to get your location. You need to allow them.")
                            .setPositiveButton("OK") { _, _ ->
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                    requestPermissions(
                                        permissionsRejected.toTypedArray(),
                                        ALL_PERMISSIONS_RESULT
                                    )
                            }.setNegativeButton("Cancel", null).create().show()
                    }
                }
            } else {
                trackerService(START_SERVICE_ACTION, this)
            }
        }
    }

    private fun isAutostartRequested(): Boolean {
        if (BAD_MANUFACTURES.find { it == Build.MANUFACTURER.toLowerCase() } == null) return true
        return getSetting(KEY_AUTOSTART, this)
    }

    private fun autostart() {
        val requested = getSetting(KEY_AUTOSTART, this)
        if (!requested) {
            val manufacturer = Build.MANUFACTURER
            try {
                val intent = Intent()
                if ("xiaomi".equals(manufacturer, ignoreCase = true)) {
                    intent.component = ComponentName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"
                    )
                } else if ("oppo".equals(manufacturer, ignoreCase = true)) {
                    intent.component = ComponentName(
                        "com.coloros.safecenter",
                        "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                    )
                } else if ("vivo".equals(manufacturer, ignoreCase = true)) {
                    intent.component = ComponentName(
                        "com.vivo.permissionmanager",
                        "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                    )
                } else if ("Letv".equals(manufacturer, ignoreCase = true)) {
                    intent.component = ComponentName(
                        "com.letv.android.letvsafe",
                        "com.letv.android.letvsafe.AutobootManageActivity"
                    )
                } else if ("Honor".equals(manufacturer, ignoreCase = true)) {
                    intent.component = ComponentName(
                        "com.huawei.systemmanager",
                        "com.huawei.systemmanager.optimize.process.ProtectActivity"
                    )
                }
                setSetting(KEY_AUTOSTART, true, this)
                val list = packageManager.queryIntentActivities(
                    intent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                if (list.size > 0) startActivity(intent)
            } catch (e: Exception) {
                Log.w("MainActivity", "autostart", e)
            }
        }
    }
}

private const val ALL_PERMISSIONS_RESULT = 1011
private const val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
private val BAD_MANUFACTURES = listOf("xiaomi", "oppo", "vivo", "Letv", "Honor")