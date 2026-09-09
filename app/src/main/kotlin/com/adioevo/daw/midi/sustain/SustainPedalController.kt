package com.adioevo.daw.midi.sustain

import com.adioevo.daw.midi.voice.MidiVoiceManager
import com.adioevo.daw.midi.voice.SustainManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

data class SustainPedalUIState(
    val isPhysicalSustainActive: Boolean = false,
    val isTouchscreenSustainActive: Boolean = false,
    val isSustainEffectivelyActive: Boolean = false
)

@Singleton
class SustainPedalController @Inject constructor(
    private val normalSustainEngine: NormalSustainEngine,
    private val sustainManager: SustainManager,
    private val voiceManager: MidiVoiceManager
) {
    
    private val _uiState = MutableStateFlow(SustainPedalUIState())
    val uiState: StateFlow<SustainPedalUIState> = _uiState
    
    fun processControlChange(cc: Int, value: Int, channel: Int) {
        if (cc == NormalSustainEngine.CC64_SUSTAIN_PEDAL) {
            normalSustainEngine.handleControlChange(cc, value, channel)
            updateUIState()
        }
    }
    
    fun toggleTouchscreenSustain(channel: Int) {
        if (_uiState.value.isTouchscreenSustainActive) {
            normalSustainEngine.disableTouchscreenSustain(channel)
        } else {
            normalSustainEngine.enableTouchscreenSustain()
        }
        updateUIState()
    }
    
    fun releaseSustainPedal(channel: Int) {
        normalSustainEngine.disableTouchscreenSustain(channel)
        updateUIState()
    }
    
    fun isSustainActive(): Boolean = normalSustainEngine.isSustainActive()
    
    private fun updateUIState() {
        val state = normalSustainEngine.sustainPedalState.value
        _uiState.value = SustainPedalUIState(
            isPhysicalSustainActive = state.isPhysicalSustainActive,
            isTouchscreenSustainActive = state.isTouchscreenSustainActive,
            isSustainEffectivelyActive = state.isSustainEffectivelyActive
        )
    }
}
