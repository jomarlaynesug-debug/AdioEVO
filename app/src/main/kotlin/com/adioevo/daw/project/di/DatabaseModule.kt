package com.adioevo.daw.project.di

import android.content.Context
import androidx.room.Room
import com.adioevo.daw.project.database.AppDatabase
import com.adioevo.daw.soundfont.database.SoundFontDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "adioevo_database"
        ).build()
    }
    
    @Provides
    @Singleton
    fun provideSoundFontDao(database: AppDatabase): SoundFontDao {
        return database.soundFontDao()
    }
}
