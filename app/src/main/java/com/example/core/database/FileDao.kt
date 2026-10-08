package com.example.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {

    @Query("SELECT * FROM project_files WHERE projectId = :projectId ORDER BY isDirectory DESC, path ASC")
    fun getFilesForProject(projectId: String): Flow<List<ProjectFileEntity>>

    @Query("SELECT * FROM project_files WHERE projectId = :projectId AND path = :path LIMIT 1")
    fun getFile(projectId: String, path: String): Flow<ProjectFileEntity?>

    @Query("SELECT * FROM project_files WHERE projectId = :projectId AND isDirectory = 0")
    suspend fun getAllDirectFiles(projectId: String): List<ProjectFileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: ProjectFileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<ProjectFileEntity>)

    @Query("UPDATE project_files SET content = :content, sizeBytes = :sizeBytes, lastModified = :lastModified, isDirty = 1 WHERE projectId = :projectId AND path = :path")
    suspend fun updateFileContent(projectId: String, path: String, content: String, sizeBytes: Long, lastModified: Long)

    @Query("UPDATE project_files SET isExpanded = :isExpanded WHERE projectId = :projectId AND path = :path")
    suspend fun setFolderExpanded(projectId: String, path: String, isExpanded: Boolean)

    @Query("DELETE FROM project_files WHERE projectId = :projectId AND path = :path")
    suspend fun deleteFile(projectId: String, path: String)

    @Query("DELETE FROM project_files WHERE projectId = :projectId AND (path = :dirPath OR path LIKE :dirPrefix)")
    suspend fun deleteDirectoryAndChildren(projectId: String, dirPath: String, dirPrefix: String)

    @Query("DELETE FROM project_files WHERE projectId = :projectId")
    suspend fun deleteAllProjectFiles(projectId: String)

    @Query("SELECT COUNT(*) FROM project_files WHERE projectId = :projectId")
    suspend fun countProjectFiles(projectId: String): Int
}
