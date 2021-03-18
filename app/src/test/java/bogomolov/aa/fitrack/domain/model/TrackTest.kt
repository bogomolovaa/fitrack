package bogomolov.aa.fitrack.domain.model

import bogomolov.aa.fitrack.domain.douglasPeucker
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

    @Test
    fun test_RamerDouglasPeucker() {
        val points = listOf(
            Point(id = 3, time = 1615633852451, lat = 55.7441048, lng = 37.6522096, smoothed = 0),
            Point(id = 6, time = 1615633863170, lat = 55.7440914, lng = 37.6521697, smoothed = 0),
            Point(id = 7, time = 1615633872600, lat = 55.7440589, lng = 37.6521634, smoothed = 0),
            Point(id = 8, time = 1615633877686, lat = 55.744073, lng = 37.6521608, smoothed = 0),
            Point(id = 9, time = 1615633887721, lat = 55.7440911, lng = 37.6521792, smoothed = 0)
        )
        val smoothed = douglasPeucker(points, EPSILON)
        for(point in smoothed) println(point)
    }
}