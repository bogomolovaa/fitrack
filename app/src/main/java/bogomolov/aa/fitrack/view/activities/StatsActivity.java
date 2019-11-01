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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.model.DateUtils;
import bogomolov.aa.fitrack.model.DbProvider;
import bogomolov.aa.fitrack.model.Tag;
import bogomolov.aa.fitrack.model.Track;

import static bogomolov.aa.fitrack.model.DateUtils.getMonthRange;
import static bogomolov.aa.fitrack.model.DateUtils.getTodayRange;
import static bogomolov.aa.fitrack.model.DateUtils.getWeekRange;

public class StatsActivity extends AppCompatActivity {
    private static final String NO_TAG = "-";

    private LineChart chart;
    private DbProvider dbProvider;
    private List<Track> tracks;
    private Date[] datesRange;
    private String selectedTag = NO_TAG;


    private static final int PARAM_DISTANCE = 0;
    private static final int PARAM_SPEED = 1;
    private static final int PARAM_TIME = 2;

    private static final int TIME_STEP_DAY = 0;
    private static final int TIME_STEP_WEEK = 1;

    private static final int FILTER_TODAY = 0;
    private static final int FILTER_WEEK = 1;
    private static final int FILTER_MONTH = 2;
    private static final int FILTER_SELECT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        Toolbar toolbar = findViewById(R.id.toolbar_stats);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final Spinner tagSpinner = findViewById(R.id.stats_spinner_tag);

        Spinner periodSpinner = findViewById(R.id.stats_spinner_period);
        periodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case FILTER_TODAY:
                        datesRange = getTodayRange();
                        updateView();
                        break;
                    case FILTER_WEEK:
                        datesRange = getWeekRange();
                        updateView();
                        break;
                    case FILTER_MONTH:
                        datesRange = getMonthRange();
                        updateView();
                        break;
                    case FILTER_SELECT:
                        DateUtils.selectDatesRange(StatsActivity.this, new DateUtils.DatesSelector() {
                            @Override
                            public void onSelect(Date[] dates) {
                                datesRange = dates;
                                updateView();
                            }
                        });
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        List<Tag> tags = dbProvider.getTags();
        String[] tagNames = new String[tags.size() + 1];
        tagNames[0] = NO_TAG;
        for (int i = 0; i < tags.size(); i++) tagNames[i + 1] = tags.get(i).getName();
        ArrayAdapter<String> tagsArrayAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, tagNames);
        tagSpinner.setAdapter(tagsArrayAdapter);
        tagSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedTag = tagSpinner.getSelectedItem().toString();
                updateView();
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
                updateChart(i, timeStepSpinner.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        timeStepSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updateChart(paramSpinner.getSelectedItemPosition(), i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        dbProvider = new DbProvider(false);
        chart = findViewById(R.id.chart);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbProvider.close();
    }

    private void updateView() {
        String startDateString = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(datesRange[0]);
        String endDateString = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(datesRange[1]);
        TextView periodTextView = findViewById(R.id.stats_text_selected_period);
        periodTextView.setText(startDateString + " - " + endDateString);

        TextView textDistance = findViewById(R.id.stats_text_distance);
        TextView textTime = findViewById(R.id.stats_text_time);
        TextView textSpeed = findViewById(R.id.stats_text_avg_speed);

        tracks = selectedTag.equals(NO_TAG) ? dbProvider.getFinishedTracks(datesRange) : dbProvider.getFinishedTracks(datesRange, selectedTag);
        Track sumTrack = Track.sumTracks(tracks);

        textDistance.setText((int) sumTrack.getDistance() + " m");
        textTime.setText(sumTrack.getTimeString());
        textSpeed.setText(String.format("%.1f", 3.6 * sumTrack.getSpeed()) + " km/h");

        updateChart(PARAM_DISTANCE, TIME_STEP_DAY);
    }


    private void updateChart(int param, int timeStep) {


        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            entries.add(new Entry(i, i)); //x,y
        }
        LineDataSet dataSet = new LineDataSet(entries, "Label");
        dataSet.setColor(Color.RED);
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate();

        //https://weeklycoding.com/mpandroidchart-documentation/
        //https://weeklycoding.com/mpandroidchart-documentation/getting-started/

    }

}
