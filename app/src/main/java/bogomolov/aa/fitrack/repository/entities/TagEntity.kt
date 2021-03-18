package bogomolov.aa.fitrack.repository.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TagEntity (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    val name: String
)
