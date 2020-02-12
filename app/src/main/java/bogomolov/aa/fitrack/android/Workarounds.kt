package bogomolov.aa.fitrack.android

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.edit
import androidx.preference.PreferenceManager

private const val AUTOSTART_REQUESTED = "autostart_requested"
private val AUTOSTART_MANUFACTURES = arrayOf("xiaomi", "oppo", "vivo", "Letv", "Honor")

fun isAutostartRequested(context: Context?): Boolean {
    var contains = false
    for (name in AUTOSTART_MANUFACTURES) if (name == Build.MANUFACTURER.toLowerCase()) {
        contains = true
        break
    }
    if (!contains) return true
    val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    return preferences.getBoolean(AUTOSTART_REQUESTED, false)
}


fun autostart(context: Context) {
    val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    val requested = preferences.getBoolean(AUTOSTART_REQUESTED, false)
    if (!requested) {
        val manufacturer = Build.MANUFACTURER
        try {
            val intent = Intent()
            if ("xiaomi".equals(manufacturer, ignoreCase = true)) {
                intent.component = ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
            } else if ("oppo".equals(manufacturer, ignoreCase = true)) {
                intent.component = ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")
            } else if ("vivo".equals(manufacturer, ignoreCase = true)) {
                intent.component = ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
            } else if ("Letv".equals(manufacturer, ignoreCase = true)) {
                intent.component = ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")
            } else if ("Honor".equals(manufacturer, ignoreCase = true)) {
                intent.component = ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")
            }
            preferences.edit {
                putBoolean(AUTOSTART_REQUESTED, true)
            }
            val list = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            if (list.size > 0) {
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
