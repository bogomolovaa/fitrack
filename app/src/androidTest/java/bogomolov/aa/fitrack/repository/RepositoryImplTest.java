package bogomolov.aa.fitrack.repository;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import bogomolov.aa.fitrack.core.model.Track;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class RepositoryImplTest {
    private Repository repository;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        AppDatabase db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        repository = new RepositoryImpl(db);
    }

    @After
    public void closeDb() {
        repository.close();
    }

    @Test
    public void testLastTrack(){
        Track track = new Track();
        repository.addTrack(track);
        Track lastTrack = repository.getLastTrack();
        assertThat(track.getId(), equalTo(lastTrack.getId()));
    }


}