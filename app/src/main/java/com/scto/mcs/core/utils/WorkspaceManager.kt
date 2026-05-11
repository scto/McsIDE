package com.scto.mcside.core.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File

object WorkspaceManager {
    private const val PREFS_NAME = "mcside_prefs"
    private const val KEY_WORKSPACE_PATH = "workspace_path"
    private const val KEY_IS_CONFIGURED = "is_workspace_configured"

    fun getDefaultPath(context: Context): String {
        val dir = context.getExternalFilesDir(null)
        return dir?.absolutePath ?: context.filesDir.absolutePath
    }

    /**
     * Retrieves the workspace path with automatic error correction logic.
     */
    fun getWorkspacePath(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedPath = prefs.getString(KEY_WORKSPACE_PATH, null)

        // 1. Return default if no path is saved
        if (savedPath.isNullOrBlank()) {
            return getDefaultPath(context)
        }

        // 2. Robust path validation:
        // Logic: If the path is inside "Android/data/", ensure it belongs to the current app's package.
        // This prevents access issues caused by path variations (e.g., /sdcard vs /storage/emulated/0).
        if (savedPath.contains("/Android/data/", ignoreCase = true)) {
            val packageName = context.packageName
            if (!savedPath.contains(packageName)) {
                android.util.Log.e("WorkspaceManager", "Invalid path detected (Package mismatch): $savedPath. Resetting to default.")
                val validPath = getDefaultPath(context)
                saveWorkspacePath(context, validPath) // Auto-correct and save
                return validPath
            }
        }

        return savedPath
    }

    fun isWorkspaceConfigured(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Returns true if the user has completed the initialization wizard.
        return prefs.getBoolean(KEY_IS_CONFIGURED, false)
    }

    fun getWorkspacePathFlow(context: Context): Flow<String> = callbackFlow {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_WORKSPACE_PATH) {
                trySend(getWorkspacePath(context))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(getWorkspacePath(context))
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun saveWorkspacePath(context: Context, path: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putString(KEY_WORKSPACE_PATH, path)
            // Mark as configured once the user confirms the workspace.
            putBoolean(KEY_IS_CONFIGURED, true)
        }
        ensurePathExists(context, path)
    }

    fun ensurePathExists(context: Context, path: String): Boolean {
        val file = File(path)
        if (file.exists() && file.isDirectory) return true

        return try {
            // Check if we have permission to create this directory (usually true for private storage).
            if (path.contains(context.packageName)) {
                file.mkdirs() || file.exists()
            } else {
                file.mkdirs() || file.exists()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}