package com.adioevo.daw.midi.model

data class MidiDevice(
    val id: Int,
    val name: String,
    val manufacturer: String,
    val type: MidiDeviceType,
    val inputPorts: Int,
    val outputPorts: Int,
    val isConnected: Boolean,
    val supportsBluetoothMidi: Boolean = false
)

enum class MidiDeviceType {
    USB_DEVICE,
    BLUETOOTH,
    VIRTUAL,
    UNKNOWN
}

data class MidiPort(
    val portNumber: Int,
    val portName: String,
    val direction: MidiPortDirection
)

enum class MidiPortDirection {
    INPUT,
    OUTPUT,
    BIDIRECTIONAL
}
