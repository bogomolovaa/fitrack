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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

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
import bogomolov.aa.fitrack.model.DateUtils;
import bogomolov.aa.fitrack.model.Track;
import bogomolov.aa.fitrack.view.StatsView;
import bogomolov.aa.fitrack.viewmodels.StatsViewModel;
import dagger.android.support.AndroidSupportInjection;

import static bogomolov.aa.fitrack.model.DateUtils.getMonthRange;
import static bogomolov.aa.fitrack.model.DateUtils.getTodayRange;
import static bogomolov.aa.fitrack.model.DateUtils.getWeekRange;


public class StatsFragment extends Fragment implements StatsView {
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


        viewModel = ViewModelProviders.of(this,viewModelFactory).get(StatsViewModel.class);
        FragmentStatsBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_stats, container, false);
        binding.setLifecycleOwner(this);
        View view = binding.getRoot();
        binding.setViewModel(viewModel);

        chart = view.findViewById(R.id.chart);

        Toolbar toolbar = view.findViewById(R.id.toolbar_stats);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(toolbar, navController);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_stats);

        Spinner tagSpinner = view.findViewById(R.id.stats_spinner_tag);

        Spinner periodSpinner = view.findViewById(R.id.stats_spinner_period);
        periodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, final int i, long l) {
                switch (i) {
                    case FILTER_TODAY:
                        viewModel.setTimeFilter(getTodayRange(), i,StatsFragment.this);
                        break;
                    case FILTER_WEEK:
                        viewModel.setTimeFilter(getWeekRange(), i,StatsFragment.this);
                        break;
                    case FILTER_MONTH:
                        viewModel.setTimeFilter(getMonthRange(), i,StatsFragment.this);
                        break;
                    case FILTER_SELECT:
                        DateUtils.selectDatesRange(getChildFragmentManager(), getContext(), new DateUtils.DatesSelector() {
                            @Override
                            public void onSelect(Date[] dates) {
                                viewModel.setTimeFilter(dates, i,StatsFragment.this);
                            }
                        });
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter<String> tagsArrayAdapter = new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_spinner_item, viewModel.getTagNames());
        tagSpinner.setAdapter(tagsArrayAdapter);
        tagSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                viewModel.setTagFilter(tagSpinner.getSelectedItem().toString(), StatsFragment.this);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        final Spinner paramSpinner = view.findViewById(R.id.stats_spinner_param);
        final Spinner timeStepSpinner = view.findViewById(R.id.stats_spinner_time_step);

        paramSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                viewModel.setParam(i,StatsFragment.this);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter arrayAdapter = new ArrayAdapter<CharSequence>(getContext(), R.layout.support_simple_spinner_dropdown_item, getResources().getStringArray(R.array.stats_filter_time_step)) {
            @Override
            public boolean isEnabled(int position) {
                if (periodSpinner.getSelectedItemPosition() == FILTER_TODAY || periodSpinner.getSelectedItemPosition() == FILTER_WEEK) {
                    return position == TIME_STEP_DAY;
                }
                return true;
            }
        };
        timeStepSpinner.setAdapter(arrayAdapter);
        timeStepSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                viewModel.setTimeStep(i, StatsFragment.this);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.updateView(this);
    }

    private double getParamValue(Track track, int param) {
        if (track == null) return 0;
        switch (param) {
            case PARAM_DISTANCE:
                return track.getDistance() / 1000.0;
            case PARAM_SPEED:
                return 3.6 * track.getSpeed();
            case PARAM_TIME:
                return (track.getEndTime() - track.getStartTime()) / (3600 * 1000.0);
        }
        return 0;
    }

    @Override
    public void updateView(Date[] datesRange, List<Track> tracks, int selectedParam, int selectedTimeStep, int selectedTimeFilter) {
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
            sumTracks.add(dayTracks.size() > 0 ? Track.sumTracks(dayTracks) : null);
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        for (int i = 0; i < sumTracks.size(); i++) {
            Track sumTrack = sumTracks.get(i);
            entries.add(new BarEntry(i, (float) getParamValue(sumTrack, selectedParam)));
            calendar.setTime(dates.get(i));
            if (selectedTimeFilter == FILTER_TODAY) {
                categories.add("today");
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
