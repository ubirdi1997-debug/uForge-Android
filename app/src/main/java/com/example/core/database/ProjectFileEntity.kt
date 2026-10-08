package com.example.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "project_files")
data class ProjectFileEntity(
    @PrimaryKey val id: String, // format: "$projectId:$path"
    val projectId: String,
    val path: String, // e.g., "src/ship.ts" or "src"
    val name: String, // e.g., "ship.ts" or "src"
    val parentPath: String, // e.g., "src" or "" for root
    val isDirectory: Boolean = false,
    val content: String = "",
    val extension: String = "",
    val sizeBytes: Long = 0L,
    val lastModified: Long = System.currentTimeMillis(),
    val isExpanded: Boolean = true,
    val isDirty: Boolean = false
)
