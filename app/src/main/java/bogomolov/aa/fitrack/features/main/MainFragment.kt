package bogomolov.aa.fitrack.features.main

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import bogomolov.aa.fitrack.R
import bogomolov.aa.fitrack.databinding.FragmentMainBinding
import bogomolov.aa.fitrack.di.ViewModelFactory
import bogomolov.aa.fitrack.domain.model.Point
import bogomolov.aa.fitrack.features.settings.KEY_SERVICE_STARTED
import bogomolov.aa.fitrack.features.settings.getSetting
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class MainFragment : Fragment(), OnMapReadyCallback {
    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: MainViewModel by viewModels { viewModelFactory }
    private var googleMap: GoogleMap? = null
    private var trackSmoothedPolyline: Polyline? = null
    private var currentPositionMarker: Marker? = null
    private var zoomed: Boolean = false
    private var canStart: Boolean = false
    private lateinit var binding: FragmentMainBinding

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentMainBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)

        val navController = findNavController()
        val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
        NavigationUI.setupWithNavController(binding.toolbar, navController, drawerLayout)

        viewModel.canStartLiveData.observe(viewLifecycleOwner) {
            canStart = it
            requireActivity().invalidateOptionsMenu()
        }
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        viewModel.stateLiveData.observe(viewLifecycleOwner) {
            updateView(it)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.menu_track_start).isVisible = canStart
        menu.findItem(R.id.menu_track_stop).isVisible = !canStart
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.start_stop, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_track_start -> {
                if (!getSetting(KEY_SERVICE_STARTED, requireContext()))
                    trackerService(START_SERVICE_ACTION, requireContext())
                viewModel.startTrack()
            }
            R.id.menu_track_stop -> viewModel.stopTrack()
        }
        return true
    }

    private fun updateView(state: MainState) {
        binding.textDistance.text = state.distance
        binding.textTime.text = state.time
        binding.textAvgSpeed.text = state.avgSpeed
        binding.textSpeed.text = state.speed
        val point = state.lastPoint
        val track = state.currentTrack
        val smoothedPoints = state.smoothedPoints
        val googleMap = this.googleMap
        if (googleMap != null) {
            if (point != null) {
                val latLng = LatLng(point.lat, point.lng)
                if (currentPositionMarker == null) {
                    currentPositionMarker = googleMap.addMarker(
                        MarkerOptions().position(latLng).flat(true)
                            .anchor(0.5f, 0.5f)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.direction_arrow))
                    )
                } else {
                    Log.i("test", "updateView position ${latLng.latitude} ${latLng.longitude}")
                    currentPositionMarker!!.position = latLng
                }

                if (!zoomed) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
                    zoomed = true
                } else if (track != null && track.isOpened()) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                }
            }

            if (track != null && track.isOpened() && smoothedPoints != null) {
                currentPositionMarker!!.rotation = if (smoothedPoints.size > 1)
                    angleFromCoordinate(
                        smoothedPoints[smoothedPoints.size - 2],
                        smoothedPoints[smoothedPoints.size - 1]
                    ) else 0f
                if (trackSmoothedPolyline == null) trackSmoothedPolyline =
                    googleMap.addPolyline(PolylineOptions().color(-0x10000).clickable(false))
                trackSmoothedPolyline!!.points = toPolylineCoordinates(smoothedPoints)
            } else {
                if (trackSmoothedPolyline != null) {
                    trackSmoothedPolyline!!.remove()
                    trackSmoothedPolyline = null
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
    }

    override fun onStart() {
        super.onStart()
        viewModel.startUpdating()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopUpdating()
    }
}

fun toPolylineCoordinates(points: List<Point>) = points.map { LatLng(it.lat, it.lng) }

private fun angleFromCoordinate(point1: Point, point2: Point): Float {
    val f1 = Math.toRadians(point1.lat)
    val f2 = Math.toRadians(point2.lat)
    val l1 = Math.toRadians(point1.lng)
    val l2 = Math.toRadians(point2.lng)

    val y = Math.sin(l2 - l1) * Math.cos(f2)
    val x = Math.cos(f1) * Math.sin(f2) - Math.sin(f1) * Math.cos(f2) * Math.cos(l2 - l1)

    val brng = Math.toDegrees(Math.atan2(y, x))
    return (brng - 90).toFloat()
}