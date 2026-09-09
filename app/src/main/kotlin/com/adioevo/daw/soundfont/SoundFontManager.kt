package com.adioevo.daw.soundfont

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import com.adioevo.daw.soundfont.database.SoundFontDao
import com.adioevo.daw.soundfont.model.SoundFontEntity
import com.adioevo.daw.soundfont.model.SoundFontPreset
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundFontManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val soundFontDao: SoundFontDao
) {
    
    private val _soundFonts = MutableStateFlow<List<SoundFontEntity>>(emptyList())
    val soundFonts: StateFlow<List<SoundFontEntity>> = _soundFonts
    
    private val _selectedSoundFont = MutableStateFlow<SoundFontEntity?>(null)
    val selectedSoundFont: StateFlow<SoundFontEntity?> = _selectedSoundFont
    
    private val _presets = MutableStateFlow<List<SoundFontPreset>>(emptyList())
    val presets: StateFlow<List<SoundFontPreset>> = _presets
    
    private val soundFontCache = mutableMapOf<Int, SoundFontEntity>()
    private val presetCache = mutableMapOf<Int, List<SoundFontPreset>>()
    
    suspend fun importSoundFont(uri: Uri): Result<SoundFontEntity> = withContext(Dispatchers.IO) {
        try {
            val fileName = getFileName(uri) ?: return@withContext Result.failure(Exception("Invalid file name"))
            val fileType = getFileExtension(fileName)
            
            if (fileType !in listOf("sf2", "sf3")) {
                return@withContext Result.failure(Exception("Unsupported file type: $fileType"))
            }
            
            val destinationDir = File(context.filesDir, "soundfonts")
            destinationDir.mkdirs()
            val destinationFile = File(destinationDir, fileName)
            
            // Copy file from URI to app storage
            context.contentResolver.openInputStream(uri)?.use { input ->
                destinationFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(Exception("Failed to read file"))
            
            val fileSize = destinationFile.length()
            val presets = extractPresets(destinationFile, fileType)
            
            val soundFont = SoundFontEntity(
                name = fileName.substringBefore('.'),
                filePath = destinationFile.absolutePath,
                fileType = fileType,
                fileSize = fileSize,
                presetCount = presets.size
            )
            
            val id = soundFontDao.insert(soundFont).toInt()
            val inserted = soundFont.copy(id = id)
            soundFontCache[id] = inserted
            presetCache[id] = presets
            
            _soundFonts.value = _soundFonts.value + inserted
            
            Timber.d("Imported SoundFont: $fileName ($fileType) with ${presets.size} presets")
            Result.success(inserted)
            
        } catch (e: Exception) {
            Timber.e(e, "Error importing SoundFont")
            Result.failure(e)
        }
    }
    
    suspend fun deleteSoundFont(id: Int) = withContext(Dispatchers.IO) {
        try {
            val soundFont = soundFontCache[id] ?: soundFontDao.getSoundFontById(id) ?: return@withContext
            
            // Delete file
            File(soundFont.filePath).delete()
            
            // Delete from database
            soundFontDao.delete(soundFont)
            
            // Clear caches
            soundFontCache.remove(id)
            presetCache.remove(id)
            
            _soundFonts.value = _soundFonts.value.filter { it.id != id }
            if (_selectedSoundFont.value?.id == id) {
                _selectedSoundFont.value = null
            }
            
            Timber.d("Deleted SoundFont: ${soundFont.name}")
        } catch (e: Exception) {
            Timber.e(e, "Error deleting SoundFont")
        }
    }
    
    suspend fun selectSoundFont(id: Int) = withContext(Dispatchers.Main) {
        val soundFont = soundFontCache[id] ?: soundFontDao.getSoundFontById(id)
        _selectedSoundFont.value = soundFont
        
        if (soundFont != null) {
            val presets = presetCache[id] ?: extractPresets(
                File(soundFont.filePath),
                soundFont.fileType
            )
            _presets.value = presets
            presetCache[id] = presets
        }
    }
    
    suspend fun renameSoundFont(id: Int, newName: String) = withContext(Dispatchers.IO) {
        try {
            val soundFont = soundFontCache[id] ?: soundFontDao.getSoundFontById(id) ?: return@withContext
            val updated = soundFont.copy(name = newName)
            
            soundFontDao.update(updated)
            soundFontCache[id] = updated
            
            _soundFonts.value = _soundFonts.value.map { if (it.id == id) updated else it }
            if (_selectedSoundFont.value?.id == id) {
                _selectedSoundFont.value = updated
            }
            
            Timber.d("Renamed SoundFont: ${soundFont.name} → $newName")
        } catch (e: Exception) {
            Timber.e(e, "Error renaming SoundFont")
        }
    }
    
    fun getPresetsForSoundFont(id: Int): List<SoundFontPreset> =
        presetCache[id] ?: emptyList()
    
    private fun extractPresets(file: File, fileType: String): List<SoundFontPreset> {
        // Placeholder: Parse SF2/SF3 file structure
        // Real implementation would parse binary SoundFont headers
        return listOf(
            SoundFontPreset(
                soundFontId = 0,
                bank = 0,
                program = 0,
                presetName = "Default"
            )
        )
    }
    
    private fun getFileName(uri: Uri): String? {
        return uri.lastPathSegment?.substringAfterLast('/')
    }
    
    private fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast('.').lowercase()
    }
}
