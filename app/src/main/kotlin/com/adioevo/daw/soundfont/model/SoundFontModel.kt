package com.adioevo.daw.soundfont.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "soundfonts")
data class SoundFontEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val filePath: String,
    val fileType: String,  // "sf2" or "sf3"
    val fileSize: Long,
    val presetCount: Int = 0,
    val isLoaded: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class SoundFontPreset(
    val soundFontId: Int,
    val bank: Int,
    val program: Int,
    val presetName: String,
    val instrumentName: String = ""
)

data class SoundFontInstrument(
    val presetId: String,  // "soundfontId:bank:program"
    val name: String,
    val bank: Int,
    val program: Int,
    val sampleData: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SoundFontInstrument

        if (presetId != other.presetId) return false
        if (name != other.name) return false
        if (bank != other.bank) return false
        if (program != other.program) return false
        if (sampleData != null) {
            if (other.sampleData == null) return false
            if (!sampleData.contentEquals(other.sampleData)) return false
        } else if (other.sampleData != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = presetId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + bank
        result = 31 * result + program
        result = 31 * result + (sampleData?.contentHashCode() ?: 0)
        return result
    }
}
