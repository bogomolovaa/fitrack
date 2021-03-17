package bogomolov.aa.fitrack.features.main

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import bogomolov.aa.fitrack.R
import bogomolov.aa.fitrack.domain.Repository
import bogomolov.aa.fitrack.domain.getTodayRange
import bogomolov.aa.fitrack.domain.model.RAW
import bogomolov.aa.fitrack.domain.model.sumDistance
import bogomolov.aa.fitrack.domain.model.smooth
import bogomolov.aa.fitrack.domain.model.sumTracks
import dagger.android.AndroidInjection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

fun updateWidget(context: Context) {
    val intent = Intent(context, WidgetProvider::class.java)
    intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
    val ids = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, WidgetProvider::class.java))
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
    context.sendBroadcast(intent)
}

class WidgetProvider : AppWidgetProvider() {
    @Inject
    lateinit var repository: Repository

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)
        super.onReceive(context, intent)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            GlobalScope.launch(Dispatchers.IO) {
                val tracks = repository.getFinishedTracks(getTodayRange(), null)
                val sumTrack = sumTracks(tracks)
                var distance = sumTrack.distance
                val lastTrack = repository.getLastTrack()
                if (lastTrack!!.isOpened()) {
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