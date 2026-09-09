package com.adioevo.daw.midi

import org.junit.Test
import org.junit.Assert.*

class MidiMessageTest {
    @Test
    fun testMidiNoteOn() {
        // MIDI Note On: Status=0x90 (channel 1), Data1=60 (Middle C), Data2=100 (velocity)
        val status = 0x90
        val data1 = 60
        val data2 = 100
        
        val message = MidiMessage(
            timestamp = 0L,
            status = status,
            data1 = data1,
            data2 = data2,
            data = byteArrayOf(0x90.toByte(), 60.toByte(), 100.toByte())
        )
        
        assertEquals(0x90, message.status)
        assertEquals(60, message.data1)
        assertEquals(100, message.data2)
    }
    
    @Test
    fun testMidiNoteOff() {
        // MIDI Note Off: Status=0x80 (channel 1), Data1=60 (Middle C), Data2=64 (release velocity)
        val status = 0x80
        val data1 = 60
        val data2 = 64
        
        val message = MidiMessage(
            timestamp = 0L,
            status = status,
            data1 = data1,
            data2 = data2,
            data = byteArrayOf(0x80.toByte(), 60.toByte(), 64.toByte())
        )
        
        assertEquals(0x80, message.status)
        assertEquals(60, message.data1)
        assertEquals(64, message.data2)
    }
    
    @Test
    fun testMidiControlChange() {
        // MIDI CC: Status=0xB0 (channel 1), Data1=64 (Sustain Pedal), Data2=127 (on)
        val status = 0xB0
        val data1 = 64
        val data2 = 127
        
        val message = MidiMessage(
            timestamp = 0L,
            status = status,
            data1 = data1,
            data2 = data2,
            data = byteArrayOf(0xB0.toByte(), 64.toByte(), 127.toByte())
        )
        
        assertEquals(0xB0, message.status)
        assertEquals(64, message.data1)
        assertEquals(127, message.data2)
    }
    
    @Test
    fun testMidiProgramChange() {
        // MIDI Program Change: Status=0xC0 (channel 1), Data1=0 (program 0)
        val status = 0xC0
        val data1 = 0
        
        val message = MidiMessage(
            timestamp = 0L,
            status = status,
            data1 = data1,
            data2 = 0,
            data = byteArrayOf(0xC0.toByte(), 0.toByte())
        )
        
        assertEquals(0xC0, message.status)
        assertEquals(0, message.data1)
    }
}
