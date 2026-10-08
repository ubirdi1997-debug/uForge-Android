package com.example.domain.model

import com.example.core.database.ProjectFileEntity

data class FileTreeNode(
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val extension: String,
    val sizeBytes: Long,
    val lastModified: Long,
    val isExpanded: Boolean = true,
    val isDirty: Boolean = false,
    val children: List<FileTreeNode> = emptyList(),
    val depth: Int = 0
) {
    companion object {
        fun buildTree(entities: List<ProjectFileEntity>, expandedPaths: Set<String> = emptySet()): List<FileTreeNode> {
            // Build root items and nested children
            val nodeMap = mutableMapOf<String, MutableList<ProjectFileEntity>>()
            val directories = mutableSetOf<String>()

            // Extract implicit and explicit directories
            entities.forEach { entity ->
                val parent = entity.parentPath
                nodeMap.getOrPut(parent) { mutableListOf() }.add(entity)
                if (entity.isDirectory) {
                    directories.add(entity.path)
                }
            }

            fun createNode(entity: ProjectFileEntity, depth: Int): FileTreeNode {
                val isExp = if (expandedPaths.contains(entity.path)) true else entity.isExpanded
                val childEntities = nodeMap[entity.path] ?: emptyList()
                val sortedChildren = childEntities
                    .sortedWith(compareByDescending<ProjectFileEntity> { it.isDirectory }.thenBy { it.name })
                    .map { createNode(it, depth + 1) }

                return FileTreeNode(
                    path = entity.path,
                    name = entity.name,
                    isDirectory = entity.isDirectory,
                    extension = entity.extension,
                    sizeBytes = entity.sizeBytes,
                    lastModified = entity.lastModified,
                    isExpanded = isExp,
                    isDirty = entity.isDirty,
                    children = sortedChildren,
                    depth = depth
                )
            }

            val rootEntities = (nodeMap[""] ?: emptyList())
                .sortedWith(compareByDescending<ProjectFileEntity> { it.isDirectory }.thenBy { it.name })

            return rootEntities.map { createNode(it, 0) }
        }

        fun flattenVisibleNodes(nodes: List<FileTreeNode>, expandedPaths: Set<String>): List<FileTreeNode> {
            val result = mutableListOf<FileTreeNode>()
            fun traverse(node: FileTreeNode) {
                result.add(node)
                val isExp = expandedPaths.contains(node.path) || node.isExpanded
                if (node.isDirectory && isExp) {
                    node.children.forEach { traverse(it) }
                }
            }
            nodes.forEach { traverse(it) }
            return result
        }
    }
}
