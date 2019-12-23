package bogomolov.aa.fitrack.view.fragments;


import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

import javax.inject.Inject;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.dagger.AppComponent;
import bogomolov.aa.fitrack.dagger.AppModule;
import bogomolov.aa.fitrack.dagger.DaggerAppComponent;
import bogomolov.aa.fitrack.model.Point;
import bogomolov.aa.fitrack.model.Tag;
import bogomolov.aa.fitrack.model.Track;
import bogomolov.aa.fitrack.presenter.TrackViewPresenter;
import bogomolov.aa.fitrack.view.TagSelectionDialog;
import bogomolov.aa.fitrack.view.TrackViewView;


/**
 * A simple {@link Fragment} subclass.
 */
public class TrackViewFragment extends Fragment implements OnMapReadyCallback, TrackViewView {
    private TextView textTag;

    //@Inject
    TrackViewPresenter trackViewPresenter;

    public TrackViewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_track_view, container, false);

        //AppComponent appComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();
        //appComponent.injectsTrackViewActivity(this);

        trackViewPresenter = new TrackViewPresenter(this);

        long trackId = (Long) getArguments().get("trackId");
        Track track = trackViewPresenter.setTrack(trackId);


        /*
        Toolbar toolbar = findViewById(R.id.toolbar_track_view);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        */

        TextView textDistance = view.findViewById(R.id.track_text_distance);
        TextView textTime = view.findViewById(R.id.track_text_time);
        TextView textSpeed = view.findViewById(R.id.track_text_avg_speed);
        textTag = view.findViewById(R.id.track_text_tag);

        textTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TagSelectionDialog dialog = new TagSelectionDialog();
                dialog.setTagResultListener(trackViewPresenter);
                dialog.show(getChildFragmentManager(), "dialog");
            }
        });

        //setTitle(track.getName());
        textDistance.setText((int) track.getDistance() + " m");
        textTime.setText(track.getTimeString());
        textSpeed.setText(String.format("%.1f", 3.6 * track.getSpeed()) + " km/h");
        textTag.setText(track.getTag() != null ? track.getTag() : getResources().getString(R.string.no_tag));

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_track_view);
        mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void updateTag(Tag tag) {
        textTag.setText(tag.getName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        trackViewPresenter.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        List<Point> smoothedPoints = trackViewPresenter.getTrackPoints();
        if (smoothedPoints.size() > 0) {
            double minLat = 1000;
            double maxLat = 0;
            double minLng = 1000;
            double maxLng = 0;
            for (Point point : smoothedPoints) {
                if (point.getLat() < minLat) minLat = point.getLat();
                if (point.getLng() < minLng) minLng = point.getLng();
                if (point.getLat() > maxLat) maxLat = point.getLat();
                if (point.getLng() > maxLng) maxLng = point.getLng();
            }
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(new LatLng(minLat, maxLng));
            builder.include(new LatLng(maxLat, maxLng));
            builder.include(new LatLng(maxLat, minLng));
            builder.include(new LatLng(minLat, minLng));
            int padding = 50;
            LatLngBounds bounds = builder.build();
            try {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            } catch (Exception e) {
                Point lastPoint = smoothedPoints.get(smoothedPoints.size() - 1);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastPoint.getLat(), lastPoint.getLng()), 15));
            }
            googleMap.addPolyline((new PolylineOptions()).color(0xffffff00).clickable(false).add(Point.toPolylineCoordinates(smoothedPoints)));
            //trackSmoothedPolyline.setPoints(Arrays.asList(Point.toPolylineCoordinates(smoothedPoints)));
        }
    }

}
