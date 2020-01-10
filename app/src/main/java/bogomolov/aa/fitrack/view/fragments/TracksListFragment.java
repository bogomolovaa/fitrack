package bogomolov.aa.fitrack.view.fragments;


import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.Spinner;

import javax.inject.Inject;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.dagger.ViewModelFactory;
import bogomolov.aa.fitrack.databinding.FragmentTracksListBinding;
import bogomolov.aa.fitrack.core.DateUtils;
import bogomolov.aa.fitrack.core.model.Tag;
import bogomolov.aa.fitrack.view.TagResultListener;
import bogomolov.aa.fitrack.view.TracksPagedAdapter;
import bogomolov.aa.fitrack.viewmodels.TracksListViewModel;
import dagger.android.support.AndroidSupportInjection;


public class TracksListFragment extends Fragment implements TagResultListener {
    private TracksPagedAdapter adapter;
    private ActionMode actionMode;
    private Toolbar toolbar;

    @Inject
    ViewModelFactory viewModelFactory;

    private TracksListViewModel viewModel;
    private boolean spinnersCanClicked;


    private static final int FILTER_TODAY = 0;
    private static final int FILTER_WEEK = 1;
    private static final int FILTER_MONTH = 2;
    private static final int FILTER_SELECT = 3;

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(TracksListViewModel.class);
        FragmentTracksListBinding fragmentTracksListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_tracks_list, container, false);
        fragmentTracksListBinding.setViewModel(viewModel);
        fragmentTracksListBinding.setLifecycleOwner(this);
        View view = fragmentTracksListBinding.getRoot();

        toolbar = view.findViewById(R.id.toolbar_tracks_list);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(toolbar, navController);

        RecyclerView recyclerView = view.findViewById(R.id.track_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (viewModel.tracksLiveData.getValue() == null)
            viewModel.updateTracks(DateUtils.getTodayRange());

        adapter = new TracksPagedAdapter(this);
        recyclerView.setAdapter(adapter);

        viewModel.tracksLiveData.observe(this, tracks -> {
            Log.i("test","loaded "+tracks.size());
            adapter.submitList(tracks);
            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(TracksListFragment.this.getContext(), R.anim.track_item_layout_anim);
            recyclerView.setLayoutAnimation(animation);
        });


        Spinner filterSpinner = view.findViewById(R.id.tracks_time_spinner);
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!spinnersCanClicked) return;
                switch (i) {
                    case FILTER_TODAY:
                        viewModel.updateTracks(DateUtils.getTodayRange());
                        break;
                    case FILTER_WEEK:
                        viewModel.updateTracks(DateUtils.getWeekRange());
                        break;
                    case FILTER_MONTH:
                        viewModel.updateTracks(DateUtils.getMonthRange());
                        break;
                    case FILTER_SELECT:
                        DateUtils.selectDatesRange(getChildFragmentManager(), dates -> viewModel.updateTracks(dates));
                        break;
                    default:
                        viewModel.updateTracks(DateUtils.getTodayRange());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        getActivity().getWindow().getDecorView().postDelayed(() -> spinnersCanClicked = true, 500);


        return view;
    }

    public void onLongClick() {
        if (actionMode == null)
            actionMode = toolbar.startActionMode(callback);
        else
            actionMode.finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        //adapter.notifyDataSetChanged();
    }

    private ActionMode.Callback callback = new ActionMode.Callback() {

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.track_group_actions, menu);
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_track_delete:
                    Log.i("test","delete ids "+adapter.getSelectedIds());
                    viewModel.deleteTracks(adapter.getSelectedIds());
                    actionMode.finish();
                    break;
                case R.id.menu_track_tag:
                    TagSelectionDialog dialog = new TagSelectionDialog();
                    dialog.setTagResultListener(TracksListFragment.this);
                    dialog.show(getChildFragmentManager(), "TagSelectionDialog");
                    break;
            }
            return true;
        }

        public void onDestroyActionMode(ActionMode mode) {
            if(adapter!=null) adapter.disableCheckMode();
            actionMode = null;
        }

    };

    @Override
    public void onTagSelectionResult(Tag tag) {
        if (tag != null) viewModel.setTag(tag, adapter.getSelectedIds());
        if(actionMode!=null) actionMode.finish();
    }

}
