package com.adioevo.daw.project.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.adioevo.daw.soundfont.database.SoundFontDao
import com.adioevo.daw.soundfont.model.SoundFontEntity

@Database(
    entities = [
        SoundFontEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun soundFontDao(): SoundFontDao
}
