package bogomolov.aa.fitrack.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        TrackingScheduler.schedule(context);
    }
}
