package com.tapman104.mpvplayer.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ResumePositionEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun resumePositionDao(): ResumePositionDao
}
