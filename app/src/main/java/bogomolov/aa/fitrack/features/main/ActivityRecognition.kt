package bogomolov.aa.fitrack.android

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import bogomolov.aa.fitrack.features.main.START_SERVICE_ACTION
import bogomolov.aa.fitrack.features.main.startTrackerService
import com.google.android.gms.location.*
import java.util.*

fun startActivityRecognition(context: Context) {
    val transitions = ArrayList<ActivityTransition>()
    transitions.add(
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.WALKING)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build()
    )
    transitions.add(
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.RUNNING)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build()
    )
    transitions.add(
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.IN_VEHICLE)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build()
    )
    val request = ActivityTransitionRequest(transitions)
    ActivityRecognition.getClient(context)
        .requestActivityTransitionUpdates(request, getPendingIntent(context))

}

private fun getPendingIntent(context: Context): PendingIntent {
    val intent = Intent(context, StartupReceiver::class.java)
    return PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
}

fun stopActivityRecognition(context: Context) {
    val myPendingIntent = getPendingIntent(context)
    val task = ActivityRecognition.getClient(context)
        .removeActivityTransitionUpdates(myPendingIntent)
    task.addOnSuccessListener { myPendingIntent.cancel() }
}

class StartupReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)
            if (result != null)
                for (event in result.transitionEvents) {
                    if ((SystemClock.elapsedRealtime() - event.elapsedRealTimeNanos / 1000000) / 1000 <= 30) {
                        startTrackerService(START_SERVICE_ACTION, context)
                        stopActivityRecognition(context)
                    }
                }
        }
    }
}

class BootReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        startActivityRecognition(context)
    }
}