package bogomolov.aa.fitrack.android;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.preference.PreferenceManager;

import java.util.List;

public class Workarounds {
    private static String AUTOSTART_REQUESTED = "autostart_requested";
    private static String[] AUTOSTART_MANUFACTURES = {"xiaomi", "oppo", "vivo", "Letv", "Honor"};


    public static boolean isAutostartRequested(Context context) {
        boolean contains = false;
        for (String name : AUTOSTART_MANUFACTURES)
            if (name.equals(android.os.Build.MANUFACTURER.toLowerCase())) {
                contains = true;
                break;
            }
        if (!contains) return true;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(AUTOSTART_REQUESTED, false);
    }

    public static void autostart(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean requested = preferences.getBoolean(AUTOSTART_REQUESTED, false);
        if (!requested) {
            String manufacturer = android.os.Build.MANUFACTURER;
            try {
                Intent intent = new Intent();
                if ("xiaomi".equalsIgnoreCase(manufacturer)) {
                    intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
                } else if ("oppo".equalsIgnoreCase(manufacturer)) {
                    intent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
                } else if ("vivo".equalsIgnoreCase(manufacturer)) {
                    intent.setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
                } else if ("Letv".equalsIgnoreCase(manufacturer)) {
                    intent.setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"));
                } else if ("Honor".equalsIgnoreCase(manufacturer)) {
                    intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
                }


                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(AUTOSTART_REQUESTED, true);
                editor.apply();

                List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                if (list.size() > 0) {
                    context.startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
