package bogomolov.aa.fitrack.features.tracks.list

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import bogomolov.aa.fitrack.R
import bogomolov.aa.fitrack.databinding.FragmentTracksListBinding
import bogomolov.aa.fitrack.di.ViewModelFactory
import bogomolov.aa.fitrack.domain.getMonthRange
import bogomolov.aa.fitrack.domain.getTodayRange
import bogomolov.aa.fitrack.domain.getWeekRange
import bogomolov.aa.fitrack.domain.selectDatesRange
import bogomolov.aa.fitrack.features.shared.onSelection
import bogomolov.aa.fitrack.features.tracks.tags.TagSelectionDialog
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class TracksListFragment : Fragment() {
    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: TracksListViewModel by viewModels { viewModelFactory }
    private lateinit var adapter: TracksPagedAdapter
    private var actionMode: ActionMode? = null
    private lateinit var toolbar: Toolbar
    private var spinnersCanClicked = false

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentTracksListBinding.inflate(inflater, container, false)
        toolbar = binding.toolbarTracksList
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        val navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        NavigationUI.setupWithNavController(toolbar, navController)

        val recyclerView = binding.trackRecycler
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = TracksPagedAdapter(this)
        recyclerView.adapter = adapter
        val layoutAnimation = AnimationUtils.loadLayoutAnimation(
            requireContext(),
            R.anim.track_item_layout_anim
        )
        viewModel.tracksLiveData.observe(viewLifecycleOwner) { tracks ->
            adapter.submitList(tracks)
            recyclerView.layoutAnimation = layoutAnimation
        }
        recyclerView.doOnPreDraw {
            startPostponedEnterTransition()
        }
        postponeEnterTransition()

        binding.tracksTimeSpinner.onSelection {
            if (spinnersCanClicked)
                when (it) {
                    FILTER_TODAY -> viewModel.updateTracks(getTodayRange())
                    FILTER_WEEK -> viewModel.updateTracks(getWeekRange())
                    FILTER_MONTH -> viewModel.updateTracks(getMonthRange())
                    FILTER_SELECT -> selectDatesRange(childFragmentManager) { dates ->
                        viewModel.updateTracks(dates)
                    }
                    else -> viewModel.updateTracks(getTodayRange())
                }
        }
        requireActivity().window.decorView.postDelayed({ spinnersCanClicked = true }, 500)

        return binding.root
    }

    fun onLongClick() {
        if (actionMode == null) actionMode = toolbar.startActionMode(callback)
        else actionMode?.finish()
    }

    private val callback = object : ActionMode.Callback {

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.track_group_actions, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = false

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.menu_track_delete -> {
                    viewModel.deleteTracks(adapter.selectedIds)
                    actionMode?.finish()
                }
                R.id.menu_track_tag -> {
                    TagSelectionDialog { tag ->
                        if (tag != null) viewModel.setTag(tag, adapter.selectedIds)
                        actionMode?.finish()
                    }.show(childFragmentManager, "")
                }
            }
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            adapter.disableCheckMode()
            actionMode = null
        }
    }
}

private val FILTER_TODAY = 0
private val FILTER_WEEK = 1
private val FILTER_MONTH = 2
private val FILTER_SELECT = 3