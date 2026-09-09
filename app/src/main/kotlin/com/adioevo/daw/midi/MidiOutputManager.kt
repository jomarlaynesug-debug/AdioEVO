package com.adioevo.daw.midi

import android.media.midi.MidiDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MidiOutputManager @Inject constructor() {
    private var midiDevice: MidiDevice? = null
    private var midiOutputPort: android.media.midi.MidiOutputPort? = null
    private var midiOutputReceiver: android.media.midi.MidiReceiver? = null
    
    suspend fun openOutputPort(
        device: MidiDevice,
        portNumber: Int,
        receiver: android.media.midi.MidiReceiver
    ) = withContext(Dispatchers.IO) {
        try {
            midiOutputPort = device.openOutputPort(portNumber)
            midiDevice = device
            midiOutputReceiver = receiver
            midiOutputPort?.connect(receiver)
            Timber.d("Opened MIDI output port $portNumber with receiver")
        } catch (e: IOException) {
            Timber.e(e, "Failed to open MIDI output port")
        }
    }
    
    suspend fun closeOutputPort() = withContext(Dispatchers.IO) {
        try {
            midiOutputPort?.disconnect(midiOutputReceiver)
            midiOutputPort?.close()
            midiOutputPort = null
            midiOutputReceiver = null
            Timber.d("Closed MIDI output port")
        } catch (e: IOException) {
            Timber.e(e, "Error closing MIDI output port")
        }
    }
}
