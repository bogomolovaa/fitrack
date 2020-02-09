package bogomolov.aa.fitrack.view.fragments;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.inject.Inject;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.dagger.ViewModelFactory;
import bogomolov.aa.fitrack.databinding.FragmentStatsBinding;
import bogomolov.aa.fitrack.core.model.Track;
import bogomolov.aa.fitrack.viewmodels.StatsViewModel;
import dagger.android.support.AndroidSupportInjection;

import static bogomolov.aa.fitrack.viewmodels.StatsViewModel.FILTER_MONTH;
import static bogomolov.aa.fitrack.viewmodels.StatsViewModel.FILTER_TODAY;
import static bogomolov.aa.fitrack.viewmodels.StatsViewModel.FILTER_WEEK;
import static bogomolov.aa.fitrack.viewmodels.StatsViewModel.PARAM_DISTANCE;
import static bogomolov.aa.fitrack.viewmodels.StatsViewModel.PARAM_SPEED;
import static bogomolov.aa.fitrack.viewmodels.StatsViewModel.PARAM_TIME;
import static bogomolov.aa.fitrack.viewmodels.StatsViewModel.TIME_STEP_DAY;
import static bogomolov.aa.fitrack.viewmodels.StatsViewModel.TIME_STEP_WEEK;


public class StatsFragment extends Fragment {


    private BarChart chart;
    private StatsViewModel viewModel;

    @Inject
    ViewModelFactory viewModelFactory;

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(StatsViewModel.class);
        FragmentStatsBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_stats, container, false);
        binding.setLifecycleOwner(this);
        View view = binding.getRoot();
        binding.setViewModel(viewModel);

        chart = binding.chart;

        Toolbar toolbar = binding.toolbarStats;
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(toolbar, navController);

        viewModel.tracksLiveData.observe(this, tracks ->
                updateView(viewModel.datesRange, tracks, viewModel.selectedParam, viewModel.selectedTimeStep, viewModel.selectedTimeFilter));


        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.stats_filters_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.stats_filters:
                new FiltersBottomSheetDialogFragment().show(getChildFragmentManager(),"Filters bottom sheet");
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.updateView(true);
    }

    private double getParamValue(Track track, int param) {
        if (track == null) return 0;
        switch (param) {
            case PARAM_DISTANCE:
                return track.getDistance() / 1000.0;
            case PARAM_SPEED:
                return track.getSpeed();
            case PARAM_TIME:
                return (track.getEndTime() - track.getStartTime()) / (60 * 1000.0);
        }
        return 0;
    }

    private void updateView(Date[] datesRange, List<Track> tracks, int selectedParam, int selectedTimeStep, int selectedTimeFilter) {
        List<Track> sumTracks = new ArrayList<>();
        List<Date> dates = new ArrayList<>();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(datesRange[0]);
        while (calendar.getTime().getTime() <= datesRange[1].getTime()) {
            Date date = calendar.getTime();
            dates.add(date);
            if (selectedTimeStep == TIME_STEP_DAY) calendar.add(Calendar.DAY_OF_YEAR, 1);
            if (selectedTimeStep == TIME_STEP_WEEK) calendar.add(Calendar.DAY_OF_YEAR, 7);
            Date toDate = calendar.getTime();
            List<Track> dayTracks = new ArrayList<>();
            for (Track track : tracks)
                if (track.getStartTime() >= date.getTime() && track.getStartTime() < toDate.getTime())
                    dayTracks.add(track);
            sumTracks.add(dayTracks.size() > 0 ? Track.Companion.sumTracks(dayTracks) : null);
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        for (int i = 0; i < sumTracks.size(); i++) {
            Track sumTrack = sumTracks.get(i);
            entries.add(new BarEntry(i, (float) getParamValue(sumTrack, selectedParam)));
            calendar.setTime(dates.get(i));
            if (selectedTimeFilter == FILTER_TODAY) {
                categories.add(getContext().getResources().getString(R.string.today));
            } else if (selectedTimeFilter == FILTER_WEEK) {
                categories.add(new DateFormatSymbols(getResources().getConfiguration().locale).getShortWeekdays()[calendar.get(Calendar.DAY_OF_WEEK)]);
            } else {
                if (selectedTimeStep == TIME_STEP_DAY) {
                    if (selectedTimeFilter == FILTER_MONTH) {
                        categories.add("" + calendar.get(Calendar.DAY_OF_MONTH));
                    } else {
                        categories.add("" + calendar.get(Calendar.DATE));
                    }
                }
                if (selectedTimeStep == TIME_STEP_WEEK) {
                    if (selectedTimeFilter == FILTER_MONTH) {
                        categories.add("" + calendar.get(Calendar.WEEK_OF_MONTH));
                    } else {
                        categories.add("" + calendar.get(Calendar.WEEK_OF_YEAR));
                    }
                }
            }
        }
        BarDataSet set = new BarDataSet(entries, getResources().getStringArray(R.array.stats_param_units)[selectedParam]);
        BarData data = new BarData(set);
        IndexAxisValueFormatter formatter = new IndexAxisValueFormatter(categories);
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(formatter);
        chart.setData(data);
        chart.setFitBars(true);
        chart.invalidate();

    }


}
