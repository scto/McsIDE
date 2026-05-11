package com.scto.mcside.core.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mcside_log_config")

data class LogEntry(
    val timestamp: Long,
    val level: String,
    val tag: String,
    val message: String
)

data class LogConfigState(
    val isLogEnabled: Boolean = true,
    val logFilePath: String = "",
    val isLoaded: Boolean = false
)

class LogConfigRepository(private val context: Context) {
    private object PreferencesKeys {
        val LOG_ENABLED = booleanPreferencesKey("log_enabled")
        val LOG_FILE_PATH = stringPreferencesKey("log_file_path")
    }

    /**
     * ✅ Core Fix: Use combine to merge DataStore and WorkspaceManager flows.
     * This ensures the state is recalculated whenever log settings OR the workspace directory change.
     */
    val logConfigFlow: Flow<LogConfigState> = context.dataStore.data
        .combine(WorkspaceManager.getWorkspacePathFlow(context)) { preferences, workspacePath ->

            // 1. Get the dynamic workspace directory from the Flow
            
            // 2. Build default log directory: workspacePath/logs
            val defaultLogPath = File(workspacePath, "logs").absolutePath

            // 3. Determine final path:
            // If a custom path is saved in DataStore, use it.
            // Otherwise, automatically follow the current workspace directory.
            val savedPath = preferences[PreferencesKeys.LOG_FILE_PATH]
            val finalPath = if (savedPath.isNullOrEmpty()) defaultLogPath else savedPath

            LogConfigState(
                isLogEnabled = preferences[PreferencesKeys.LOG_ENABLED] ?: true,
                logFilePath = finalPath,
                isLoaded = true
            )
        }

    suspend fun saveLogConfig(isEnabled: Boolean, filePath: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOG_ENABLED] = isEnabled

            // Optional optimization: If the user-selected path matches the default path,
            // store it as null/empty so it continues to follow workspace changes automatically.
            val currentWorkspace = WorkspaceManager.getWorkspacePath(context)
            val defaultPath = File(currentWorkspace, "logs").absolutePath

            if (filePath == defaultPath) {
                preferences.remove(PreferencesKeys.LOG_FILE_PATH)
            } else {
                preferences[PreferencesKeys.LOG_FILE_PATH] = filePath
            }
        }
    }

    suspend fun resetLogPath() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.LOG_FILE_PATH)
        }
    }
}

object LogCatcher {
    @Volatile
    private var logConfig: LogConfigState? = null

    @Volatile
    private var isInitialized = false

    private val _logFlow = MutableSharedFlow<LogEntry>(extraBufferCapacity = 1000)
    val logFlow = _logFlow.asSharedFlow()

