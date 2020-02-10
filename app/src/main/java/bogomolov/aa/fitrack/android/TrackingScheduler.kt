package bogomolov.aa.fitrack.android

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity
import java.util.*

fun startActivityRecognition(context: Context) {
    val transitions = ArrayList<ActivityTransition>()
    transitions.add(
            ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.WALKING)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                    .build())
    transitions.add(
            ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.RUNNING)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                    .build())
    transitions.add(
            ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.IN_VEHICLE)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                    .build())
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
