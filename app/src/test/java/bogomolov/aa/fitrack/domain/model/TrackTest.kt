package bogomolov.aa.fitrack.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class TrackTest {

    @Test
    fun sumTracks() {
        val track1 = Track()
        track1.distance = 100.0
        val track2 = Track()
        track2.distance = 100.0
        val sumTrack = sumTracks(Arrays.asList(track1, track2))
        assertEquals(sumTrack.distance, 200.0, 0.0)
    }
}