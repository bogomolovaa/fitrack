package bogomolov.aa.fitrack.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;

import java.util.List;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.model.DbProvider;
import bogomolov.aa.fitrack.model.Track;

public class ListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Toolbar toolbar = findViewById(R.id.toolbar_tracks_list);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        DbProvider dbProvider = new DbProvider(true);
        for (int i = 0; i < 5; i++) {
            Track track = new Track();
            track.setDistance(i * 1000 + 100);
            track.setStartTime(System.currentTimeMillis() - (i + 1) * 3600 * 1000);
            track.setEndTime(System.currentTimeMillis());
            dbProvider.addTrack(track);
        }
        List<Track> tracks = dbProvider.getFinishedTracks();
        dbProvider.close();
        TracksRecyclerAdapter adapter = new TracksRecyclerAdapter(tracks);
        RecyclerView recyclerView = findViewById(R.id.track_recycler);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
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
}