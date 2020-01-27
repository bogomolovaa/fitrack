package bogomolov.aa.fitrack.view.fragments;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.android.MapSaver;
import bogomolov.aa.fitrack.dagger.ViewModelFactory;
import bogomolov.aa.fitrack.databinding.FragmentMainBinding;
import bogomolov.aa.fitrack.core.model.Point;
import bogomolov.aa.fitrack.core.model.Track;
import bogomolov.aa.fitrack.viewmodels.MainViewModel;
import dagger.android.support.AndroidSupportInjection;


public class MainFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private Polyline trackRawPolyline;
    private Polyline trackSmoothedPolyline;
    private Marker currentPositionMarker;
    private boolean zoomed;
    private boolean canStart;

    @Inject
    ViewModelFactory viewModelFactory;

    private MainViewModel viewModel;

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        viewModel = new ViewModelProvider(this, viewModelFactory).get(MainViewModel.class);
        FragmentMainBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);
        binding.setLifecycleOwner(this);
        View view = binding.getRoot();
        binding.setViewModel(viewModel);

        Toolbar toolbar = binding.toolbar;
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
        NavigationUI.setupWithNavController(toolbar, navController, drawerLayout);

        viewModel.startStop.observe(this, b -> {
            canStart = b;
            getActivity().invalidateOptionsMenu();
        });
        viewModel.lastPointLiveData.observe(this, point -> updateView(viewModel.track, point, viewModel.rawPoints, viewModel.smoothedPoints));

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        return view;
    }


    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.menu_track_start).setVisible(canStart);
        menu.findItem(R.id.menu_track_stop).setVisible(!canStart);
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.start_stop, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_track_start:
                viewModel.startTrack(getContext());
                break;
            case R.id.menu_track_stop:
                viewModel.stopTrack(getContext());
                break;
            default:
                break;
        }
        return true;
    }

    private void updateView(Track track, Point point, List<Point> rawTrackPoints, List<Point> smoothedPoints) {
        if (googleMap != null) {
            if (point != null) {
                LatLng latLng = new LatLng(point.getLat(), point.getLng());
                if (currentPositionMarker == null) {
                    currentPositionMarker = googleMap.addMarker(new MarkerOptions().
                            position(latLng).flat(true).anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory.fromResource(R.drawable.direction_arrow)));
                } else {
                    currentPositionMarker.setPosition(latLng);

                }

                if (!zoomed) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
                    zoomed = true;
                } else if (track != null && track.isOpened()) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            }

            if (track != null) {
                canStart = !track.isOpened();
                getActivity().invalidateOptionsMenu();
            }
            if (track != null && track.isOpened()) {
                if (trackRawPolyline == null) {
                    trackRawPolyline = googleMap.addPolyline((new PolylineOptions())
                            .clickable(false).add(toPolylineCoordinates(rawTrackPoints)));
                } else {
                    trackRawPolyline.setPoints(Arrays.asList(toPolylineCoordinates(rawTrackPoints)));
                }


                float rotation = 0;
                if (smoothedPoints.size() > 1)
                    rotation = angleFromCoordinate(smoothedPoints.get(smoothedPoints.size() - 2), smoothedPoints.get(smoothedPoints.size() - 1));
                currentPositionMarker.setRotation(rotation);
                if (trackSmoothedPolyline == null) {
                    trackSmoothedPolyline = googleMap.addPolyline((new PolylineOptions()).color(0xffff0000)
                            .clickable(false).add(toPolylineCoordinates(smoothedPoints)));
                } else {
                    trackSmoothedPolyline.setPoints(Arrays.asList(toPolylineCoordinates(smoothedPoints)));
                }
            } else {
                if (trackRawPolyline != null) {
                    trackRawPolyline.remove();
                    trackRawPolyline = null;
                }
                if (trackSmoothedPolyline != null) {
                    trackSmoothedPolyline.remove();
                    trackSmoothedPolyline = null;
                }
            }
        }
    }

    private static float angleFromCoordinate(Point point1, Point point2) {
        double f1 = Math.toRadians(point1.getLat());
        double f2 = Math.toRadians(point2.getLat());
        double l1 = Math.toRadians(point1.getLng());
        double l2 = Math.toRadians(point2.getLng());

        double y = Math.sin(l2 - l1) * Math.cos(f2);
        double x = Math.cos(f1) * Math.sin(f2) - Math.sin(f1) * Math.cos(f2) * Math.cos(l2 - l1);

        double brng = Math.toDegrees(Math.atan2(y, x));
        return (float) (brng - 90);
    }

    public static LatLng[] toPolylineCoordinates(List<Point> points) {
        LatLng[] latLngs = new LatLng[points.size()];
        for (int i = 0; i < points.size(); i++)
            latLngs[i] = new LatLng(points.get(i).getLat(), points.get(i).getLng());
        return latLngs;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.startUpdating();
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.stopUpdating();
    }

}
