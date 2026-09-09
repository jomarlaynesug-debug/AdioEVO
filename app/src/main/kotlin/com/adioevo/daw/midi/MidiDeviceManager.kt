package com.adioevo.daw.midi

import android.content.Context
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import com.adioevo.daw.midi.model.MidiDevice
import com.adioevo.daw.midi.model.MidiDeviceType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MidiDeviceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val midiManager = context.getSystemService(Context.MIDI_SERVICE) as? MidiManager
    
    private val _midiDevices = MutableStateFlow<List<MidiDevice>>(emptyList())
    val midiDevices: StateFlow<List<MidiDevice>> = _midiDevices
    
    private val _selectedMidiIn = MutableStateFlow<MidiDevice?>(null)
    val selectedMidiIn: StateFlow<MidiDevice?> = _selectedMidiIn
    
    private val _selectedMidiOut = MutableStateFlow<MidiDevice?>(null)
    val selectedMidiOut: StateFlow<MidiDevice?> = _selectedMidiOut
    
    private val _selectedMidiChannel = MutableStateFlow(0) // 0-15 for MIDI channels 1-16
    val selectedMidiChannel: StateFlow<Int> = _selectedMidiChannel
    
    suspend fun refreshDevices() = withContext(Dispatchers.IO) {
        try {
            if (midiManager == null) {
                Timber.w("MIDI Manager not available on this device")
                _midiDevices.value = emptyList()
                return@withContext
            }
            
            val devices = midiManager.devices
            val midiDeviceList = mutableListOf<MidiDevice>()
            
            for (device in devices) {
                val info = device.info
                val type = determineMidiDeviceType(info)
                val inputPorts = info.inputPortCount
                val outputPorts = info.outputPortCount
                
                val midiDevice = MidiDevice(
                    id = info.id,
                    name = info.properties?.getString(MidiDeviceInfo.PROPERTY_NAME) ?: "Unknown Device",
                    manufacturer = info.properties?.getString(MidiDeviceInfo.PROPERTY_MANUFACTURER) ?: "Unknown",
                    type = type,
                    inputPorts = inputPorts,
                    outputPorts = outputPorts,
                    isConnected = true,
                    supportsBluetoothMidi = type == MidiDeviceType.BLUETOOTH
                )
                midiDeviceList.add(midiDevice)
            }
            
            _midiDevices.value = midiDeviceList
            Timber.d("Found ${midiDeviceList.size} MIDI devices")
            
        } catch (e: Exception) {
            Timber.e(e, "Error refreshing MIDI devices")
            _midiDevices.value = emptyList()
        }
    }
    
    private fun determineMidiDeviceType(info: MidiDeviceInfo): MidiDeviceType {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && 
            info.properties?.getInt(MidiDeviceInfo.PROPERTY_BLUETOOTH) == 1 -> {
                MidiDeviceType.BLUETOOTH
            }
            info.properties?.getInt(MidiDeviceInfo.PROPERTY_USB) == 1 -> {
                MidiDeviceType.USB_DEVICE
            }
            else -> MidiDeviceType.UNKNOWN
        }
    }
    
    fun selectMidiInput(device: MidiDevice) {
        _selectedMidiIn.value = device
        Timber.d("Selected MIDI input: ${device.name}")
    }
    
    fun selectMidiOutput(device: MidiDevice) {
        _selectedMidiOut.value = device
        Timber.d("Selected MIDI output: ${device.name}")
    }
    
    fun selectMidiChannel(channel: Int) {
        val validChannel = channel.coerceIn(0, 15)
        _selectedMidiChannel.value = validChannel
        Timber.d("Selected MIDI channel: ${validChannel + 1}")
    }
    
    fun getDeviceInputPorts(device: MidiDevice): Int = device.inputPorts
    
    fun getDeviceOutputPorts(device: MidiDevice): Int = device.outputPorts
    
    fun isMidiAvailable(): Boolean = midiManager != null
}
