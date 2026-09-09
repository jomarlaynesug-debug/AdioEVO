package com.adioevo.daw.midi.keyboard

import com.adioevo.daw.midi.MidiDeviceManager
import com.adioevo.daw.midi.MidiMessageProcessor
import com.adioevo.daw.midi.voice.MidiVoiceManager
import com.adioevo.daw.midi.voice.SustainManager
import com.adioevo.daw.midi.voice.TimedSustainManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MidiKeyboardEngine @Inject constructor(
    private val voiceManager: MidiVoiceManager,
    private val sustainManager: SustainManager,
    private val timedSustainManager: TimedSustainManager,
    private val midiDeviceManager: MidiDeviceManager
) {
    
    private val _keyboardState = MutableStateFlow(
        KeyboardState(
            octave = 4,
            transpose = 0,
            velocity = 100,
            sustainMode = SustainMode.NORMAL
        )
    )
    val keyboardState: StateFlow<KeyboardState> = _keyboardState
    
    private val messageProcessor = MidiMessageProcessor()
    
    fun processPhysicalMidiMessage(status: Int, data1: Int, data2: Int) {
        val statusHigh = (status and 0xF0).toInt()
        val channel = (status and 0x0F).toInt()
        
        when (statusHigh) {
            0x90 -> handleNoteOn(data1, data2, channel)
            0x80 -> handleNoteOff(data1, data2, channel)
            0xB0 -> handleControlChange(data1, data2, channel)
            0xC0 -> handleProgramChange(data1, channel)
            0xE0 -> handlePitchBend(data1, data2, channel)
            0xD0 -> handleChannelPressure(data1, channel)
            0xA0 -> handlePolyPressure(data1, data2, channel)
            else -> Timber.d("Unhandled MIDI status: 0x${status.toString(16)}")
        }
    }
    
    fun playVirtualKeyNote(note: Int, velocity: Int = 100) {
        val channel = midiDeviceManager.selectedMidiChannel.value
        handleNoteOn(note, velocity, channel)
    }
    
    fun stopVirtualKeyNote(note: Int) {
        val channel = midiDeviceManager.selectedMidiChannel.value
        handleNoteOff(note, 64, channel)
    }
    
    fun setOctave(octave: Int) {
        _keyboardState.value = _keyboardState.value.copy(
            octave = octave.coerceIn(0, 8)
        )
        Timber.d("Octave: ${_keyboardState.value.octave}")
    }
    
    fun setTranspose(semitones: Int) {
        _keyboardState.value = _keyboardState.value.copy(
            transpose = semitones.coerceIn(-12, 12)
        )
        Timber.d("Transpose: ${_keyboardState.value.transpose} semitones")
    }
    
    fun setVelocity(velocity: Int) {
        _keyboardState.value = _keyboardState.value.copy(
            velocity = velocity.coerceIn(1, 127)
        )
    }
    
    fun setSustainMode(mode: SustainMode) {
        _keyboardState.value = _keyboardState.value.copy(sustainMode = mode)
        Timber.d("Sustain Mode: $mode")
    }
    
    fun panic() {
        Timber.w("PANIC: Releasing all notes")
        voiceManager.allNotesOff()
        sustainManager.clearSustainedNotes()
        timedSustainManager.cancelAllScheduledReleases()
    }
    
    private fun handleNoteOn(note: Int, velocity: Int, channel: Int) {
        if (velocity == 0) {
            handleNoteOff(note, 64, channel)
            return
        }
        
        val voice = voiceManager.noteOn(note, velocity, channel)
        if (voice != null) {
            Timber.d("Note ON: $note velocity=$velocity channel=$channel")
        } else {
            Timber.w("Failed to allocate voice for note $note")
        }
    }
    
    private fun handleNoteOff(note: Int, velocity: Int, channel: Int) {
        val sustainMode = _keyboardState.value.sustainMode
        
        if (sustainManager.isSustainActive()) {
            sustainManager.addSustainedNote(note)
            Timber.d("Note $note added to sustain pedal")
            return
        }
        
        when (sustainMode) {
            SustainMode.NORMAL -> {
                voiceManager.noteOff(note, channel)
            }
            SustainMode.TIMED_SUSTAIN -> {
                timedSustainManager.scheduleNoteRelease(note, channel) { n, ch ->
                    voiceManager.noteOff(n, ch)
                }
            }
        }
        
        Timber.d("Note OFF: $note channel=$channel sustainMode=$sustainMode")
    }
    
    private fun handleControlChange(cc: Int, value: Int, channel: Int) {
        when (cc) {
            64 -> {
                sustainManager.handleControlChange(cc, value)
                if (value < 64) {
                    // Sustain off: release all sustained notes
                    sustainManager.getSustainedNotes().forEach { note ->
                        voiceManager.noteOff(note, channel)
                    }
                    sustainManager.clearSustainedNotes()
                }
            }
            else -> Timber.d("CC $cc: $value on channel $channel")
        }
    }
    
    private fun handleProgramChange(program: Int, channel: Int) {
        Timber.d("Program Change: $program on channel $channel")
    }
    
    private fun handlePitchBend(lsb: Int, msb: Int, channel: Int) {
        val pitchValue = ((msb shl 7) or lsb) - 8192
        val pitchBend = pitchValue.toFloat() / 8192f
        voiceManager.pitchBend(pitchBend, channel)
        Timber.d("Pitch Bend: ${String.format("%.2f", pitchBend)} on channel $channel")
    }
    
    private fun handleChannelPressure(pressure: Int, channel: Int) {
        Timber.d("Channel Pressure: $pressure on channel $channel")
    }
    
    private fun handlePolyPressure(note: Int, pressure: Int, channel: Int) {
        voiceManager.aftertouch(note, pressure, channel)
        Timber.d("Poly Pressure: note=$note pressure=$pressure channel=$channel")
    }
}

data class KeyboardState(
    val octave: Int = 4,
    val transpose: Int = 0,
    val velocity: Int = 100,
    val sustainMode: SustainMode = SustainMode.NORMAL
)

enum class SustainMode {
    NORMAL,
    TIMED_SUSTAIN
}
