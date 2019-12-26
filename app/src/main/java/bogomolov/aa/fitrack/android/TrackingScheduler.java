package bogomolov.aa.fitrack.android;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import bogomolov.aa.fitrack.android.StartupReceiver;


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



}
