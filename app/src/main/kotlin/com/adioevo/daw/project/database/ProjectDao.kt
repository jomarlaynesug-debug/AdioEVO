package com.adioevo.daw.project.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.adioevo.daw.project.model.ProjectEntity
import com.adioevo.daw.project.model.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Insert
    suspend fun insertProject(project: ProjectEntity): Long
    
    @Update
    suspend fun updateProject(project: ProjectEntity)
    
    @Delete
    suspend fun deleteProject(project: ProjectEntity)
    
    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: Int): ProjectEntity?
    
    @Query("SELECT * FROM projects ORDER BY modifiedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>
    
    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: Int)
}

@Dao
interface TrackDao {
    @Insert
    suspend fun insertTrack(track: TrackEntity): Long
    
    @Update
    suspend fun updateTrack(track: TrackEntity)
    
    @Delete
    suspend fun deleteTrack(track: TrackEntity)
    
    @Query("SELECT * FROM tracks WHERE id = :id")
    suspend fun getTrackById(id: Int): TrackEntity?
    
    @Query("SELECT * FROM tracks WHERE projectId = :projectId ORDER BY trackNumber")
    fun getTracksForProject(projectId: Int): Flow<List<TrackEntity>>
    
    @Query("UPDATE tracks SET soundFontId = :soundFontId, bankMsb = :bankMsb, bankLsb = :bankLsb, program = :program WHERE id = :trackId")
    suspend fun updateTrackInstrument(
        trackId: Int,
        soundFontId: Int,
        bankMsb: Int,
        bankLsb: Int,
        program: Int
    )
    
    @Query("UPDATE tracks SET sustainMode = :sustainMode, timedSustainDuration = :duration WHERE id = :trackId")
    suspend fun updateTrackSustain(
        trackId: Int,
        sustainMode: String,
        duration: Long
    )
    
    @Query("DELETE FROM tracks WHERE projectId = :projectId")
    suspend fun deleteTracksForProject(projectId: Int)
}
