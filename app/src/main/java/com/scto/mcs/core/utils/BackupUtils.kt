package com.scto.mcside.core.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object BackupUtils {
    // Keep the most recent 5 backups
    private const val MAX_BACKUP_COUNT = 5

    /**
     * Backs up a project by zipping it into the app's private directory.
     */
    suspend fun backupProject(context: Context, projectPath: String): String = withContext(Dispatchers.IO) {
        val projectDir = File(projectPath)
        if (!projectDir.exists()) return@withContext "Project does not exist"

        val folderName = projectDir.name
        // Private directory: /data/data/pkg/files/project_backups/projectName/
        val backupRootDir = File(context.filesDir, "project_backups/$folderName")
        if (!backupRootDir.exists()) backupRootDir.mkdirs()

        // Naming: ProjectName_Timestamp.zip
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val backupFile = File(backupRootDir, "${folderName}_$timestamp.zip")

        try {
            zipFolder(projectDir, backupFile) { file ->
                val path = file.absolutePath
                // Filter out build, .git, and .gradle directories for smaller backups
                !path.contains("/build/") && !path.contains("/.git/") && !path.contains("/.gradle/")
            }
            cleanOldBackups(backupRootDir)
            return@withContext backupFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext "Fail: ${e.message}"
        }
    }

    private fun zipFolder(srcFolder: File, destZipFile: File, filter: (File) -> Boolean) {
        ZipOutputStream(BufferedOutputStream(FileOutputStream(destZipFile))).use { zos ->
            addFolderToZip(srcFolder, srcFolder, zos, filter)
        }
    }

    private fun addFolderToZip(rootFolder: File, srcFolder: File, zos: ZipOutputStream, filter: (File) -> Boolean) {
        val files = srcFolder.listFiles() ?: return
        for (file in files) {
            if (!filter(file)) continue
            if (file.isDirectory) {
                addFolderToZip(rootFolder, file, zos, filter)
            } else {
                val relPath = file.toRelativeString(rootFolder)
                zos.putNextEntry(ZipEntry(relPath))
                BufferedInputStream(FileInputStream(file)).use { bis ->
                    bis.copyTo(zos)
                }
                zos.closeEntry()
            }
        }
    }

    private fun cleanOldBackups(backupDir: File) {
        val files = backupDir.listFiles { _, name -> name.endsWith(".zip") } ?: return
        if (files.size > MAX_BACKUP_COUNT) {
            files.sortBy { it.lastModified() }
            // Delete the oldest files until we are within the limit
            files.take(files.size - MAX_BACKUP_COUNT).forEach { it.delete() }
        }
    }
}