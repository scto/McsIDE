/*
 * McsIDE - A powerful IDE for Android development.
 */

package com.scto.mcside.ui.projects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import com.scto.mcside.R
import com.scto.mcside.core.utils.LogConfigRepository
import com.scto.mcside.core.utils.PermissionManager
import com.scto.mcside.core.utils.WorkspaceManager
import com.scto.mcside.ui.components.DirectorySelector

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceSelectionScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Get current path on initialization
    var selectedWorkspace by remember { mutableStateOf(WorkspaceManager.getWorkspacePath(context)) }
    var showFileSelector by remember { mutableStateOf(false) }

    // Permission request callback
    val permissionState = PermissionManager.rememberPermissionRequest(
        onPermissionGranted = {
            saveAndNavigate(context, selectedWorkspace, navController, scope)
        },
        onPermissionDenied = { /* Optional: Hint user */ }
    )

    // ✅ Fix 1: Check if already configured on page entry.
    // If configured, navigate to main list immediately.
    LaunchedEffect(Unit) {
        if (WorkspaceManager.isWorkspaceConfigured(context)) {
            navController.navigate("project_list") {
                popUpTo("workspace_selection") { inclusive = true }
            }
        } else {
            // Suggest false to let users see UI text first
            showFileSelector = false
        }
    }

    // Only render UI if not configured to avoid flickering
    if (!WorkspaceManager.isWorkspaceConfigured(context)) {
        Scaffold(
            topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.FolderOpen,
                    null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    stringResource(R.string.ws_select_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(R.string.ws_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (selectedWorkspace.contains("Android/data")) {
                    Text(
                        stringResource(R.string.ws_recommended),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { showFileSelector = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.FolderOpen, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.ws_change_dir))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.ws_current), style = MaterialTheme.typography.bodySmall)
                        Text(
                            selectedWorkspace,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (PermissionManager.isSystemPermissionRequiredForPath(
                                context,
                                selectedWorkspace
                            )
                        ) {
                            permissionState.requestPermissions()
                        } else {
                            saveAndNavigate(context, selectedWorkspace, navController, scope)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.ws_confirm_continue))
                }
            }
        }
    }

    if (showFileSelector) {
        DirectorySelector(
            initialPath = selectedWorkspace,
            onPathSelected = { selectedWorkspace = it; showFileSelector = false },
            onDismissRequest = { showFileSelector = false }
        )
    }
}

private fun saveAndNavigate(
    context: android.content.Context,
    path: String,
    navController: NavController,
    scope: kotlinx.coroutines.CoroutineScope
) {
    WorkspaceManager.saveWorkspacePath(context, path)

    scope.launch {
        try {
            LogConfigRepository(context).resetLogPath()
        } catch (_: Exception) {
        }
    }
    navController.navigate("project_list") {
        popUpTo("workspace_selection") { inclusive = true }
    }
}