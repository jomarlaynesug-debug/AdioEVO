package com.adioevo.daw.midi.voice

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

data class SustainState(
    val isSustainActive: Boolean = false,
    val sustainedNotes: Set<Int> = emptySet()  // Set of note numbers being sustained
)

@Singleton
class SustainManager @Inject constructor() {
    private val _sustainState = MutableStateFlow(SustainState())
    val sustainState: StateFlow<SustainState> = _sustainState
    
    fun handleControlChange(cc: Int, value: Int) {
        if (cc == CC64_SUSTAIN_PEDAL) {
            val isSustainOn = value >= 64
            _sustainState.value = _sustainState.value.copy(isSustainActive = isSustainOn)
            Timber.d("Sustain Pedal: ${if (isSustainOn) "ON" else "OFF"}")
        }
    }
    
    fun addSustainedNote(note: Int) {
        val current = _sustainState.value
        _sustainState.value = current.copy(
            sustainedNotes = current.sustainedNotes + note
        )
    }
    
    fun removeSustainedNote(note: Int) {
        val current = _sustainState.value
        _sustainState.value = current.copy(
            sustainedNotes = current.sustainedNotes - note
        )
    }
    
    fun clearSustainedNotes() {
        _sustainState.value = _sustainState.value.copy(
            sustainedNotes = emptySet()
        )
    }
    
    fun isSustainActive(): Boolean = _sustainState.value.isSustainActive
    
    fun getSustainedNotes(): Set<Int> = _sustainState.value.sustainedNotes
    
    companion object {
        const val CC64_SUSTAIN_PEDAL = 64
    }
}
