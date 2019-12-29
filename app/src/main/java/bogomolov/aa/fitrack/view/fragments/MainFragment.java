package bogomolov.aa.fitrack.view.fragments;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import bogomolov.aa.fitrack.R;
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
    private Menu startStopMenu;

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

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel.class);
        FragmentMainBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);
        binding.setLifecycleOwner(this);
        View view = binding.getRoot();
        binding.setViewModel(viewModel);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
        NavigationUI.setupWithNavController(toolbar, navController, drawerLayout);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_tracking);


        viewModel.startStop.observe(this, this::showStartStopButtons);
        viewModel.lastPointLiveData.observe(this, point -> updateView(viewModel.track, point, viewModel.rawPoints, viewModel.smoothedPoints));

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        return view;
    }

    public void showStartStopButtons(boolean canStart) {
        if (startStopMenu != null) {
            startStopMenu.findItem(R.id.menu_track_start).setVisible(canStart);
            startStopMenu.findItem(R.id.menu_track_stop).setVisible(!canStart);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.start_stop, menu);
        startStopMenu = menu;
        viewModel.onStartStopButtonsCreated();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_track_start:
                viewModel.startTrack(getContext());
                break;
            case R.id.menu_track_stop:
                viewModel.stopTrack();
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
                    currentPositionMarker = googleMap.addMarker(new MarkerOptions().position(latLng).title("Current Location"));
                } else {
                    currentPositionMarker.setPosition(latLng);
                }
                if (track == null || !track.isOpened()) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
                } else {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            }

            if (track != null) showStartStopButtons(!track.isOpened());
            if (track != null && track.isOpened()) {
                if (trackRawPolyline == null) {
                    trackRawPolyline = googleMap.addPolyline((new PolylineOptions())
                            .clickable(false).add(Point.toPolylineCoordinates(rawTrackPoints)));
                } else {
                    trackRawPolyline.setPoints(Arrays.asList(Point.toPolylineCoordinates(rawTrackPoints)));
                }

                if (trackSmoothedPolyline == null) {
                    trackSmoothedPolyline = googleMap.addPolyline((new PolylineOptions()).color(0xffffff00)
                            .clickable(false).add(Point.toPolylineCoordinates(smoothedPoints)));
                } else {
                    trackSmoothedPolyline.setPoints(Arrays.asList(Point.toPolylineCoordinates(smoothedPoints)));
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
