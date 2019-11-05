package bogomolov.aa.fitrack.view.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.dagger.AppComponent;
import bogomolov.aa.fitrack.dagger.AppModule;
import bogomolov.aa.fitrack.dagger.DaggerAppComponent;
import bogomolov.aa.fitrack.model.DateUtils;
import bogomolov.aa.fitrack.model.Tag;
import bogomolov.aa.fitrack.model.Track;
import bogomolov.aa.fitrack.presenter.StatsPresenter;
import bogomolov.aa.fitrack.view.StatsView;

import static bogomolov.aa.fitrack.model.DateUtils.getMonthRange;
import static bogomolov.aa.fitrack.model.DateUtils.getTodayRange;
import static bogomolov.aa.fitrack.model.DateUtils.getWeekRange;
import static bogomolov.aa.fitrack.view.StatsView.*;


public class StatsActivity extends AppCompatActivity implements StatsView {
    private BarChart chart;

    @Inject
    StatsPresenter statsPresenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        chart = findViewById(R.id.chart);

        AppComponent appComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();
        appComponent.injectsStatsActivity(this);


        Toolbar toolbar = findViewById(R.id.toolbar_stats);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_stats);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final Spinner tagSpinner = findViewById(R.id.stats_spinner_tag);

        final Spinner periodSpinner = findViewById(R.id.stats_spinner_period);
        periodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, final int i, long l) {
                switch (i) {
                    case FILTER_TODAY:
                        statsPresenter.setTimeFilter(getTodayRange(),i);
                        break;
                    case FILTER_WEEK:
                        statsPresenter.setTimeFilter(getWeekRange(),i);
                        break;
                    case FILTER_MONTH:
                        statsPresenter.setTimeFilter(getMonthRange(),i);
                        break;
                    case FILTER_SELECT:
                        DateUtils.selectDatesRange(StatsActivity.this, new DateUtils.DatesSelector() {
                            @Override
                            public void onSelect(Date[] dates) {
                                statsPresenter.setTimeFilter(dates,i);
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
                this, android.R.layout.simple_spinner_item, statsPresenter.getTagNames());
        tagSpinner.setAdapter(tagsArrayAdapter);
        tagSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                statsPresenter.setTagFilter(tagSpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        final Spinner paramSpinner = findViewById(R.id.stats_spinner_param);
        final Spinner timeStepSpinner = findViewById(R.id.stats_spinner_time_step);

        paramSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                statsPresenter.setParam(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter arrayAdapter = new ArrayAdapter<CharSequence>(this, R.layout.support_simple_spinner_dropdown_item, getResources().getStringArray(R.array.stats_filter_time_step)) {
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
                statsPresenter.setTimeStep(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        statsPresenter.onDestroy();
    }

    @Override
    public void updateView(Date[] datesRange, List<Track> tracks, int selectedParam, int selectedTimeStep, int selectedTimeFilter) {
        String startDateString = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(datesRange[0]);
        String endDateString = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(datesRange[1]);
        TextView periodTextView = findViewById(R.id.stats_text_selected_period);
        periodTextView.setText(startDateString + " - " + endDateString);

        TextView textDistance = findViewById(R.id.stats_text_distance);
        TextView textTime = findViewById(R.id.stats_text_time);
        TextView textSpeed = findViewById(R.id.stats_text_avg_speed);

        Track sumTrack = Track.sumTracks(tracks);

        textDistance.setText((int) sumTrack.getDistance() + " m");
        textTime.setText(sumTrack.getTimeString());
        textSpeed.setText(String.format("%.1f", 3.6 * sumTrack.getSpeed()) + " km/h");

        updateChart(datesRange, tracks, selectedParam, selectedTimeStep, selectedTimeFilter);
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

    private void updateChart(Date[] datesRange, List<Track> tracks, int selectedParam, int selectedTimeStep, int selectedTimeFilter) {
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
