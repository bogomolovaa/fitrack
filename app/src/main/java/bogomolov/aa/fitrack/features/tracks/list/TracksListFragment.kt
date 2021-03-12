package bogomolov.aa.fitrack.features.tracks.list


import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import bogomolov.aa.fitrack.R
import bogomolov.aa.fitrack.databinding.FragmentTracksListBinding
import bogomolov.aa.fitrack.di.ViewModelFactory
import bogomolov.aa.fitrack.domain.*
import bogomolov.aa.fitrack.domain.model.Tag
import bogomolov.aa.fitrack.features.tracks.tags.TagResultListener
import bogomolov.aa.fitrack.features.tracks.tags.TagSelectionDialog
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject


class TracksListFragment : Fragment(), TagResultListener {
    private lateinit var adapter: TracksPagedAdapter
    private var actionMode: ActionMode? = null
    private lateinit var toolbar: Toolbar

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: TracksListViewModel
    private var spinnersCanClicked = false

    private val callback = object : ActionMode.Callback {

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.track_group_actions, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.menu_track_delete -> {
                    viewModel!!.deleteTracks(adapter.selectedIds)
                    actionMode!!.finish()
                }
                R.id.menu_track_tag -> {
                    val dialog = TagSelectionDialog()
                    dialog.tagResultListener = this@TracksListFragment
                    dialog.show(childFragmentManager, "TagSelectionDialog")
                }
            }
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            if (adapter != null) adapter!!.disableCheckMode()
            actionMode = null
        }

    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProvider(this, viewModelFactory).get(TracksListViewModel::class.java)
        val binding = DataBindingUtil.inflate<FragmentTracksListBinding>(inflater, R.layout.fragment_tracks_list, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        val view = binding.root

        toolbar = binding.toolbarTracksList
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        val navController = Navigation.findNavController(activity!!, R.id.nav_host_fragment)
        NavigationUI.setupWithNavController(toolbar, navController)

        val recyclerView = binding.trackRecycler
        recyclerView.layoutManager = LinearLayoutManager(context)
        if (viewModel.tracksLiveData.value == null)
            viewModel.updateTracks(getTodayRange())

        adapter = TracksPagedAdapter(this)
        recyclerView.adapter = adapter

        viewModel.tracksLiveData.observe(viewLifecycleOwner) { tracks ->
            adapter.submitList(tracks)
            val animation = AnimationUtils.loadLayoutAnimation(this@TracksListFragment.context, R.anim.track_item_layout_anim)
            recyclerView.layoutAnimation = animation
        }

        recyclerView.viewTreeObserver.addOnPreDrawListener(
                object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        recyclerView.viewTreeObserver.removeOnPreDrawListener(this)
                        startPostponedEnterTransition()
                        return true
                    }
                })

        postponeEnterTransition()

        val filterSpinner = binding.tracksTimeSpinner
        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                if (!spinnersCanClicked) return
                when (i) {
                    FILTER_TODAY -> viewModel.updateTracks(getTodayRange())
                    FILTER_WEEK -> viewModel.updateTracks(getWeekRange())
                    FILTER_MONTH -> viewModel.updateTracks(getMonthRange())
                    FILTER_SELECT -> selectDatesRange(childFragmentManager) { dates -> viewModel.updateTracks(dates) }
                    else -> viewModel.updateTracks(getTodayRange())
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }

        activity!!.window.decorView.postDelayed({ spinnersCanClicked = true }, 500)

        return view
    }

    fun onLongClick() {
        if (actionMode == null)
            actionMode = toolbar.startActionMode(callback)
        else
            actionMode!!.finish()
    }

    override fun onTagSelectionResult(tag: Tag?) {
        if (tag != null) viewModel.setTag(tag, adapter.selectedIds)
        if (actionMode != null) actionMode!!.finish()
    }

    companion object {
        private val FILTER_TODAY = 0
        private val FILTER_WEEK = 1
        private val FILTER_MONTH = 2
        private val FILTER_SELECT = 3
    }

}
