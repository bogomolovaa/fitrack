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
import android.widget.AdapterView;
import android.widget.Spinner;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.dagger.ViewModelFactory;
import bogomolov.aa.fitrack.databinding.FragmentTracksListBinding;
import bogomolov.aa.fitrack.model.DateUtils;
import bogomolov.aa.fitrack.model.Tag;
import bogomolov.aa.fitrack.model.Track;
import bogomolov.aa.fitrack.view.TagResultListener;
import bogomolov.aa.fitrack.view.TagSelectionDialog;
import bogomolov.aa.fitrack.view.TracksListView;
import bogomolov.aa.fitrack.view.TracksRecyclerAdapter;
import bogomolov.aa.fitrack.viewmodels.TracksListViewModel;
import dagger.android.support.AndroidSupportInjection;


public class TracksListFragment extends Fragment implements TagResultListener, TracksListView {
    private TracksRecyclerAdapter adapter;
    private ActionMode actionMode;
    private Toolbar toolbar;

    @Inject
    ViewModelFactory viewModelFactory;

    private TracksListViewModel viewModel;

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
        viewModel = ViewModelProviders.of(this,viewModelFactory).get(TracksListViewModel.class);
        FragmentTracksListBinding fragmentTracksListBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_tracks_list,container,false);
        fragmentTracksListBinding.setViewModel(viewModel);
        fragmentTracksListBinding.setLifecycleOwner(this);
        View view = fragmentTracksListBinding.getRoot();

        toolbar = view.findViewById(R.id.toolbar_tracks_list);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(toolbar, navController);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_tracks);



        Spinner filterSpinner = view.findViewById(R.id.tracks_time_spinner);
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case FILTER_TODAY:
                        viewModel.onTimeFilterSelect(DateUtils.getTodayRange(),TracksListFragment.this);
                        break;
                    case FILTER_WEEK:
                        viewModel.onTimeFilterSelect(DateUtils.getWeekRange(),TracksListFragment.this);
                        break;
                    case FILTER_MONTH:
                        viewModel.onTimeFilterSelect(DateUtils.getMonthRange(),TracksListFragment.this);
                        break;
                    case FILTER_SELECT:
                        DateUtils.selectDatesRange(getChildFragmentManager(),getContext(), new DateUtils.DatesSelector() {
                            @Override
                            public void onSelect(Date[] dates) {
                                viewModel.onTimeFilterSelect(dates,TracksListFragment.this);
                            }
                        });
                        break;
                    default:
                        viewModel.onTimeFilterSelect(DateUtils.getTodayRange(),TracksListFragment.this);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



        RecyclerView recyclerView = view.findViewById(R.id.track_recycler);
        adapter = new TracksRecyclerAdapter(this,getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        viewModel.onTimeFilterSelect(DateUtils.getTodayRange(), this);

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
        adapter.notifyDataSetChanged();
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
            Log.d("test", "item " + item.getTitle());

            switch (item.getItemId()) {
                case R.id.menu_track_delete:
                    actionMode.finish();
                    adapter.deleteTracks();
                    viewModel.deleteTracks(adapter.getSelectedIds());
                    break;
                case R.id.menu_track_tag:
                    TagSelectionDialog dialog = new TagSelectionDialog();
                    dialog.setTagResultListener(TracksListFragment.this);
                    dialog.show(getChildFragmentManager(), "dialog");
                    break;
            }
            return true;
        }

        public void onDestroyActionMode(ActionMode mode) {
            adapter.disableCheckMode();
            actionMode = null;
        }

    };

    @Override
    public void onTagSelectionResult(Tag tag) {
        viewModel.setTag(tag, adapter.getSelectedIds());
        actionMode.finish();
    }

    @Override
    public void updateTracksList(List<Track> tracks) {
        adapter.setTracks(tracks);
    }

}
