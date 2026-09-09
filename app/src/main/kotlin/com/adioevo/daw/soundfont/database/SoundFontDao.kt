package com.adioevo.daw.soundfont.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.adioevo.daw.soundfont.model.SoundFontEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SoundFontDao {
    @Insert
    suspend fun insert(soundFont: SoundFontEntity): Long
    
    @Update
    suspend fun update(soundFont: SoundFontEntity)
    
    @Delete
    suspend fun delete(soundFont: SoundFontEntity)
    
    @Query("SELECT * FROM soundfonts WHERE id = :id")
    suspend fun getSoundFontById(id: Int): SoundFontEntity?
    
    @Query("SELECT * FROM soundfonts ORDER BY createdAt DESC")
    fun getAllSoundFonts(): Flow<List<SoundFontEntity>>
    
    @Query("SELECT * FROM soundfonts WHERE fileType = :fileType ORDER BY name")
    fun getSoundFontsByType(fileType: String): Flow<List<SoundFontEntity>>
    
    @Query("SELECT COUNT(*) FROM soundfonts")
    suspend fun getSoundFontCount(): Int
    
    @Query("DELETE FROM soundfonts WHERE id = :id")
    suspend fun deleteSoundFontById(id: Int)
}
