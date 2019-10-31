package bogomolov.aa.fitrack.view.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.model.DbProvider;
import bogomolov.aa.fitrack.model.Tag;
import bogomolov.aa.fitrack.model.Track;
import bogomolov.aa.fitrack.view.TagResultListener;
import bogomolov.aa.fitrack.view.TagSelectionDialog;

public class TrackViewActivity extends AppCompatActivity implements OnMapReadyCallback {
    private TextView textTag;
    private DbProvider dbProvider;
    private Track track;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_view);

        dbProvider = new DbProvider(false);
        long trackId = getIntent().getLongExtra("track", 0);
        track = dbProvider.getTrack(trackId);


        Toolbar toolbar = findViewById(R.id.toolbar_track_view);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        TextView textDistance = findViewById(R.id.track_text_distance);
        TextView textTime = findViewById(R.id.track_text_time);
        TextView textSpeed = findViewById(R.id.track_text_avg_speed);
        final TextView textTag = findViewById(R.id.track_text_tag);

        textTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TagSelectionDialog dialog = new TagSelectionDialog();
                dialog.setTagResultListener(new TagResultListener() {
                    @Override
                    public void onTagSelectionResult(Tag tag) {
                        if (tag != null) textTag.setTag(tag.getName());
                    }
                });
                dialog.show(getSupportFragmentManager(), "dialog");
            }
        });

        setTitle(track.getName());
        textDistance.setText((int) track.getDistance() + " m");
        textTime.setText(track.getTimeString());
        textSpeed.setText(String.format("%.1f", 3.6 * track.getSpeed()) + " km/h");
        textTag.setText(track.getTag() != null ? track.getTag() : getResources().getString(R.string.no_tag));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


}
