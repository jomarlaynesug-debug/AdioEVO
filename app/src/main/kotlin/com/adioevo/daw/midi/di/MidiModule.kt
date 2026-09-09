package com.adioevo.daw.midi.di

import com.adioevo.daw.midi.MidiDeviceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MidiModule {
    @Provides
    @Singleton
    fun provideMidiDeviceManager(
        midiDeviceManager: MidiDeviceManager
    ): MidiDeviceManager = midiDeviceManager
}
