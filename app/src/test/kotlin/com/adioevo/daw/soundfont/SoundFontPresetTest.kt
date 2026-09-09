package com.adioevo.daw.soundfont

import org.junit.Test
import org.junit.Assert.*

class SoundFontPresetTest {
    @Test
    fun testPresetCreation() {
        val presetId = "1:0:0"
        val presetName = "Acoustic Grand Piano"
        val instrumentName = "Grand Piano"
        
        val instrument = com.adioevo.daw.soundfont.model.SoundFontInstrument(
            presetId = presetId,
            name = presetName,
            bank = 0,
            program = 0,
            sampleData = byteArrayOf(1, 2, 3, 4)
        )
        
        assertEquals(presetId, instrument.presetId)
        assertEquals(presetName, instrument.name)
        assertEquals(0, instrument.bank)
        assertEquals(0, instrument.program)
        assertNotNull(instrument.sampleData)
    }
}
