package bogomolov.aa.fitrack.repository.dao;

import androidx.room.Dao;
import androidx.room.Insert;

import bogomolov.aa.fitrack.repository.entities.PointEntity;

@Dao
public interface PointDao {

    @Insert
    long insert(PointEntity pointEntity);
}
