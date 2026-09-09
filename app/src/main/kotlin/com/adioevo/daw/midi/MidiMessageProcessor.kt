package com.adioevo.daw.midi

import android.media.midi.MidiReceiver
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import timber.log.Timber

data class MidiMessage(
    val timestamp: Long,
    val status: Int,
    val data1: Int,
    val data2: Int,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MidiMessage

        if (timestamp != other.timestamp) return false
        if (status != other.status) return false
        if (data1 != other.data1) return false
        if (data2 != other.data2) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + status
        result = 31 * result + data1
        result = 31 * result + data2
        result = 31 * result + data.contentHashCode()
        return result
    }
}

class MidiMessageProcessor : MidiReceiver() {
    private val _midiMessages = MutableSharedFlow<MidiMessage>()
    val midiMessages: SharedFlow<MidiMessage> = _midiMessages
    
    override fun onSend(
        msg: ByteArray?,
        offset: Int,
        count: Int,
        timestamp: Long
    ) {
        if (msg == null || count < 1) return
        
        try {
            val status = msg[offset].toInt() and 0xFF
            val data1 = if (count > 1) msg[offset + 1].toInt() and 0xFF else 0
            val data2 = if (count > 2) msg[offset + 2].toInt() and 0xFF else 0
            
            val midiMsg = MidiMessage(
                timestamp = timestamp,
                status = status,
                data1 = data1,
                data2 = data2,
                data = msg.copyOfRange(offset, offset + count)
            )
            
            Timber.d("MIDI Message: status=0x${status.toString(16)}, data1=$data1, data2=$data2")
        } catch (e: Exception) {
            Timber.e(e, "Error processing MIDI message")
        }
    }
}
