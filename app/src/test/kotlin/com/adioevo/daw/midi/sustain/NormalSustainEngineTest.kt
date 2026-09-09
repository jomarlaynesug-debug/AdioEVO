package com.adioevo.daw.midi.sustain

import org.junit.Test
import org.junit.Assert.*
import com.adioevo.daw.midi.voice.MidiVoiceManager
import com.adioevo.daw.midi.voice.SustainManager

class NormalSustainEngineTest {
    private val sustainManager = SustainManager()
    private val voiceManager = MidiVoiceManager()
    private val engine = NormalSustainEngine(
        sustainManager = sustainManager,
        voiceManager = voiceManager
    )
    
    @Test
    fun testSustainPedalOn() {
        voiceManager.noteOn(60, 100, 0)
        
        engine.handleControlChange(64, 127, 0)
        
        assertTrue(engine.isSustainActive())
    }
    
    @Test
    fun testSustainPedalOff() {
        voiceManager.noteOn(60, 100, 0)
        engine.handleControlChange(64, 127, 0)
        assertTrue(engine.isSustainActive())
        
        engine.handleControlChange(64, 0, 0)
        assertFalse(engine.isSustainActive())
    }
    
    @Test
    fun testSustainPedalThreshold() {
        // Value >= 64 should activate sustain
        engine.handleControlChange(64, 64, 0)
        assertTrue(engine.isSustainActive())
        
        // Value < 64 should deactivate
        engine.handleControlChange(64, 63, 0)
        assertFalse(engine.isSustainActive())
    }
    
    @Test
    fun testTouchscreenSustain() {
        assertFalse(engine.isSustainActive())
        
        engine.enableTouchscreenSustain()
        assertTrue(engine.isSustainActive())
        
        engine.disableTouchscreenSustain(0)
        assertFalse(engine.isSustainActive())
    }
    
    @Test
    fun testSustainedNoteRelease() {
        voiceManager.noteOn(60, 100, 0)
        engine.addToSustain(60)
        
        assertEquals(1, engine.getSustainedNotes().size)
        assertTrue(engine.getSustainedNotes().contains(60))
    }
    
    @Test
    fun testCombinedPhysicalAndTouchscreenSustain() {
        engine.handleControlChange(64, 127, 0)  // Physical ON
        assertTrue(engine.isSustainActive())
        
        engine.disableTouchscreenSustain(0)  // Disable touchscreen (physical still on)
        assertTrue(engine.isSustainActive())
        
        engine.handleControlChange(64, 0, 0)  // Physical OFF
        assertFalse(engine.isSustainActive())
    }
}
