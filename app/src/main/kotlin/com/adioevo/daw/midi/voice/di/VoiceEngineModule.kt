package com.adioevo.daw.midi.voice.di

import com.adioevo.daw.midi.MidiDeviceManager
import com.adioevo.daw.midi.MidiInputManager
import com.adioevo.daw.midi.MidiOutputManager
import com.adioevo.daw.midi.keyboard.MidiKeyboardEngine
import com.adioevo.daw.midi.voice.MidiVoiceManager
import com.adioevo.daw.midi.voice.MultitrackVoiceEngine
import com.adioevo.daw.midi.voice.SustainManager
import com.adioevo.daw.midi.voice.TimedSustainManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VoiceEngineModule {
    @Provides
    @Singleton
    fun provideMidiVoiceManager(): MidiVoiceManager = MidiVoiceManager()
    
    @Provides
    @Singleton
    fun provideSustainManager(): SustainManager = SustainManager()
    
    @Provides
    @Singleton
    fun provideTimedSustainManager(): TimedSustainManager = TimedSustainManager()
    
    @Provides
    @Singleton
    fun provideMultitrackVoiceEngine(
        voiceManager: MidiVoiceManager,
        sustainManager: SustainManager,
        timedSustainManager: TimedSustainManager
    ): MultitrackVoiceEngine = MultitrackVoiceEngine(
        voiceManager = voiceManager,
        sustainManager = sustainManager,
        timedSustainManager = timedSustainManager
    )
}
