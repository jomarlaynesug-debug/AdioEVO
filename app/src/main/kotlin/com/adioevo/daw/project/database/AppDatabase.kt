package com.adioevo.daw.project.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.adioevo.daw.project.model.ProjectEntity
import com.adioevo.daw.project.model.TrackEntity
import com.adioevo.daw.soundfont.database.SoundFontDao
import com.adioevo.daw.soundfont.model.SoundFontEntity

@Database(
    entities = [
        ProjectEntity::class,
        TrackEntity::class,
        SoundFontEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun trackDao(): TrackDao
    abstract fun soundFontDao(): SoundFontDao
}
