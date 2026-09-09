package com.adioevo.daw.project

import com.adioevo.daw.project.database.ProjectDao
import com.adioevo.daw.project.database.TrackDao
import com.adioevo.daw.project.model.Project
import com.adioevo.daw.project.model.ProjectEntity
import com.adioevo.daw.project.model.Track
import com.adioevo.daw.project.model.TrackEntity
import com.adioevo.daw.project.model.TrackType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepository @Inject constructor(
    private val projectDao: ProjectDao,
    private val trackDao: TrackDao
) {
    
    private val _currentProject = MutableStateFlow<ProjectEntity?>(null)
    val currentProject: StateFlow<ProjectEntity?> = _currentProject
    
    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks
    
    private val _selectedTrack = MutableStateFlow<Track?>(null)
    val selectedTrack: StateFlow<Track?> = _selectedTrack
    
    suspend fun createProject(name: String): Result<ProjectEntity> = withContext(Dispatchers.IO) {
        try {
            val project = ProjectEntity(name = name)
            val id = projectDao.insertProject(project).toInt()
            val created = project.copy(id = id)
            
            _currentProject.value = created
            Timber.d("Created project: $name (id=$id)")
            
            Result.success(created)
        } catch (e: Exception) {
            Timber.e(e, "Error creating project")
            Result.failure(e)
        }
    }
    
    suspend fun openProject(id: Int): Result<ProjectEntity> = withContext(Dispatchers.IO) {
        try {
            val project = projectDao.getProjectById(id) ?: return@withContext Result.failure(
                Exception("Project not found")
            )
            
            _currentProject.value = project
            
            // Load tracks
            val tracksFlow = trackDao.getTracksForProject(id)
            tracksFlow.collect { trackEntities ->
                _tracks.value = trackEntities.map { Track.fromEntity(it) }
            }
            
            Timber.d("Opened project: ${project.name} (id=$id)")
            Result.success(project)
        } catch (e: Exception) {
            Timber.e(e, "Error opening project")
            Result.failure(e)
        }
    }
    
    suspend fun saveProject() = withContext(Dispatchers.IO) {
        try {
            val project = _currentProject.value ?: return@withContext
            val updated = project.copy(modifiedAt = System.currentTimeMillis())
            projectDao.updateProject(updated)
            _currentProject.value = updated
            Timber.d("Saved project: ${project.name}")
        } catch (e: Exception) {
            Timber.e(e, "Error saving project")
        }
    }
    
    suspend fun deleteProject(id: Int) = withContext(Dispatchers.IO) {
        try {
            val project = projectDao.getProjectById(id) ?: return@withContext
            projectDao.deleteProject(project)
            
            if (_currentProject.value?.id == id) {
                _currentProject.value = null
                _tracks.value = emptyList()
            }
            
            Timber.d("Deleted project: ${project.name}")
        } catch (e: Exception) {
            Timber.e(e, "Error deleting project")
        }
    }
    
    suspend fun createTrack(
        type: TrackType,
        name: String,
        midiChannel: Int = 0
    ): Result<Track> = withContext(Dispatchers.IO) {
        try {
            val projectId = _currentProject.value?.id ?: return@withContext Result.failure(
                Exception("No project open")
            )
            
            val trackNumber = _tracks.value.size + 1
            
            val trackEntity = TrackEntity(
                projectId = projectId,
                trackNumber = trackNumber,
                name = name,
                type = type,
                midiChannel = midiChannel
            )
            
            val trackId = trackDao.insertTrack(trackEntity).toInt()
            val created = Track.fromEntity(trackEntity.copy(id = trackId))
            
            _tracks.value = _tracks.value + created
            
            Timber.d("Created track: $name (type=$type, channel=$midiChannel)")
            Result.success(created)
        } catch (e: Exception) {
            Timber.e(e, "Error creating track")
            Result.failure(e)
        }
    }
    
    suspend fun updateTrackInstrument(
        trackId: Int,
        soundFontId: Int,
        bankMsb: Int,
        bankLsb: Int,
        program: Int
    ) = withContext(Dispatchers.IO) {
        try {
            trackDao.updateTrackInstrument(trackId, soundFontId, bankMsb, bankLsb, program)
            
            val updated = _tracks.value.map { track ->
                if (track.id == trackId) {
                    track.copy(
                        soundFontId = soundFontId,
                        bankMsb = bankMsb,
                        bankLsb = bankLsb,
                        program = program
                    )
                } else track
            }
            _tracks.value = updated
            
            Timber.d(
                "Updated track $trackId instrument: " +
                "SF=$soundFontId Bank=$bankMsb:$bankLsb Program=$program"
            )
        } catch (e: Exception) {
            Timber.e(e, "Error updating track instrument")
        }
    }
    
    suspend fun updateTrackSustain(
        trackId: Int,
        sustainMode: String,
        duration: Long = 5000L
    ) = withContext(Dispatchers.IO) {
        try {
            trackDao.updateTrackSustain(trackId, sustainMode, duration)
            
            val updated = _tracks.value.map { track ->
                if (track.id == trackId) {
                    track.copy(
                        sustainMode = sustainMode,
                        timedSustainDuration = duration
                    )
                } else track
            }
            _tracks.value = updated
            
            Timber.d(
                "Updated track $trackId sustain: " +
                "mode=$sustainMode duration=${duration}ms"
            )
        } catch (e: Exception) {
            Timber.e(e, "Error updating track sustain")
        }
    }
    
    suspend fun deleteTrack(trackId: Int) = withContext(Dispatchers.IO) {
        try {
            val track = _tracks.value.find { it.id == trackId } ?: return@withContext
            trackDao.deleteTrack(track.toEntity())
            
            _tracks.value = _tracks.value.filter { it.id != trackId }
            if (_selectedTrack.value?.id == trackId) {
                _selectedTrack.value = null
            }
            
            Timber.d("Deleted track: ${track.name}")
        } catch (e: Exception) {
            Timber.e(e, "Error deleting track")
        }
    }
    
    fun selectTrack(trackId: Int) {
        _selectedTrack.value = _tracks.value.find { it.id == trackId }
    }
    
    fun getTrackById(trackId: Int): Track? = _tracks.value.find { it.id == trackId }
}
