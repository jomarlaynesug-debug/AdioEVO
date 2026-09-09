package com.adioevo.daw.midi.sustain

import com.adioevo.daw.midi.voice.MidiVoiceManager
import com.adioevo.daw.midi.voice.SustainManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

data class SustainPedalState(
    val isPhysicalSustainActive: Boolean = false,
    val isTouchscreenSustainActive: Boolean = false,
    val sustainedNotes: Set<Int> = emptySet()
) {
    val isSustainEffectivelyActive: Boolean
        get() = isPhysicalSustainActive || isTouchscreenSustainActive
}

@Singleton
class NormalSustainEngine @Inject constructor(
    private val sustainManager: SustainManager,
    private val voiceManager: MidiVoiceManager
) {
    private val _sustain PedalState = MutableStateFlow(SustainPedalState())
    val sustainPedalState: StateFlow<SustainPedalState> = _sustainPedalState
    
    fun handleControlChange(cc: Int, value: Int, channel: Int) {
        if (cc != CC64_SUSTAIN_PEDAL) return
        
        val isSustainOn = value >= 64
        
        _sustainPedalState.value = _sustainPedalState.value.copy(
            isPhysicalSustainActive = isSustainOn
        )
        
        sustainManager.handleControlChange(cc, value)
        
        if (isSustainOn) {
            Timber.d("Sustain Pedal ON (CC64 value=$value)")
        } else {
            Timber.d("Sustain Pedal OFF (CC64 value=$value)")
            releaseSustainedNotes(channel)
        }
    }
    
    fun enableTouchscreenSustain() {
        _sustainPedalState.value = _sustainPedalState.value.copy(
            isTouchscreenSustainActive = true
        )
        sustainManager.handleControlChange(CC64_SUSTAIN_PEDAL, 127)
        Timber.d("Touchscreen Sustain enabled")
    }
    
    fun disableTouchscreenSustain(channel: Int) {
        _sustainPedalState.value = _sustainPedalState.value.copy(
            isTouchscreenSustainActive = false
        )
        sustainManager.handleControlChange(CC64_SUSTAIN_PEDAL, 0)
        releaseSustainedNotes(channel)
        Timber.d("Touchscreen Sustain disabled")
    }
    
    private fun releaseSustainedNotes(channel: Int) {
        val sustainedNotes = sustainManager.getSustainedNotes().toList()
        
        for (note in sustainedNotes) {
            voiceManager.noteOff(note, channel)
            sustainManager.removeSustainedNote(note)
        }
        
        if (sustainedNotes.isNotEmpty()) {
            Timber.d("Released ${sustainedNotes.size} sustained notes on channel $channel")
        }
        
        _sustainPedalState.value = _sustainPedalState.value.copy(
            sustainedNotes = emptySet()
        )
    }
    
    fun addToSustain(note: Int) {
        if (!_sustainPedalState.value.isSustainEffectivelyActive) return
        
        sustainManager.addSustainedNote(note)
        val current = _sustainPedalState.value
        _sustainPedalState.value = current.copy(
            sustainedNotes = current.sustainedNotes + note
        )
        Timber.d("Note $note added to sustain pedal")
    }
    
    fun isSustainActive(): Boolean = _sustainPedalState.value.isSustainEffectivelyActive
    
    fun getSustainedNotes(): Set<Int> = sustainManager.getSustainedNotes()
    
    companion object {
        const val CC64_SUSTAIN_PEDAL = 64
    }
}
