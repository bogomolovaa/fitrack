package bogomolov.aa.fitrack.domain

import bogomolov.aa.fitrack.domain.model.Point
import bogomolov.aa.fitrack.domain.model.Track

interface MapSaver {
    suspend fun save(track: Track, points: List<Point>, width: Int, height: Int)
}