package com.sctp.mcside.core.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.accompanist.permissions.*

/**
 * Moderner PermissionManager unter Verwendung von Accompanist Permissions für Compose.
 * Unterstützt Standard-Runtime-Berechtigungen sowie spezielle System-Berechtigungen.
 */
object PermissionManager {

    // Liste der angeforderten Standard-Berechtigungen
    val REQUIRED_PERMISSIONS = mutableListOf<String>().apply {
        add(Manifest.permission.INTERNET)
        add(Manifest.permission.WAKE_LOCK)
        add(Manifest.permission.VIBRATE)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            add(Manifest.permission.FOREGROUND_SERVICE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Spezialfall für Android 14+ Foreground Services
            add("android.permission.FOREGROUND_SERVICE_SPECIAL_USE")
        }

        // Legacy Storage Permissions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        
        // Custom Termux Permission
        add("com.termux.permission.RUN_COMMAND")
    }

    /**
     * Prüft "All Files Access" für Android 11+
     */
    fun hasAllFilesAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    /**
     * Prüft, ob die App Akku-Optimierungen ignoriert.
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    /**
     * Prüft globale Berechtigungs-Anforderungen.
     */
    fun hasRequiredPermissions(context: Context): Boolean {
        val standardGranted = REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        return standardGranted && hasAllFilesAccess()
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun rememberPermissionRequest(
        onAllGranted: () -> Unit = {},
        onDenied: (List<String>) -> Unit = {}
    ): PermissionRequestState {
        val context = LocalContext.current
        
        // Accompanist State für Standard-Berechtigungen
        val multiplePermissionsState = rememberMultiplePermissionsState(permissions = REQUIRED_PERMISSIONS)
        
        // Launcher für Spezial-Berechtigungen (Settings-Intents)
        val allFilesLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (hasAllFilesAccess()) checkAll(multiplePermissionsState, onAllGranted)
        }

        val batteryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // Rückmeldung optional
        }

        val installLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // Rückmeldung optional
        }

        return remember(multiplePermissionsState) {
            PermissionRequestState(
                requestPermissions = {
                    multiplePermissionsState.launchMultiplePermissionRequest()
                    
                    // Trigger spezielle System-Dialoge falls nötig
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !hasAllFilesAccess()) {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                            data = "package:${context.packageName}".toUri()
                        }
                        try { allFilesLauncher.launch(intent) } catch (_: Exception) {
                            allFilesLauncher.launch(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                        }
                    }
                },
                requestBatteryExemption = {
                    if (!isIgnoringBatteryOptimizations(context)) {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = "package:${context.packageName}".toUri()
                        }
                        batteryLauncher.launch(intent)
                    }
                },
                requestInstallPackages = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (!context.packageManager.canRequestPackageInstalls()) {
                            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                                data = "package:${context.packageName}".toUri()
                            }
                            installLauncher.launch(intent)
                        }
                    }
                },
                multiplePermissionsState = multiplePermissionsState
            )
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    private fun checkAll(state: MultiplePermissionsState, onAllGranted: () -> Unit) {
        if (state.allPermissionsGranted && hasAllFilesAccess()) {
            onAllGranted()
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    data class PermissionRequestState(
        val requestPermissions: () -> Unit,
        val requestBatteryExemption: () -> Unit,
        val requestInstallPackages: () -> Unit,
        val multiplePermissionsState: MultiplePermissionsState
    ) {
        val allGranted: Boolean get() = multiplePermissionsState.allPermissionsGranted && hasAllFilesAccess()
        val shouldShowRationale: Boolean get() = multiplePermissionsState.shouldShowRationale
    }
}