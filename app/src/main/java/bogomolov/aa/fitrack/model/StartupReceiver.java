package bogomolov.aa.fitrack.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartupReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        TrackerService.startTrackerService(intent.getAction(), context);
    }
}
