package com.adioevo.daw.midi.voice

import org.junit.Test
import org.junit.Assert.*

class MidiVoiceManagerTest {
    private val voiceManager = MidiVoiceManager()
    
    @Test
    fun testNoteOn() {
        val voice = voiceManager.noteOn(note = 60, velocity = 100, channel = 0)
        assertNotNull(voice)
        assertTrue(voice!!.isActive)
        assertEquals(60, voice.note)
        assertEquals(100, voice.velocity)
    }
    
    @Test
    fun testNoteOff() {
        val voiceOn = voiceManager.noteOn(note = 60, velocity = 100, channel = 0)
        assertNotNull(voiceOn)
        
        val voiceOff = voiceManager.noteOff(note = 60, channel = 0)
        assertNotNull(voiceOff)
        assertFalse(voiceOff!!.isActive)
    }
    
    @Test
    fun testPolyphony() {
        // Play multiple notes simultaneously
        val voice1 = voiceManager.noteOn(note = 60, velocity = 100, channel = 0)
        val voice2 = voiceManager.noteOn(note = 64, velocity = 100, channel = 0)
        val voice3 = voiceManager.noteOn(note = 67, velocity = 100, channel = 0)
        
        assertNotNull(voice1)
        assertNotNull(voice2)
        assertNotNull(voice3)
        
        val activeCount = voiceManager.getActiveVoiceCount()
        assertEquals(3, activeCount)
    }
    
    @Test
    fun testAllNotesOff() {
        voiceManager.noteOn(note = 60, velocity = 100, channel = 0)
        voiceManager.noteOn(note = 64, velocity = 100, channel = 0)
        voiceManager.noteOn(note = 67, velocity = 100, channel = 0)
        
        voiceManager.allNotesOff()
        
        val activeCount = voiceManager.getActiveVoiceCount()
        assertEquals(0, activeCount)
    }
    
    @Test
    fun testNoteRetrigger() {
        voiceManager.noteOn(note = 60, velocity = 100, channel = 0)
        val retriggered = voiceManager.noteOn(note = 60, velocity = 127, channel = 0)
        
        assertNotNull(retriggered)
        assertEquals(127, retriggered!!.velocity)
    }
    
    @Test
    fun testVelocityZeroAsNoteOff() {
        voiceManager.noteOn(note = 60, velocity = 100, channel = 0)
        val released = voiceManager.noteOn(note = 60, velocity = 0, channel = 0)
        
        assertFalse(released!!.isActive)
    }
}
