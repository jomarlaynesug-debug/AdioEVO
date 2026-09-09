package com.adioevo.daw.project.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val bpm: Int = 120,
    val timeSignatureNumerator: Int = 4,
    val timeSignatureDenominator: Int = 4,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tracks",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TrackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val projectId: Int,
    val trackNumber: Int,
    val name: String,
    val type: TrackType,
    val soundFontId: Int? = null,  // Reference to SoundFont
    val bankMsb: Int = 0,
    val bankLsb: Int = 0,
    val program: Int = 0,
    val midiChannel: Int = 0,
    val volume: Float = 1.0f,  // 0.0 - 2.0
    val pan: Float = 0.0f,     // -1.0 (left) to 1.0 (right)
    val isMuted: Boolean = false,
    val isSolo: Boolean = false,
    val isRecordArmed: Boolean = false,
    val sustainMode: String = "NORMAL",  // NORMAL or TIMED_SUSTAIN
    val timedSustainDuration: Long = 5000L,  // milliseconds
    val reverbSend: Float = 0.0f,
    val chorusSend: Float = 0.0f
)

enum class TrackType {
    MIDI,
    AUDIO,
    INSTRUMENT
}

data class Track(
    val id: Int,
    val projectId: Int,
    val trackNumber: Int,
    val name: String,
    val type: TrackType,
    val soundFontId: Int?,
    val bankMsb: Int,
    val bankLsb: Int,
    val program: Int,
    val midiChannel: Int,
    val volume: Float,
    val pan: Float,
    val isMuted: Boolean,
    val isSolo: Boolean,
    val isRecordArmed: Boolean,
    val sustainMode: String,
    val timedSustainDuration: Long,
    val reverbSend: Float,
    val chorusSend: Float
) {
    companion object {
        fun fromEntity(entity: TrackEntity): Track = Track(
            id = entity.id,
            projectId = entity.projectId,
            trackNumber = entity.trackNumber,
            name = entity.name,
            type = entity.type,
            soundFontId = entity.soundFontId,
            bankMsb = entity.bankMsb,
            bankLsb = entity.bankLsb,
            program = entity.program,
            midiChannel = entity.midiChannel,
            volume = entity.volume,
            pan = entity.pan,
            isMuted = entity.isMuted,
            isSolo = entity.isSolo,
            isRecordArmed = entity.isRecordArmed,
            sustainMode = entity.sustainMode,
            timedSustainDuration = entity.timedSustainDuration,
            reverbSend = entity.reverbSend,
            chorusSend = entity.chorusSend
        )
    }
    
    fun toEntity(): TrackEntity = TrackEntity(
        id = id,
        projectId = projectId,
        trackNumber = trackNumber,
        name = name,
        type = type,
        soundFontId = soundFontId,
        bankMsb = bankMsb,
        bankLsb = bankLsb,
        program = program,
        midiChannel = midiChannel,
        volume = volume,
        pan = pan,
        isMuted = isMuted,
        isSolo = isSolo,
        isRecordArmed = isRecordArmed,
        sustainMode = sustainMode,
        timedSustainDuration = timedSustainDuration,
        reverbSend = reverbSend,
        chorusSend = chorusSend
    )
}
