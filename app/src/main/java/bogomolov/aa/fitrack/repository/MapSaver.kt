package bogomolov.aa.fitrack.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import bogomolov.aa.fitrack.domain.MapSaver
import bogomolov.aa.fitrack.domain.model.Point
import bogomolov.aa.fitrack.domain.model.Track
import bogomolov.aa.fitrack.features.main.toPolylineCoordinates
import bogomolov.aa.fitrack.features.tracks.track.TrackViewFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapSaverImpl @Inject constructor(
    private val context: Context
) : MapSaver {

    override suspend fun save(track: Track, points: List<Point>, width: Int, height: Int) {
        withContext(Dispatchers.Main) {
            val options = GoogleMapOptions()
                .compassEnabled(false)
                .mapToolbarEnabled(false)
                .liteMode(true)
            val mapView = MapView(context, options)
            mapView.onCreate(null)
            mapView.getMapAsync { googleMap ->
                mapView.measure(
                    View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
                )
                mapView.layout(0, 0, width, height)
                TrackViewFragment.updateMap(googleMap, points)
                googleMap.moveCamera(CameraUpdateFactory.zoomTo(18f))
                for (latLng in toPolylineCoordinates(points)) {
                    var isVisible = googleMap.projection.visibleRegion.latLngBounds.contains(latLng)
                    while (!isVisible) {
                        googleMap.moveCamera(CameraUpdateFactory.zoomOut())
                        isVisible = googleMap.projection.visibleRegion.latLngBounds.contains(latLng)
                    }
                }
                googleMap.setOnMapLoadedCallback {
                    mapView.isDrawingCacheEnabled = true
                    mapView.measure(
                        View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
                    )
                    mapView.layout(0, 0, width, height)
                    mapView.buildDrawingCache(true)
                    val bitmap = Bitmap.createBitmap(mapView.drawingCache)
                    mapView.isDrawingCacheEnabled = false
                    saveImage(bitmap, getTrackImageFile(context, track))
                }
            }
        }
    }


    private fun saveImage(bitmap: Bitmap, fileName: String) {
        try {
            val os = FileOutputStream(File(fileName))
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
            os.flush()
            os.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun getTrackImageFile(context: Context, track: Track) =
    File(context.filesDir, "track_${track.id}.png").absolutePath