    // Scope for asynchronous file writing operations
    private val logScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + CoroutineName("LogCatcherScope"))

    // Used to store build log history
    private val _buildLogs = Collections.synchronizedList(ArrayList<LogEntry>())

    // ThreadLocal for SimpleDateFormat to ensure thread-safe and efficient formatting
    private val dateFormat = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat {
            return SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        }
    }

    @JvmStatic
    fun getBuildLogs(): List<LogEntry> {
        synchronized(_buildLogs) {
            return ArrayList(_buildLogs)
        }
    }

    @JvmStatic
    fun clearBuildLogs() {
        _buildLogs.clear()
    }

    @JvmStatic
    fun updateConfig(config: LogConfigState) {
        logConfig = config
        isInitialized = true
        i("LogCatcher", "Log system configured - Enabled: ${config.isLogEnabled}, Path: ${config.logFilePath}")
    }

    @JvmStatic
    fun d(tag: String, message: String) {
        android.util.Log.d(tag, message)
        emitLog("DEBUG", tag, message)
        writeToFile("DEBUG", tag, message)
    }

    @JvmStatic
    fun i(tag: String, message: String) {
        android.util.Log.i(tag, message)
        emitLog("INFO", tag, message)
        writeToFile("INFO", tag, message)
    }

    @JvmStatic
    fun w(tag: String, message: String) {
        android.util.Log.w(tag, message)
        emitLog("WARN", tag, message)
        writeToFile("WARN", tag, message)
    }

    @JvmStatic
    @JvmOverloads
    fun e(tag: String, message: String, exception: Throwable? = null) {
        android.util.Log.e(tag, message, exception)
        val msg = "$message${exception?.let { " - ${it.message}" } ?: ""}"
        emitLog("ERROR", tag, msg)
        writeToFile("ERROR", tag, msg)
    }

    private fun emitLog(level: String, tag: String, message: String) {
        val entry = LogEntry(System.currentTimeMillis(), level, tag, message)
        
        // Save build-related logs to history
        if (tag == "ApkBuilder" || tag == "Build") {
            _buildLogs.add(entry)
        }
        
        _logFlow.tryEmit(entry)
    }

    private fun writeToFile(level: String, tag: String, message: String) {
        val config = logConfig ?: return
        if (!config.isLogEnabled || config.logFilePath.isEmpty()) return

        logScope.launch {
            try {
                val logDir = File(config.logFilePath)
                if (!logDir.exists()) {
                    logDir.mkdirs()
                }
                val logFile = File(logDir, "webide.log")
                val timestamp = dateFormat.get()?.format(Date()) ?: System.currentTimeMillis().toString()
                val logEntry = "[$timestamp] [$level] [$tag] $message\n"
                logFile.appendText(logEntry)
            } catch (e: Exception) {
                android.util.Log.e("LogCatcher", "Failed to write log to file: ${e.message}")
            }
        }
    }
}

/**
 * LogConfig.kt
 * ✅ Thread-Sicherheit: logConfig in LogCatcher ist jetzt mit @Volatile markiert, um eine korrekte Sichtbarkeit über Threads hinweg zu gewährleisten.
 * ✅ Coroutine-Management: GlobalScope wurde durch einen dedizierten logScope ersetzt. Die Verwendung von GlobalScope gilt als "Delicate API" und sollte in Android-Apps vermieden werden, da sie die strukturierte Nebenläufigkeit (Structured Concurrency) untergräbt.
 * ✅ Performance-Optimierung: * Das SimpleDateFormat wird nun einmalig (per ThreadLocal) erstellt, statt bei jedem Log-Schreibvorgang eine neue Instanz zu erzeugen.
 * ✅ Imports wurden bereinigt.
 * ✅ Logging: Die Fehlerbehandlung beim Schreiben in Dateien gibt nun detailliertere Informationen aus.
 
 
 
​WorkspaceManager: Die Pfadvalidierung wurde robuster gestaltet, um fehlerhafte Verweise auf Verzeichnisse anderer Apps sicher zu verhindern.

​ThemeDataStore: Die Konvertierung zwischen Compose-Farben und Long-Werten wurde präzisiert, um Plattform-Unterschiede bei der Farbdarstellung zu vermeiden.

​BackupUtils: Der ZIP-Vorgang wurde durch Pufferung optimiert und die Dateifilterung ist nun effizienter.

​PermissionManager: Die Logik für den Zugriff auf den "All Files"-Speicher unter Android 11+ wurde verfeinert.

​CodeFormatter: Die String-Manipulationen wurden performanter gestaltet (z.B. durch Vermeidung unnötiger Regex-Aufrufe in Schleifen).

Null-Sicherheit: Verstärkter Einsatz von sicheren Aufrufen (?.) und Standardwerten.
​Performance: In BackupUtils wurden BufferedStreams hinzugefügt, um den ZIP-Vorgang zu beschleunigen. In CodeFormatter wurde die Logik für JS-Einrückungen optimiert.
​Stabilität: Die Pfadprüfung im WorkspaceManager ist nun unempfindlicher gegenüber verschiedenen Android-Mount-Points.
​Haben Sie noch weitere Dateien CodeFormatter

**/