package bogomolov.aa.fitrack.features.main


import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import bogomolov.aa.fitrack.R
import bogomolov.aa.fitrack.databinding.FragmentMainBinding
import bogomolov.aa.fitrack.di.ViewModelFactory
import bogomolov.aa.fitrack.domain.model.Point
import bogomolov.aa.fitrack.domain.model.Track
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import dagger.android.support.AndroidSupportInjection
import java.util.*
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


    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentMainBinding>(
            inflater,
            R.layout.fragment_main,
            container,
            false
        )
        binding.lifecycleOwner = this
        val view = binding.root
        binding.viewModel = viewModel

        val toolbar = binding.toolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)

        val navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
        NavigationUI.setupWithNavController(toolbar, navController, drawerLayout)

        viewModel.canStartLiveData.observe(viewLifecycleOwner) {
            canStart = it
            requireActivity().invalidateOptionsMenu()
        }
        viewModel.lastPointLiveData.observe(viewLifecycleOwner) { point ->
            updateView(point)
        }

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        return view
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
                if (TrackerService.working) {
                    viewModel.startTrack()
                } else {
                    startTrackerService(START_SERVICE_ACTION, requireContext())
                }
            }
            R.id.menu_track_stop -> viewModel.stopTrack()
        }
        return true
    }

    private fun updateView(point: Point?) {
        val track = viewModel.currentTrack
        val smoothedPoints = viewModel.smoothedPoints
        if (googleMap != null) {
            if (point != null) {
                val latLng = LatLng(point.lat, point.lng)
                if (currentPositionMarker == null) {
                    currentPositionMarker = googleMap!!.addMarker(
                        MarkerOptions().position(latLng).flat(true)
                            .anchor(0.5f, 0.5f)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.direction_arrow))
                    )
                } else {
                    currentPositionMarker!!.position = latLng
                }

                if (!zoomed) {
                    googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
                    zoomed = true
                } else if (track != null && track.isOpened()) {
                    googleMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                }
            }

            if (track != null) {
                canStart = !track.isOpened()
                requireActivity().invalidateOptionsMenu()
            }
            if (track != null && track.isOpened()) {
                val rotation = if (smoothedPoints.size > 1)
                    angleFromCoordinate(
                        smoothedPoints[smoothedPoints.size - 2],
                        smoothedPoints[smoothedPoints.size - 1]
                    ) else 0f
                currentPositionMarker!!.rotation = rotation
                if (trackSmoothedPolyline == null) {
                    trackSmoothedPolyline = googleMap!!.addPolyline(
                        PolylineOptions().color(-0x10000)
                            .clickable(false).add(*toPolylineCoordinates(smoothedPoints))
                    )
                } else {
                    trackSmoothedPolyline!!.points =
                        Arrays.asList(*toPolylineCoordinates(smoothedPoints))
                }
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

    companion object {

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

        fun toPolylineCoordinates(points: List<Point>): Array<LatLng> =
            Array(points.size) { i -> LatLng(points[i].lat, points[i].lng) }
    }

}
