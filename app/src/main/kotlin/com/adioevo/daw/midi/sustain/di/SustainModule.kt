package com.adioevo.daw.midi.sustain.di

import com.adioevo.daw.midi.sustain.NormalSustainEngine
import com.adioevo.daw.midi.sustain.SustainPedalController
import com.adioevo.daw.midi.voice.MidiVoiceManager
import com.adioevo.daw.midi.voice.SustainManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SustainModule {
    @Provides
    @Singleton
    fun provideNormalSustainEngine(
        sustainManager: SustainManager,
        voiceManager: MidiVoiceManager
    ): NormalSustainEngine = NormalSustainEngine(
        sustainManager = sustainManager,
        voiceManager = voiceManager
    )
    
    @Provides
    @Singleton
    fun provideSustainPedalController(
        normalSustainEngine: NormalSustainEngine,
        sustainManager: SustainManager,
        voiceManager: MidiVoiceManager
    ): SustainPedalController = SustainPedalController(
        normalSustainEngine = normalSustainEngine,
        sustainManager = sustainManager,
        voiceManager = voiceManager
    )
}
