package bogomolov.aa.fitrack.core.model;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class TrackTest {

    @Test
    public void sumTracks() {
        Track track1 = new Track();
        track1.setDistance(100);
        Track track2 = new Track();
        track2.setDistance(100);
        Track sumTrack = Track.Companion.sumTracks(Arrays.asList(track1, track2));
        assertEquals(sumTrack.getDistance(), 200,0);
    }
}