package bogomolov.aa.fitrack.features.main

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import bogomolov.aa.fitrack.R
import bogomolov.aa.fitrack.TrackerApplication
import bogomolov.aa.fitrack.domain.Repository
import bogomolov.aa.fitrack.domain.getTodayRange
import bogomolov.aa.fitrack.domain.model.RAW
import bogomolov.aa.fitrack.domain.model.smooth
import bogomolov.aa.fitrack.domain.model.sumDistance
import bogomolov.aa.fitrack.domain.model.sumTracks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun updateWidget(context: Context) {
    val intent = Intent(context, WidgetProvider::class.java)
    intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
    val ids = AppWidgetManager.getInstance(context)
        .getAppWidgetIds(ComponentName(context, WidgetProvider::class.java))
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
    context.sendBroadcast(intent)
}

class WidgetProvider : AppWidgetProvider()  {
    private lateinit var repository: Repository

    override fun onReceive(context: Context, intent: Intent) {
        repository = (context as TrackerApplication).koin.get()
        super.onReceive(context, intent)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            GlobalScope.launch(Dispatchers.IO) {
                val sumTrack = sumTracks(repository.getFinishedTracks(getTodayRange()))
                var distance = sumTrack.distance
                val lastTrack = repository.getLastTrack()
                if (lastTrack?.isOpened() == true) {
                    val smoothedPoints = smooth(repository.getTrackPoints(lastTrack, RAW))
                    distance += sumDistance(smoothedPoints)
                }
                val widgetText = context.resources.getString(R.string.distance_km, distance / 1000)
                val views = RemoteViews(context.packageName, R.layout.widget_layout)
                views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent)
                views.setTextViewText(R.id.widget_text, widgetText)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}