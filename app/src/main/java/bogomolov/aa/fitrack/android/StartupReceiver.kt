package bogomolov.aa.fitrack.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.google.android.gms.location.ActivityTransitionResult

class StartupReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)
            if (result != null)
                for (event in result.transitionEvents) {
                    if ((SystemClock.elapsedRealtime() - event.elapsedRealTimeNanos / 1000000) / 1000 <= 30) {
                        TrackerService.startTrackerService(TrackerService.START_SERVICE_ACTION, context)
                        stopActivityRecognition(context)
                    }
                }
        }
    }
}