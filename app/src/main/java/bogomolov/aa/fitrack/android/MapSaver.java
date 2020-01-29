package bogomolov.aa.fitrack.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import bogomolov.aa.fitrack.core.model.Point;
import bogomolov.aa.fitrack.core.model.Track;
import bogomolov.aa.fitrack.view.fragments.MainFragment;
import bogomolov.aa.fitrack.view.fragments.TrackViewFragment;

public class MapSaver {

    public static String getTrackImageFile(Context context, Track track) {
        File filesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(filesDir, "track_" + track.getId() + ".png").getAbsolutePath();
    }

    public static void saveUI(Context context, Track track, List<Point> points, int width, int height) {
        Rx.ui(() -> save(context, track, points, width, height));
    }

    public static void save(Context context, Track track, List<Point> points, int width, int height) {
        GoogleMapOptions options = new GoogleMapOptions()
                .compassEnabled(false)
                .mapToolbarEnabled(false)
                .liteMode(true);
        MapView mMapView = new MapView(context, options);
        mMapView.onCreate(null);

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {

                mMapView.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
                mMapView.layout(0, 0, width, height);

                TrackViewFragment.updateMap(googleMap, points);
                googleMap.moveCamera(CameraUpdateFactory.zoomTo(18));


                for (LatLng latLng : MainFragment.toPolylineCoordinates(points)) {
                    boolean isVisible = googleMap.getProjection().getVisibleRegion().latLngBounds.contains(latLng);
                    while (!isVisible) {
                        googleMap.moveCamera(CameraUpdateFactory.zoomOut());
                        isVisible = googleMap.getProjection().getVisibleRegion().latLngBounds.contains(latLng);
                    }
                }


                googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        mMapView.setDrawingCacheEnabled(true);
                        mMapView.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
                        mMapView.layout(0, 0, width, height);
                        mMapView.buildDrawingCache(true);
                        Bitmap b = Bitmap.createBitmap(mMapView.getDrawingCache());
                        mMapView.setDrawingCacheEnabled(false);
                        Rx.worker(() -> persistImage(b, getTrackImageFile(context, track)));
                    }
                });

            }
        });
    }

    private static void persistImage(Bitmap bitmap, String fileName) {
        File imageFile = new File(fileName);
        try {
            OutputStream os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
