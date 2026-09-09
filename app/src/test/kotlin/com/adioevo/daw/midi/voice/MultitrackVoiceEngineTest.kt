package com.adioevo.daw.midi.voice

import org.junit.Test
import org.junit.Assert.*
import com.adioevo.daw.project.model.Track
import com.adioevo.daw.project.model.TrackType

class MultitrackVoiceEngineTest {
    private val voiceManager = MidiVoiceManager()
    private val sustainManager = SustainManager()
    private val timedSustainManager = TimedSustainManager()
    private val engine = MultitrackVoiceEngine(
        voiceManager = voiceManager,
        sustainManager = sustainManager,
        timedSustainManager = timedSustainManager
    )
    
    @Test
    fun testRegisterTrack() {
        val track = Track(
            id = 1, projectId = 1, trackNumber = 1,
            name = "Piano", type = TrackType.INSTRUMENT,
            soundFontId = 1, bankMsb = 0, bankLsb = 0,
            program = 0, midiChannel = 0,
            volume = 1.0f, pan = 0.0f, isMuted = false,
            isSolo = false, isRecordArmed = false,
            sustainMode = "NORMAL",
            timedSustainDuration = 5000L,
            reverbSend = 0.0f, chorusSend = 0.0f
        )
        
        engine.registerTrack(track)
        
        val states = engine.trackVoiceStates.value
        assertTrue(states.containsKey(track.id))
        assertEquals(0, states[track.id]?.activeVoiceCount ?: -1)
    }
    
    @Test
    fun testNoteOnForTrack() {
        val track = Track(
            id = 1, projectId = 1, trackNumber = 1,
            name = "Piano", type = TrackType.INSTRUMENT,
            soundFontId = 1, bankMsb = 0, bankLsb = 0,
            program = 0, midiChannel = 0,
            volume = 1.0f, pan = 0.0f, isMuted = false,
            isSolo = false, isRecordArmed = false,
            sustainMode = "NORMAL",
            timedSustainDuration = 5000L,
            reverbSend = 0.0f, chorusSend = 0.0f
        )
        
        engine.registerTrack(track)
        engine.noteOnForTrack(track.id, 60, 100)
        
        assertEquals(1, engine.getTrackVoiceCount(track.id))
    }
    
    @Test
    fun testIndependentTrackVoices() {
        val track1 = Track(
            id = 1, projectId = 1, trackNumber = 1,
            name = "Piano", type = TrackType.INSTRUMENT,
            soundFontId = 1, bankMsb = 0, bankLsb = 0,
            program = 0, midiChannel = 0,
            volume = 1.0f, pan = 0.0f, isMuted = false,
            isSolo = false, isRecordArmed = false,
            sustainMode = "NORMAL",
            timedSustainDuration = 5000L,
            reverbSend = 0.0f, chorusSend = 0.0f
        )
        
        val track2 = Track(
            id = 2, projectId = 1, trackNumber = 2,
            name = "Strings", type = TrackType.INSTRUMENT,
            soundFontId = 1, bankMsb = 0, bankLsb = 0,
            program = 48, midiChannel = 1,
            volume = 1.0f, pan = 0.0f, isMuted = false,
            isSolo = false, isRecordArmed = false,
            sustainMode = "TIMED_SUSTAIN",
            timedSustainDuration = 3000L,
            reverbSend = 0.5f, chorusSend = 0.0f
        )
        
        engine.registerTrack(track1)
        engine.registerTrack(track2)
        
        engine.noteOnForTrack(track1.id, 60, 100)
        engine.noteOnForTrack(track1.id, 64, 100)
        engine.noteOnForTrack(track2.id, 72, 100)
        
        assertEquals(2, engine.getTrackVoiceCount(track1.id))
        assertEquals(1, engine.getTrackVoiceCount(track2.id))
        assertEquals(3, engine.getTotalActiveVoices())
    }
    
    @Test
    fun testPanic() {
        val track = Track(
            id = 1, projectId = 1, trackNumber = 1,
            name = "Piano", type = TrackType.INSTRUMENT,
            soundFontId = 1, bankMsb = 0, bankLsb = 0,
            program = 0, midiChannel = 0,
            volume = 1.0f, pan = 0.0f, isMuted = false,
            isSolo = false, isRecordArmed = false,
            sustainMode = "NORMAL",
            timedSustainDuration = 5000L,
            reverbSend = 0.0f, chorusSend = 0.0f
        )
        
        engine.registerTrack(track)
        engine.noteOnForTrack(track.id, 60, 100)
        engine.noteOnForTrack(track.id, 64, 100)
        engine.noteOnForTrack(track.id, 67, 100)
        
        assertEquals(3, engine.getTotalActiveVoices())
        
        engine.panic()
        
        assertEquals(0, engine.getTotalActiveVoices())
    }
}
