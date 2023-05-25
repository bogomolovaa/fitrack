package bogomolov.aa.fitrack.repository

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import bogomolov.aa.fitrack.domain.Repository
import bogomolov.aa.fitrack.domain.model.SMOOTHED
import bogomolov.aa.fitrack.domain.model.Track
import bogomolov.aa.fitrack.features.main.toPolylineCoordinates
import bogomolov.aa.fitrack.features.tracks.track.updateMap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

private const val WIDTH = 600
private const val HEIGHT = 400

class MapSaver (
    private val context: Context,
    private val repository: Repository
) {

    fun save(track: Track, imageView: ImageView, coroutineScope: CoroutineScope) {
        val options =
            GoogleMapOptions().compassEnabled(false).mapToolbarEnabled(false).liteMode(true)
        val mapView = MapView(context, options)
        mapView.onCreate(null)
        mapView.getMapAsync { googleMap ->
            mapView.measure(
                View.MeasureSpec.makeMeasureSpec(WIDTH, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(HEIGHT, View.MeasureSpec.EXACTLY)
            )
            mapView.layout(0, 0, WIDTH, HEIGHT)
            coroutineScope.launch(Dispatchers.IO) {
                val points = repository.getTrackPoints(track, SMOOTHED)
                withContext(Dispatchers.Main) {
                    updateMap(googleMap, points)
                    googleMap.moveCamera(CameraUpdateFactory.zoomTo(18f))
                    for (latLng in toPolylineCoordinates(points)) {
                        var visible =
                            googleMap.projection.visibleRegion.latLngBounds.contains(latLng)
                        while (!visible) {
                            googleMap.moveCamera(CameraUpdateFactory.zoomOut())
                            visible =
                                googleMap.projection.visibleRegion.latLngBounds.contains(latLng)
                        }
                    }
                    googleMap.setOnMapLoadedCallback {
                        mapView.isDrawingCacheEnabled = true
                        mapView.layout(0, 0, WIDTH, HEIGHT)
                        mapView.buildDrawingCache(true)
                        val bitmap = Bitmap.createBitmap(mapView.drawingCache)
                        mapView.isDrawingCacheEnabled = false
                        imageView.setImageBitmap(bitmap)
                        coroutineScope.launch(Dispatchers.IO) {
                            saveImage(bitmap, getTrackImageFile(context, track))
                        }
                    }
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