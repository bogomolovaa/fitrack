package bogomolov.aa.fitrack.repository.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
class PointEntity {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var time: Long = 0
    var lat: Double = 0.toDouble()
    var lng: Double = 0.toDouble()
    var smoothed: Int = 0
}
