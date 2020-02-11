package bogomolov.aa.fitrack.view.fragments


import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI

import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions

import java.util.Arrays

import javax.inject.Inject

import bogomolov.aa.fitrack.R
import bogomolov.aa.fitrack.dagger.ViewModelFactory
import bogomolov.aa.fitrack.databinding.FragmentMainBinding
import bogomolov.aa.fitrack.core.model.Point
import bogomolov.aa.fitrack.core.model.Track
import bogomolov.aa.fitrack.viewmodels.MainViewModel
import dagger.android.support.AndroidSupportInjection


class MainFragment : Fragment(), OnMapReadyCallback {
    private var googleMap: GoogleMap? = null
    private var trackSmoothedPolyline: Polyline? = null
    private var currentPositionMarker: Marker? = null
    private var zoomed: Boolean = false
    private var canStart: Boolean = false

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: MainViewModel

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
        val binding = DataBindingUtil.inflate<FragmentMainBinding>(inflater, R.layout.fragment_main, container, false)
        binding.lifecycleOwner = this
        val view = binding.root
        binding.viewModel = viewModel

        val toolbar = binding.toolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)

        val navController = Navigation.findNavController(activity!!, R.id.nav_host_fragment)
        val drawerLayout = activity!!.findViewById<DrawerLayout>(R.id.drawer_layout)
        NavigationUI.setupWithNavController(toolbar, navController, drawerLayout)

        viewModel.startStop.observe(this) { b ->
            canStart = b!!
            activity!!.invalidateOptionsMenu()
        }
        viewModel.lastPointLiveData.observe(this) { point ->
            updateView(viewModel.track, point, viewModel.smoothedPoints)
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment?
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
            R.id.menu_track_start -> viewModel.startTrack(context)
            R.id.menu_track_stop -> viewModel.stopTrack(context)
        }
        return true
    }

    private fun updateView(track: Track?, point: Point?, smoothedPoints: List<Point>) {
        if (googleMap != null) {
            if (point != null) {
                val latLng = LatLng(point.lat, point.lng)
                if (currentPositionMarker == null) {
                    currentPositionMarker = googleMap!!.addMarker(MarkerOptions().position(latLng).flat(true)
                            .anchor(0.5f, 0.5f)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.direction_arrow)))
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
                activity!!.invalidateOptionsMenu()
            }
            if (track != null && track.isOpened()) {
                var rotation = 0f
                if (smoothedPoints.size > 1)
                    rotation = angleFromCoordinate(smoothedPoints[smoothedPoints.size - 2], smoothedPoints[smoothedPoints.size - 1])
                currentPositionMarker!!.rotation = rotation
                if (trackSmoothedPolyline == null) {
                    trackSmoothedPolyline = googleMap!!.addPolyline(PolylineOptions().color(-0x10000)
                            .clickable(false).add(*toPolylineCoordinates(smoothedPoints)))
                } else {
                    trackSmoothedPolyline!!.points = Arrays.asList(*toPolylineCoordinates(smoothedPoints))
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
