package bogomolov.aa.fitrack.features.main

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import bogomolov.aa.fitrack.BuildConfig
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
    //context.registerReceiver(StartupReceiver(), IntentFilter(TRANSITIONS_RECEIVER_ACTION))

    Log.i("test", "requestActivityTransitionUpdates")
    val request = ActivityTransitionRequest(transitions)
    ActivityRecognition.getClient(context)
        .requestActivityTransitionUpdates(request, getPendingIntent(context))
}

private const val TRANSITIONS_RECEIVER_ACTION =
    "${BuildConfig.APPLICATION_ID}TRANSITIONS_RECEIVER_ACTION"

private fun getPendingIntent(context: Context): PendingIntent {
    val intent = Intent(context, StartupReceiver::class.java)
    //val intent = Intent(TRANSITIONS_RECEIVER_ACTION)
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
        Toast.makeText(context, "StartupReceiver onReceive", LENGTH_LONG).show()
        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)
            if (result != null)
                for (event in result.transitionEvents) {
                    printEvent(event, context)
                    if ((SystemClock.elapsedRealtime() - event.elapsedRealTimeNanos / 1000000) / 1000 <= 30) {
                        trackerService(START_SERVICE_ACTION, context)
                        stopActivityRecognition(context)
                        break
                    }
                }
        }
    }
}

private fun printEvent(event: ActivityTransitionEvent, context: Context) {
    val activityType =
        when (event.activityType) {
            DetectedActivity.WALKING -> "WALKING"
            DetectedActivity.RUNNING -> "RUNNING"
            DetectedActivity.IN_VEHICLE -> "IN_VEHICLE"
            else -> event.activityType.toString()
        }
    val transitionType =
        when (event.transitionType) {
            ActivityTransition.ACTIVITY_TRANSITION_ENTER -> "ENTER"
            ActivityTransition.ACTIVITY_TRANSITION_EXIT -> "EXIT"
            else -> event.transitionType.toString()
        }
    val elapsed =
        (SystemClock.elapsedRealtime() - event.elapsedRealTimeNanos / 1000000) / 1000
    Toast.makeText(context, "$activityType $transitionType $elapsed s", LENGTH_LONG)
        .show()
    Log.i("test", "$activityType $transitionType $elapsed s")
}

class BootReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        startActivityRecognition(context)
    }
}