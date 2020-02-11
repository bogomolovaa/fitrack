package bogomolov.aa.fitrack.android

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.view.View
import bogomolov.aa.fitrack.core.model.Point
import bogomolov.aa.fitrack.core.model.Track
import bogomolov.aa.fitrack.view.fragments.MainFragment
import bogomolov.aa.fitrack.view.fragments.TrackViewFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView
import java.io.File
import java.io.FileOutputStream


fun getTrackImageFile(context: Context, track: Track): String =
        File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "track_" + track.id + ".png").absolutePath

fun saveUI(context: Context, track: Track, points: List<Point>, width: Int, height: Int) {
    ui { save(context, track, points, width, height) }
}

private fun save(context: Context, track: Track, points: List<Point>, width: Int, height: Int) {
    val options = GoogleMapOptions()
            .compassEnabled(false)
            .mapToolbarEnabled(false)
            .liteMode(true)
    val mMapView = MapView(context, options)
    mMapView.onCreate(null)
    mMapView.getMapAsync { googleMap ->
        mMapView.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY))
        mMapView.layout(0, 0, width, height)
        TrackViewFragment.updateMap(googleMap, points)
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(18f))
        for (latLng in MainFragment.toPolylineCoordinates(points)) {
            var isVisible = googleMap.projection.visibleRegion.latLngBounds.contains(latLng)
            while (!isVisible) {
                googleMap.moveCamera(CameraUpdateFactory.zoomOut())
                isVisible = googleMap.projection.visibleRegion.latLngBounds.contains(latLng)
            }
        }
        googleMap.setOnMapLoadedCallback {
            mMapView.isDrawingCacheEnabled = true
            mMapView.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY))
            mMapView.layout(0, 0, width, height)
            mMapView.buildDrawingCache(true)
            val b = Bitmap.createBitmap(mMapView.drawingCache)
            mMapView.isDrawingCacheEnabled = false
            Rx.worker(Runnable { persistImage(b, getTrackImageFile(context, track)) })
        }
    }
}

private fun persistImage(bitmap: Bitmap, fileName: String) {
    val imageFile = File(fileName)
    try {
        val os = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
        os.flush()
        os.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
