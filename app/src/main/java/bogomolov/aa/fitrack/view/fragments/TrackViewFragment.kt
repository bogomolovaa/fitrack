package bogomolov.aa.fitrack.view.fragments


import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.transition.ChangeBounds
import androidx.transition.TransitionInflater

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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions

import javax.inject.Inject

import bogomolov.aa.fitrack.R
import bogomolov.aa.fitrack.dagger.ViewModelFactory
import bogomolov.aa.fitrack.databinding.FragmentTrackViewBinding
import bogomolov.aa.fitrack.core.model.Point
import bogomolov.aa.fitrack.viewmodels.TrackViewModel
import dagger.android.support.AndroidSupportInjection


class TrackViewFragment : Fragment(), OnMapReadyCallback {
    private lateinit var viewModel: TrackViewModel
    private var googleMap: GoogleMap? = null

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        sharedElementReturnTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProvider(this, viewModelFactory).get(TrackViewModel::class.java)
        val binding = DataBindingUtil.inflate<FragmentTrackViewBinding>(inflater, R.layout.fragment_track_view, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        val view = binding.root


        val trackId = (arguments!!.get("trackId") as Long?)!!
        viewModel.setTrack(trackId)
        viewModel.trackLiveData.observe(this) { track ->
            (activity as AppCompatActivity).supportActionBar!!.setTitle("")
            startPostponedEnterTransition()
        }
        postponeEnterTransition()

        val toolbar = binding.toolbarTrackView
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)

        val navController = Navigation.findNavController(activity!!, R.id.nav_host_fragment)
        NavigationUI.setupWithNavController(toolbar, navController)

        binding.trackTextTag.setOnClickListener { v -> showTagSelection() }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_track_view) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        viewModel.trackPoints.observe(this, { points -> updateMap(googleMap, points) })

        return view
    }

    private fun showTagSelection() {
        val dialog = TagSelectionDialog()
        dialog.tagResultListener = viewModel
        dialog.show(childFragmentManager, "TagSelectionDialog")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.track_view_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.track_view_set_tag_item -> showTagSelection()
        }
        return true
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        val points = viewModel.trackPoints.value
        if (points != null) updateMap(googleMap, points)
    }

    companion object {

        fun updateMap(googleMap: GoogleMap?, smoothedPoints: List<Point>) {
            if (googleMap != null && smoothedPoints.size > 0) {
                var minLat = 1000.0
                var maxLat = 0.0
                var minLng = 1000.0
                var maxLng = 0.0
                for (point in smoothedPoints) {
                    if (point.lat < minLat) minLat = point.lat
                    if (point.lng < minLng) minLng = point.lng
                    if (point.lat > maxLat) maxLat = point.lat
                    if (point.lng > maxLng) maxLng = point.lng
                }
                val builder = LatLngBounds.Builder()
                builder.include(LatLng(minLat, maxLng))
                builder.include(LatLng(maxLat, maxLng))
                builder.include(LatLng(maxLat, minLng))
                builder.include(LatLng(minLat, minLng))
                val padding = 50
                val bounds = builder.build()
                try {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
                } catch (e: Exception) {
                    val lastPoint = smoothedPoints[smoothedPoints.size - 1]
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastPoint.lat, lastPoint.lng), 15f))
                }

                googleMap.addPolyline(PolylineOptions().color(-0x10000).clickable(false).add(*MainFragment.toPolylineCoordinates(smoothedPoints)))
            }
        }
    }
}
