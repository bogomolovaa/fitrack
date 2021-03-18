package bogomolov.aa.fitrack.repository.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TrackEntity (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var startPointId: Long = 0,
    var endPointId: Long = 0,
    var startSmoothedPointId: Long = 0,
    var endSmoothedPointId: Long = 0,
    var startTime: Long = 0,
    var endTime: Long = 0,
    var tag: String? = null,
    var distance: Double = 0.0
)
