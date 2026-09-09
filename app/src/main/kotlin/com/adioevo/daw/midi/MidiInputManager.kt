package com.adioevo.daw.midi

import android.media.midi.MidiDevice
import android.media.midi.MidiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MidiInputManager @Inject constructor() {
    private var midiDevice: MidiDevice? = null
    private var midiInputPort: android.media.midi.MidiInputPort? = null
    
    suspend fun openInputPort(
        device: MidiDevice,
        portNumber: Int
    ) = withContext(Dispatchers.IO) {
        try {
            midiInputPort = device.openInputPort(portNumber)
            Timber.d("Opened MIDI input port $portNumber")
        } catch (e: IOException) {
            Timber.e(e, "Failed to open MIDI input port")
        }
    }
    
    suspend fun closeInputPort() = withContext(Dispatchers.IO) {
        try {
            midiInputPort?.close()
            midiInputPort = null
            Timber.d("Closed MIDI input port")
        } catch (e: IOException) {
            Timber.e(e, "Error closing MIDI input port")
        }
    }
    
    fun sendMidiMessage(
        status: Int,
        data1: Int,
        data2: Int,
        offsetTimestamp: Long = 0
    ) {
        try {
            val message = byteArrayOf(status.toByte(), data1.toByte(), data2.toByte())
            midiInputPort?.send(message, 0, message.size, offsetTimestamp)
        } catch (e: Exception) {
            Timber.e(e, "Error sending MIDI message")
        }
    }
    
    fun sendMidiMessage(message: ByteArray, offsetTimestamp: Long = 0) {
        try {
            midiInputPort?.send(message, 0, message.size, offsetTimestamp)
        } catch (e: Exception) {
            Timber.e(e, "Error sending MIDI message")
        }
    }
}
