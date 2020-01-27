package bogomolov.aa.fitrack.android;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.util.List;

import javax.inject.Inject;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.core.DateUtils;
import bogomolov.aa.fitrack.core.model.Track;
import bogomolov.aa.fitrack.repository.Repository;
import bogomolov.aa.fitrack.view.activities.MainActivity;
import dagger.android.AndroidInjection;

public class WidgetProvider extends AppWidgetProvider {
    @Inject
    Repository repository;

    @Override
    public void onReceive(Context context, Intent intent) {
        AndroidInjection.inject(this, context);
        super.onReceive(context, intent);
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];

            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            List<Track> tracks = repository.getFinishedTracks(DateUtils.getTodayRange(), null);
            Track sumTrack = Track.sumTracks(tracks);
            String widgetText = context.getResources().getString(R.string.distance_km, sumTrack.getDistance());

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            views.setOnClickPendingIntent(R.id.widget_text, pendingIntent);
            views.setTextViewText(R.id.widget_text, widgetText);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

}
