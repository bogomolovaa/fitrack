package bogomolov.aa.fitrack.repository.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PointEntity (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var time: Long = 0,
    var lat: Double = 0.0,
    var lng: Double = 0.0,
    var smoothed: Int = 0
)