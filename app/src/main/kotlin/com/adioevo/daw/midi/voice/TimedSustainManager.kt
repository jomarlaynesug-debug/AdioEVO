package com.adioevo.daw.midi.voice

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

data class TimedSustainConfig(
    val enabled: Boolean = true,
    val durationMs: Long = 5000L  // Default 5 seconds
) {
    init {
        require(durationMs in 100..30000) { "Duration must be between 100ms and 30 seconds" }
    }
}

@Singleton
class TimedSustainManager @Inject constructor() {
    private var config = TimedSustainConfig()
    private val scheduledReleases = ConcurrentHashMap<String, Job>()
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    
    fun setConfig(durationMs: Long, enabled: Boolean = true) {
        config = TimedSustainConfig(enabled = enabled, durationMs = durationMs.coerceIn(100, 30000))
        Timber.d("Timed Sustain: enabled=$enabled duration=${config.durationMs}ms")
    }
    
    fun scheduleNoteRelease(
        note: Int,
        channel: Int,
        onRelease: (note: Int, channel: Int) -> Unit
    ) {
        if (!config.enabled) {
            onRelease(note, channel)
            return
        }
        
        val key = "$channel:$note"
        
        // Cancel any existing scheduled release for this note
        scheduledReleases[key]?.cancel()
        
        val job = scope.launch {
            delay(config.durationMs)
            try {
                onRelease(note, channel)
                Timber.d("Timed release: note=$note channel=$channel after ${config.durationMs}ms")
            } catch (e: Exception) {
                Timber.e(e, "Error releasing timed sustain note")
            } finally {
                scheduledReleases.remove(key)
            }
        }
        
        scheduledReleases[key] = job
    }
    
    fun cancelScheduledRelease(note: Int, channel: Int) {
        val key = "$channel:$note"
        scheduledReleases[key]?.cancel()
        scheduledReleases.remove(key)
        Timber.d("Canceled scheduled release for note=$note channel=$channel")
    }
    
    fun cancelAllScheduledReleases() {
        scheduledReleases.values.forEach { it.cancel() }
        scheduledReleases.clear()
        Timber.d("Canceled all scheduled releases")
    }
    
    fun getConfig(): TimedSustainConfig = config
}
