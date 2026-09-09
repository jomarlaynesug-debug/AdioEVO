package com.adioevo.daw.midi.voice

import com.adioevo.daw.project.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

data class TrackVoiceState(
    val trackId: Int,
    val voices: List<Voice> = emptyList(),
    val activeVoiceCount: Int = 0
)

@Singleton
class MultitrackVoiceEngine @Inject constructor(
    private val voiceManager: MidiVoiceManager,
    private val sustainManager: SustainManager,
    private val timedSustainManager: TimedSustainManager
) {
    
    private val trackVoiceStates = mutableMapOf<Int, TrackVoiceState>()
    private val _trackVoiceStatesFlow = MutableStateFlow<Map<Int, TrackVoiceState>>(emptyMap())
    val trackVoiceStates: StateFlow<Map<Int, TrackVoiceState>> = _trackVoiceStatesFlow
    
    private val trackChannelMap = mutableMapOf<Int, Int>()  // trackId -> midiChannel
    private val trackSoundFontMap = mutableMapOf<Int, Int>()  // trackId -> soundFontId
    private val trackSustainModeMap = mutableMapOf<Int, String>()  // trackId -> sustainMode
    private val trackSustainDurationMap = mutableMapOf<Int, Long>()  // trackId -> durationMs
    
    fun registerTrack(track: Track) {
        trackChannelMap[track.id] = track.midiChannel
        trackSoundFontMap[track.id] = track.soundFontId ?: -1
        trackSustainModeMap[track.id] = track.sustainMode
        trackSustainDurationMap[track.id] = track.timedSustainDuration
        
        trackVoiceStates[track.id] = TrackVoiceState(trackId = track.id)
        updateTrackVoiceStatesFlow()
        
        Timber.d(
            "Registered track: ${track.name} (id=${track.id}, " +
            "channel=${track.midiChannel}, sf=${track.soundFontId})"
        )
    }
    
    fun unregisterTrack(trackId: Int) {
        trackChannelMap.remove(trackId)
        trackSoundFontMap.remove(trackId)
        trackSustainModeMap.remove(trackId)
        trackSustainDurationMap.remove(trackId)
        
        // Release all voices for this track
        val channel = trackChannelMap[trackId] ?: return
        voiceManager.allNotesOff(channel)
        
        trackVoiceStates.remove(trackId)
        updateTrackVoiceStatesFlow()
        
        Timber.d("Unregistered track: id=$trackId")
    }
    
    fun noteOnForTrack(trackId: Int, note: Int, velocity: Int) {
        val channel = trackChannelMap[trackId] ?: run {
            Timber.w("Track $trackId not registered")
            return
        }
        
        val voice = voiceManager.noteOn(note, velocity, channel)
        if (voice != null) {
            Timber.d("Track $trackId NOTE_ON: note=$note velocity=$velocity voice=${voice.voiceId}")
            updateTrackVoiceCount(trackId)
        }
    }
    
    fun noteOffForTrack(trackId: Int, note: Int) {
        val channel = trackChannelMap[trackId] ?: return
        val sustainMode = trackSustainModeMap[trackId] ?: "NORMAL"
        val sustainDuration = trackSustainDurationMap[trackId] ?: 5000L
        
        if (sustainManager.isSustainActive()) {
            sustainManager.addSustainedNote(note)
            return
        }
        
        when (sustainMode) {
            "NORMAL" -> {
                voiceManager.noteOff(note, channel)
            }
            "TIMED_SUSTAIN" -> {
                timedSustainManager.scheduleNoteRelease(note, channel) { n, ch ->
                    voiceManager.noteOff(n, ch)
                }
            }
        }
        
        Timber.d("Track $trackId NOTE_OFF: note=$note sustainMode=$sustainMode")
        updateTrackVoiceCount(trackId)
    }
    
    fun updateTrackInstrument(trackId: Int, soundFontId: Int) {
        trackSoundFontMap[trackId] = soundFontId
        Timber.d("Track $trackId instrument updated to SoundFont=$soundFontId")
    }
    
    fun updateTrackSustain(trackId: Int, sustainMode: String, durationMs: Long) {
        trackSustainModeMap[trackId] = sustainMode
        trackSustainDurationMap[trackId] = durationMs
        Timber.d(
            "Track $trackId sustain updated: mode=$sustainMode duration=${durationMs}ms"
        )
    }
    
    fun updateTrackChannel(trackId: Int, channel: Int) {
        trackChannelMap[trackId] = channel
        Timber.d("Track $trackId MIDI channel updated to $channel")
    }
    
    fun getTrackVoiceCount(trackId: Int): Int {
        val channel = trackChannelMap[trackId] ?: return 0
        return voiceManager.getVoicesForChannel(channel).size
    }
    
    fun getTrackVoices(trackId: Int): List<Voice> {
        val channel = trackChannelMap[trackId] ?: return emptyList()
        return voiceManager.getVoicesForChannel(channel)
    }
    
    fun getTotalActiveVoices(): Int = voiceManager.getActiveVoiceCount()
    
    fun releaseAllNotesForTrack(trackId: Int) {
        val channel = trackChannelMap[trackId] ?: return
        voiceManager.allNotesOff(channel)
        timedSustainManager.cancelAllScheduledReleases()
        Timber.d("Released all notes for track: $trackId")
        updateTrackVoiceCount(trackId)
    }
    
    fun panic() {
        Timber.w("PANIC: Releasing all voices on all tracks")
        voiceManager.allNotesOff()
        sustainManager.clearSustainedNotes()
        timedSustainManager.cancelAllScheduledReleases()
        
        trackVoiceStates.keys.forEach { trackId ->
            updateTrackVoiceCount(trackId)
        }
    }
    
    private fun updateTrackVoiceCount(trackId: Int) {
        val voices = getTrackVoices(trackId)
        trackVoiceStates[trackId] = TrackVoiceState(
            trackId = trackId,
            voices = voices,
            activeVoiceCount = voices.size
        )
        updateTrackVoiceStatesFlow()
    }
    
    private fun updateTrackVoiceStatesFlow() {
        _trackVoiceStatesFlow.value = trackVoiceStates.toMap()
    }
}
