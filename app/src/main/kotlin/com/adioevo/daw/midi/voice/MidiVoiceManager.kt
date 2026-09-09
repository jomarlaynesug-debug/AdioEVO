package com.adioevo.daw.midi.voice

import timber.log.Timber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class Voice(
    val voiceId: Int,
    val note: Int = 0,
    val velocity: Int = 0,
    val channel: Int = 0,
    val isActive: Boolean = false,
    val pitchBend: Float = 0f,
    val pressure: Int = 0
)

@Singleton
class MidiVoiceManager @Inject constructor() {
    companion object {
        private const val MAX_VOICES = 128  // Max polyphony
    }
    
    private val voices = Array(MAX_VOICES) { id ->
        Voice(voiceId = id, isActive = false)
    }
    
    private val _activeVoices = MutableStateFlow<List<Voice>>(emptyList())
    val activeVoices: StateFlow<List<Voice>> = _activeVoices
    
    private val voiceAllocationMap = mutableMapOf<String, Int>() // "channel:note" -> voiceId
    
    fun noteOn(note: Int, velocity: Int, channel: Int, timestamp: Long = 0L): Voice? {
        if (velocity == 0) {
            return noteOff(note, channel, timestamp)
        }
        
        // Check if note is already active on this channel
        val key = "$channel:$note"
        val existingVoiceId = voiceAllocationMap[key]
        if (existingVoiceId != null) {
            Timber.d("Retriggering note $note on channel $channel with voice $existingVoiceId")
            return updateVoice(existingVoiceId, note, velocity, channel)
        }
        
        // Find first available voice
        val voiceId = voices.indexOfFirst { !it.isActive }
        if (voiceId == -1) {
            Timber.w("No available voices for note $note on channel $channel (polyphony limit reached)")
            return null
        }
        
        voices[voiceId] = Voice(
            voiceId = voiceId,
            note = note,
            velocity = velocity,
            channel = channel,
            isActive = true
        )
        
        voiceAllocationMap[key] = voiceId
        updateActiveVoicesList()
        
        Timber.d("Voice ON: voice=$voiceId note=$note velocity=$velocity channel=$channel")
        return voices[voiceId]
    }
    
    fun noteOff(note: Int, channel: Int, timestamp: Long = 0L): Voice? {
        val key = "$channel:$note"
        val voiceId = voiceAllocationMap[key] ?: return null
        
        voices[voiceId] = voices[voiceId].copy(isActive = false)
        voiceAllocationMap.remove(key)
        updateActiveVoicesList()
        
        Timber.d("Voice OFF: voice=$voiceId note=$note channel=$channel")
        return voices[voiceId]
    }
    
    fun pitchBend(value: Float, channel: Int) {
        voices.filter { it.isActive && it.channel == channel }
            .forEach { voice ->
                voices[voice.voiceId] = voice.copy(pitchBend = value)
            }
        updateActiveVoicesList()
    }
    
    fun aftertouch(note: Int, pressure: Int, channel: Int) {
        val key = "$channel:$note"
        val voiceId = voiceAllocationMap[key] ?: return
        
        voices[voiceId] = voices[voiceId].copy(pressure = pressure)
        updateActiveVoicesList()
    }
    
    fun allNotesOff(channel: Int? = null) {
        val channelToOff = channel
        
        voices.indices.forEach { id ->
            if (voices[id].isActive) {
                if (channelToOff == null || voices[id].channel == channelToOff) {
                    voices[id] = voices[id].copy(isActive = false)
                }
            }
        }
        
        voiceAllocationMap.entries.removeAll { (key, _) ->
            if (channelToOff == null) true
            else key.startsWith("$channelToOff:")
        }
        
        updateActiveVoicesList()
        Timber.d("All Notes Off for channel: $channelToOff")
    }
    
    fun allSoundOff(channel: Int? = null) {
        // Identical to allNotesOff but sent via MIDI CC120
        allNotesOff(channel)
        Timber.d("All Sound Off for channel: $channelToOff")
    }
    
    fun getActiveVoiceCount(): Int = voices.count { it.isActive }
    
    fun getVoicesForChannel(channel: Int): List<Voice> =
        voices.filter { it.isActive && it.channel == channel }
    
    fun getVoicesForNote(note: Int, channel: Int): List<Voice> =
        voices.filter { it.isActive && it.channel == channel && it.note == note }
    
    private fun updateVoice(voiceId: Int, note: Int, velocity: Int, channel: Int): Voice {
        voices[voiceId] = Voice(
            voiceId = voiceId,
            note = note,
            velocity = velocity,
            channel = channel,
            isActive = true
        )
        updateActiveVoicesList()
        return voices[voiceId]
    }
    
    private fun updateActiveVoicesList() {
        _activeVoices.value = voices.filter { it.isActive }
    }
}
