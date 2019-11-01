package bogomolov.aa.fitrack.view.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.model.DateUtils;
import bogomolov.aa.fitrack.model.DbProvider;
import bogomolov.aa.fitrack.model.Tag;
import bogomolov.aa.fitrack.model.Track;
import bogomolov.aa.fitrack.view.TagResultListener;
import bogomolov.aa.fitrack.view.TagSelectionDialog;
import bogomolov.aa.fitrack.view.TracksRecyclerAdapter;

public class TracksListActivity extends AppCompatActivity implements TagResultListener {
    private RecyclerView recyclerView;
    private DbProvider dbProvider;
    private TracksRecyclerAdapter adapter;
    private ActionMode actionMode;
    private Toolbar toolbar;

    private static final int FILTER_TODAY = 0;
    private static final int FILTER_WEEK = 1;
    private static final int FILTER_MONTH = 2;
    private static final int FILTER_SELECT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks_list);
        toolbar = findViewById(R.id.toolbar_tracks_list);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        Spinner filterSpinner = findViewById(R.id.tracks_time_spinner);
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case FILTER_TODAY:
                        updateTracksList(DateUtils.getTodayRange());
                        break;
                    case FILTER_WEEK:
                        updateTracksList(DateUtils.getWeekRange());
                        break;
                    case FILTER_MONTH:
                        updateTracksList(DateUtils.getMonthRange());
                        break;
                    case FILTER_SELECT:
                        DateUtils.selectDatesRange(TracksListActivity.this, new DateUtils.DatesSelector() {
                            @Override
                            public void onSelect(Date[] dates) {
                                updateTracksList(dates);
                            }
                        });
                        break;
                    default:
                        updateTracksList(DateUtils.getTodayRange());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        dbProvider = new DbProvider(true);
        for (int i = 0; i < 5; i++) {
            Track track = new Track();
            track.setId(i + 1000);
            track.setDistance(i * 1000 + 100);
            track.setStartTime(System.currentTimeMillis() - i * 24 * 3600 * 1000);
            track.setEndTime(System.currentTimeMillis());
            dbProvider.addTrack(track);

            Tag tag = new Tag("tag " + (i + 1));
            dbProvider.addTag(tag);
        }

        recyclerView = findViewById(R.id.track_recycler);
        adapter = new TracksRecyclerAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        updateTracksList(DateUtils.getTodayRange());

    }


    public void onLongClick() {
        if (actionMode == null)
            actionMode = toolbar.startActionMode(callback);
        else
            actionMode.finish();
    }

    @Override
    protected void onResume() {
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
                    dbProvider.deleteTracks(new ArrayList<Long>(adapter.getSelectedIds()));
                    break;
                case R.id.menu_track_tag:
                    TagSelectionDialog dialog = new TagSelectionDialog();
                    dialog.setTagResultListener(TracksListActivity.this);
                    dialog.show(getSupportFragmentManager(), "dialog");
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
        if (tag != null) {
            List<Long> ids = new ArrayList<>(adapter.getSelectedIds());
            List<Track> tracks = dbProvider.getTracks(ids);
            dbProvider.getRealm().beginTransaction();
            for (Track track : tracks) track.setTag(tag.getName());
            dbProvider.getRealm().commitTransaction();
        }
        actionMode.finish();
    }

    private void selectDatesRange() {

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


}