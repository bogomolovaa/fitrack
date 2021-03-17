package bogomolov.aa.fitrack.repository

import androidx.room.Database
import androidx.room.RoomDatabase

import bogomolov.aa.fitrack.repository.dao.PointDao
import bogomolov.aa.fitrack.repository.dao.TagDao
import bogomolov.aa.fitrack.repository.dao.TrackDao
import bogomolov.aa.fitrack.repository.entities.PointEntity
import bogomolov.aa.fitrack.repository.entities.TagEntity
import bogomolov.aa.fitrack.repository.entities.TrackEntity

const val DB_NAME = "fitrack_db"

@Database(entities = [PointEntity::class, TagEntity::class, TrackEntity::class], version = 21)
abstract class AppDatabase : RoomDatabase() {

    abstract fun pointDao(): PointDao

    abstract fun tagDao(): TagDao

    abstract fun trackDao(): TrackDao
}
