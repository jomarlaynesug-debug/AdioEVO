package com.adioevo.daw.project

import org.junit.Test
import org.junit.Assert.*
import com.adioevo.daw.project.model.Track
import com.adioevo.daw.project.model.TrackEntity
import com.adioevo.daw.project.model.TrackType

class TrackInstrumentTest {
    @Test
    fun testTrackInstrumentAssignment() {
        val track = Track(
            id = 1,
            projectId = 1,
            trackNumber = 1,
            name = "Piano",
            type = TrackType.INSTRUMENT,
            soundFontId = 42,  // SoundFont ID
            bankMsb = 0,
            bankLsb = 0,
            program = 0,  // Acoustic Grand Piano
            midiChannel = 0,
            volume = 1.0f,
            pan = 0.0f,
            isMuted = false,
            isSolo = false,
            isRecordArmed = false,
            sustainMode = "NORMAL",
            timedSustainDuration = 5000L,
            reverbSend = 0.0f,
            chorusSend = 0.0f
        )
        
        assertEquals(42, track.soundFontId)
        assertEquals(0, track.bankMsb)
        assertEquals(0, track.bankLsb)
        assertEquals(0, track.program)
        assertEquals("NORMAL", track.sustainMode)
    }
    
    @Test
    fun testDifferentTracksIndependentInstruments() {
        val track1 = Track(
            id = 1, projectId = 1, trackNumber = 1, name = "Piano",
            type = TrackType.INSTRUMENT, soundFontId = 1, bankMsb = 0,
            bankLsb = 0, program = 0, midiChannel = 0,
            volume = 1.0f, pan = 0.0f, isMuted = false, isSolo = false,
            isRecordArmed = false, sustainMode = "NORMAL",
            timedSustainDuration = 5000L, reverbSend = 0.0f, chorusSend = 0.0f
        )
        
        val track2 = Track(
            id = 2, projectId = 1, trackNumber = 2, name = "Strings",
            type = TrackType.INSTRUMENT, soundFontId = 2, bankMsb = 0,
            bankLsb = 0, program = 48, midiChannel = 1,
            volume = 1.0f, pan = 0.0f, isMuted = false, isSolo = false,
            isRecordArmed = false, sustainMode = "TIMED_SUSTAIN",
            timedSustainDuration = 3000L, reverbSend = 0.5f, chorusSend = 0.0f
        )
        
        // Verify independent instruments
        assertEquals(1, track1.soundFontId)
        assertEquals(0, track1.program)
        assertEquals("NORMAL", track1.sustainMode)
        
        assertEquals(2, track2.soundFontId)
        assertEquals(48, track2.program)
        assertEquals("TIMED_SUSTAIN", track2.sustainMode)
    }
}
