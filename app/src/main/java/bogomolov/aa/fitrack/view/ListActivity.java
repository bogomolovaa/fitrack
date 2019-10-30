package bogomolov.aa.fitrack.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.Spinner;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.model.DbProvider;
import bogomolov.aa.fitrack.model.Track;

public class ListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DbProvider dbProvider;
    private TracksRecyclerAdapter adapter;
    private static final int FILTER_TODAY = 0;
    private static final int FILTER_WEEK = 1;
    private static final int FILTER_MONTH = 2;
    private static final int FILTER_SELECT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Toolbar toolbar = findViewById(R.id.toolbar_tracks_list);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Spinner filterSpinner = findViewById(R.id.tracks_time_spinner);
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case FILTER_TODAY:
                        updateTracksList(getTodayRange());
                        break;
                    case FILTER_WEEK:
                        updateTracksList(getWeekRange());
                        break;
                    case FILTER_MONTH:
                        updateTracksList(getMonthRange());
                        break;
                    case FILTER_SELECT:
                        selectDatesRange(new Date[2]);
                        break;
                    default:
                        updateTracksList(getTodayRange());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        dbProvider = new DbProvider(true);
        for (int i = 0; i < 5; i++) {
            Track track = new Track();
            track.setDistance(i * 1000 + 100);
            track.setStartTime(System.currentTimeMillis() - i * 24 * 3600 * 1000);
            track.setEndTime(System.currentTimeMillis());
            dbProvider.addTrack(track);
        }

        recyclerView = findViewById(R.id.track_recycler);
        adapter = new TracksRecyclerAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        updateTracksList(getTodayRange());

    }

    private void selectDatesRange(final Date[] dates) {
        new DatePickerFragment(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.YEAR, year);
                c.set(Calendar.MONTH, month);
                c.set(Calendar.DAY_OF_MONTH, day);
                if (dates[0] == null) {
                    dates[0] = c.getTime();
                    Log.i("test","date[0] "+dates[0]);
                    selectDatesRange(dates);
                } else {
                    dates[1] = c.getTime();
                    Log.i("test","date[1] "+dates[1]);
                    updateTracksList(dates);
                }
            }
        }).show(getSupportFragmentManager(), "datePicker");
    }

    private Date[] getMonthRange() {
        Date[] dateRange = new Date[2];
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        dateRange[0] = calendar.getTime();
        calendar.add(Calendar.MONTH, 1);
        dateRange[1] = calendar.getTime();
        return dateRange;
    }

    private Date[] getWeekRange() {
        Date[] dateRange = new Date[2];
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_WEEK, GregorianCalendar.MONDAY);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        dateRange[0] = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, 7);
        dateRange[1] = calendar.getTime();
        return dateRange;
    }

    private Date[] getTodayRange() {
        Date[] dateRange = new Date[2];
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        dateRange[0] = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        dateRange[1] = calendar.getTime();
        return dateRange;
    }

    private void updateTracksList(Date[] dates) {
        List<Track> tracks = dbProvider.getFinishedTracks(dates);
        adapter.setTracks(tracks);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbProvider.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);

    }

    public static class DatePickerFragment extends DialogFragment{
        private DatePickerDialog.OnDateSetListener listener;

        public DatePickerFragment(DatePickerDialog.OnDateSetListener listener) {
            this.listener = listener;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), listener, year, month, day);
        }
    }

}