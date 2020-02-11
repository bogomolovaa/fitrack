package bogomolov.aa.fitrack.repository

import android.content.Context

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import bogomolov.aa.fitrack.core.model.Track

import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class RepositoryImplTest {
    private var repository: Repository? = null

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = RepositoryImpl(db)
    }

    @After
    fun closeDb() {
        repository!!.close()
    }

    @Test
    fun testLastTrack() {
        val track = Track()
        repository!!.addTrack(track)
        val lastTrack = repository!!.getLastTrack()
        assertThat(track.id, equalTo(lastTrack!!.id))
    }


}