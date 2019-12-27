package bogomolov.aa.fitrack.repository;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import bogomolov.aa.fitrack.repository.dao.PointDao;
import bogomolov.aa.fitrack.repository.dao.TagDao;
import bogomolov.aa.fitrack.repository.dao.TrackDao;
import bogomolov.aa.fitrack.repository.entities.PointEntity;
import bogomolov.aa.fitrack.repository.entities.TagEntity;
import bogomolov.aa.fitrack.repository.entities.TrackEntity;

@Database(entities = {PointEntity.class, TagEntity.class, TrackEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract PointDao pointDao();

    public abstract TagDao tagDao();

    public abstract TrackDao trackDao();

}
