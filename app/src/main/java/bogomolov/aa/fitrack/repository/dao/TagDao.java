package bogomolov.aa.fitrack.repository.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import bogomolov.aa.fitrack.core.model.Track;
import bogomolov.aa.fitrack.repository.entities.TagEntity;

@Dao
public interface TagDao {

    @Insert
    long insert(TagEntity tagEntity);

    @Query("SELECT * from TagEntity")
    List<TagEntity> getTags();

}
