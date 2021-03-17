package bogomolov.aa.fitrack.repository.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class TagEntity {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var name: String? = null

    override fun toString(): String {
        return name ?: ""
    }
}
