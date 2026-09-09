package com.adioevo.daw.midi.controller

import com.adioevo.daw.midi.MidiDeviceManager
import com.adioevo.daw.midi.keyboard.MidiKeyboardEngine
import com.adioevo.daw.midi.voice.MultitrackVoiceEngine
import com.adioevo.daw.project.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MidiController @Inject constructor(
    private val midiDeviceManager: MidiDeviceManager,
    private val keyboardEngine: MidiKeyboardEngine,
    private val multitrackVoiceEngine: MultitrackVoiceEngine,
    private val projectRepository: ProjectRepository
) {
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected
    
    suspend fun initialize() {
        try {
            if (!midiDeviceManager.isMidiAvailable()) {
                Timber.w("MIDI not available on this device")
                _isConnected.value = false
                return
            }
            
            midiDeviceManager.refreshDevices()
            _isConnected.value = true
            Timber.d("MIDI Controller initialized")
        } catch (e: Exception) {
            Timber.e(e, "Error initializing MIDI Controller")
            _isConnected.value = false
        }
    }
    
    fun registerProjectTracks() {
        projectRepository.tracks.value.forEach { track ->
            multitrackVoiceEngine.registerTrack(track)
        }
        Timber.d("Registered ${projectRepository.tracks.value.size} tracks")
    }
    
    fun unregisterProjectTracks() {
        projectRepository.tracks.value.forEach { track ->
            multitrackVoiceEngine.unregisterTrack(track.id)
        }
        Timber.d("Unregistered all tracks")
    }
    
    fun processPhysicalMidiMessage(status: Int, data1: Int, data2: Int) {
        keyboardEngine.processPhysicalMidiMessage(status, data1, data2)
    }
    
    fun playNote(trackId: Int, note: Int, velocity: Int = 100) {
        multitrackVoiceEngine.noteOnForTrack(trackId, note, velocity)
    }
    
    fun stopNote(trackId: Int, note: Int) {
        multitrackVoiceEngine.noteOffForTrack(trackId, note)
    }
    
    fun panic() {
        Timber.w("PANIC button pressed")
        multitrackVoiceEngine.panic()
        keyboardEngine.panic()
    }
}
