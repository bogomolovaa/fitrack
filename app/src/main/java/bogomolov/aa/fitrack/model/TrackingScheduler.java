package bogomolov.aa.fitrack.model;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static bogomolov.aa.fitrack.view.activities.SettingsActivity.*;

public class TrackingScheduler {

    public static void startActivityRecognition(Context context){

        List<ActivityTransition> transitions = new ArrayList<>();
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        ActivityTransitionRequest request = new ActivityTransitionRequest(transitions);
        Task<Void> task = ActivityRecognition.getClient(context)
                .requestActivityTransitionUpdates(request, getPendingIntent(context));
        task.addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Log.i("test","startActivityRecognition success");
                    }
                }
        );
        task.addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.i("test","startActivityRecognition failure");
                    }
                }
        );
    }

    private static PendingIntent getPendingIntent(Context context){
        Intent intent = new Intent(context, StartupReceiver.class);
        return PendingIntent.getBroadcast(context,1,intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void stopActivityRecognition(Context context){
        PendingIntent myPendingIntent = getPendingIntent(context);
        Task<Void> task = ActivityRecognition.getClient(context)
                .removeActivityTransitionUpdates(myPendingIntent);

        task.addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        myPendingIntent.cancel();
                        Log.i("test","stopActivityRecognition success");
                    }
                }
        );

        task.addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.i("test","stopActivityRecognition failure");
                    }
                }
        );
    }

    public static void schedule(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isEnabled = sharedPreferences.getBoolean(KEY_TRACKING_TIME_ENABLED, true);
        if (isEnabled) {
            int[] startHM = parseHoursMinutes(sharedPreferences.getString(KEY_START_TRACKING_TIME, "00:00"));
            int[] endHM = parseHoursMinutes(sharedPreferences.getString(KEY_END_TRACKING_TIME, "00:00"));
            if (startHM != null) chargeAlarm(context, KEY_START_TRACKING_TIME, startHM[0], startHM[1]);
            if (endHM != null) chargeAlarm(context,KEY_END_TRACKING_TIME, endHM[0], endHM[1]);
        }
    }

    public static void chargeAlarm(Context context, String KEY, int hours, int minutes) {
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Intent intent = new Intent(context, StartupReceiver.class);
        int requestCode = 0;
        if (KEY.equals(KEY_START_TRACKING_TIME)) {
            intent.setAction(TrackerService.START_SERVICE_ACTION);
            requestCode = 1;
        } else if (KEY.equals(KEY_END_TRACKING_TIME)) {
            intent.setAction(TrackerService.STOP_SERVICE_ACTION);
            requestCode = 2;
        }
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 24 * 3600 * 1000, pendingIntent);
    }

    public static void stopAlarms(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        for (int i = 1; i <= 2; i++) {
            Intent intent = new Intent(context, StartupReceiver.class);
            intent.setAction(i == 1 ? TrackerService.START_SERVICE_ACTION : TrackerService.STOP_SERVICE_ACTION);
            alarmManager.cancel(PendingIntent.getBroadcast(context, i, intent, 0));
        }
    }

}
