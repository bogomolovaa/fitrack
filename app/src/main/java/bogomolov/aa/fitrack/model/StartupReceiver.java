package bogomolov.aa.fitrack.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;

import java.util.Date;

import static bogomolov.aa.fitrack.model.TrackerService.START_SERVICE_ACTION;

public class StartupReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {


        if (ActivityTransitionResult.hasResult(intent)) {
            ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
            for (ActivityTransitionEvent event : result.getTransitionEvents()) {
                if(((SystemClock.elapsedRealtime()-(event.getElapsedRealTimeNanos()/1000000))/1000) <= 30) {
                    Log.i("test", "ACTION RECOGNIZED " + event.toString() + " " + new Date(event.getElapsedRealTimeNanos() / 1000000));
                    TrackerService.startTrackerService(START_SERVICE_ACTION, context);
                    TrackingScheduler.stopActivityRecognition(context);
                }
            }
        }
    }
}
