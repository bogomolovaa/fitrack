package bogomolov.aa.fitrack.repository.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

import bogomolov.aa.fitrack.repository.entities.TagEntity

@Dao
interface TagDao {

    @get:Query("SELECT * from TagEntity")
    val tags: List<TagEntity>

    @Insert
    fun insert(tagEntity: TagEntity): Long

    @Delete
    fun delete(tagEntity: TagEntity)

}
