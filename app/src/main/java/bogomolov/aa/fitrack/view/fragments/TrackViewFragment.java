package bogomolov.aa.fitrack.view.fragments;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.transition.ChangeBounds;
import androidx.transition.TransitionInflater;

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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

import javax.inject.Inject;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.android.MapSaver;
import bogomolov.aa.fitrack.dagger.ViewModelFactory;
import bogomolov.aa.fitrack.databinding.FragmentTrackViewBinding;
import bogomolov.aa.fitrack.core.model.Point;
import bogomolov.aa.fitrack.viewmodels.TrackViewModel;
import dagger.android.support.AndroidSupportInjection;


public class TrackViewFragment extends Fragment implements OnMapReadyCallback {
    private TrackViewModel viewModel;
    private GoogleMap googleMap;

    @Inject
    ViewModelFactory viewModelFactory;

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSharedElementEnterTransition(TransitionInflater.from(getContext()).inflateTransition(android.R.transition.move));
        setSharedElementReturnTransition(TransitionInflater.from(getContext()).inflateTransition(android.R.transition.move));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(TrackViewModel.class);
        FragmentTrackViewBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_track_view, container, false);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);
        View view = binding.getRoot();


        long trackId = (Long) getArguments().get("trackId");
        viewModel.setTrack(trackId);
        viewModel.trackLiveData.observe(this, track -> {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("");
            startPostponedEnterTransition();
        });
        postponeEnterTransition();

        Toolbar toolbar = binding.toolbarTrackView;
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(toolbar, navController);

        binding.trackTextTag.setOnClickListener(v -> showTagSelection());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_track_view);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        viewModel.trackPoints.observe(this, points -> {
            updateMap(googleMap, points);
            //MapSaver.save(getContext(), viewModel.trackLiveData.getValue(), points, 600, 400);
        });

        return view;
    }

    private void showTagSelection() {
        TagSelectionDialog dialog = new TagSelectionDialog();
        dialog.setTagResultListener(viewModel);
        dialog.show(getChildFragmentManager(), "TagSelectionDialog");
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.track_view_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.track_view_set_tag_item:
                showTagSelection();
                break;
            default:
                break;
        }
        return true;
    }

    public static void updateMap(GoogleMap googleMap, List<Point> smoothedPoints) {
        if (googleMap != null && smoothedPoints.size() > 0) {
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
            googleMap.addPolyline((new PolylineOptions()).color(0xffff0000).clickable(false).add(MainFragment.toPolylineCoordinates(smoothedPoints)));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        List<Point> points = viewModel.trackPoints.getValue();
        if (points != null) updateMap(googleMap, points);
    }
}
