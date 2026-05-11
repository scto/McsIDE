/*
 * WebIDE - A powerful IDE for Android web development.
 */

package com.scto.mcside.ui.projects

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import com.scto.mcside.R
import com.scto.mcside.core.utils.WorkspaceManager

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import androidx.core.content.edit

import com.scto.mcside.safeNavigate

// --- Constants ---
private const val PREFS_NAME = "project_prefs"
private const val KEY_PINNED_PROJECTS = "pinned_projects"
private const val KEY_SORT_ORDER = "sort_order"
private const val KEY_SEARCH_HISTORY = "search_history"

// --- Data Model ---
data class ProjectItem(
    val name: String,
    val lastModified: Long
)

// --- Sort Order Enum ---
enum class SortOrder(val stringRes: Int) {
    NAME_ASC(R.string.project_sort_name_asc),
    NAME_DESC(R.string.project_sort_name_desc),
    DATE_NEWEST(R.string.project_sort_date_newest),
    DATE_OLDEST(R.string.project_sort_date_oldest);

    companion object {
        fun fromOrdinal(ordinal: Int): SortOrder = entries.getOrElse(ordinal) { NAME_ASC }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProjectListScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val projectDirPath = WorkspaceManager.getWorkspacePath(context)
    val projectDir = File(projectDirPath)

    // --- Core Data States ---
    var projectList by remember { mutableStateOf<List<ProjectItem>>(emptyList()) }
    var pinnedProjects by remember { mutableStateOf<Set<String>>(emptySet()) }
    var currentSortOrder by remember { mutableStateOf(SortOrder.NAME_ASC) }

    // --- Search Related States ---
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchHistory by remember { mutableStateOf<List<String>>(emptyList()) }

    // --- UI Interaction States ---
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var projectToDelete by remember { mutableStateOf<String?>(null) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    // FAB Logic: Hide when searching, expand based on scroll position
    val isFabExpanded by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    // Physical back button handling: Exit search mode if active
    BackHandler(enabled = isSearchActive) {
        isSearchActive = false
        searchQuery = ""
    }

    // --- Persistence Utility Functions ---
    fun saveSearchHistory(query: String) {
        if (query.isBlank()) return
        val newHistory = (listOf(query) + searchHistory)
            .distinct()
            .take(10)

        searchHistory = newHistory
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putString(KEY_SEARCH_HISTORY, newHistory.joinToString("\n")) }
    }

    fun deleteHistoryItem(item: String) {
        val newHistory = searchHistory - item
        searchHistory = newHistory
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putString(KEY_SEARCH_HISTORY, newHistory.joinToString("\n")) }
    }

    fun loadHistory() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val historyStr = prefs.getString(KEY_SEARCH_HISTORY, "") ?: ""
        if (historyStr.isNotEmpty()) {
            searchHistory = historyStr.split("\n")
        }
    }

    // --- List Refresh Logic ---
    fun refreshList() {
        scope.launch(Dispatchers.IO) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedPinned = prefs.getStringSet(KEY_PINNED_PROJECTS, emptySet()) ?: emptySet()
            val sortOrdinal = prefs.getInt(KEY_SORT_ORDER, SortOrder.NAME_ASC.ordinal)
            val sortOrder = SortOrder.fromOrdinal(sortOrdinal)

            withContext(Dispatchers.Main) {
                pinnedProjects = savedPinned
                currentSortOrder = sortOrder
            }

            if (projectDir.exists() && projectDir.isDirectory) {
                val rawFiles = projectDir.listFiles { file ->
                    file.isDirectory && file.name != "logs"
                } ?: emptyArray()

                val items = rawFiles.map {
                    ProjectItem(name = it.name, lastModified = it.lastModified())
                }

                // Apply sorting
                val sortedList = items.sortedWith(
                    compareByDescending<ProjectItem> { it.name in savedPinned }
                        .then(
                            when (sortOrder) {
                                SortOrder.NAME_ASC -> compareBy { it.name.lowercase() }
                                SortOrder.NAME_DESC -> compareByDescending { it.name.lowercase() }
                                SortOrder.DATE_NEWEST -> compareByDescending { it.lastModified }
                                SortOrder.DATE_OLDEST -> compareBy { it.lastModified }
                            }
                        )
                )
                withContext(Dispatchers.Main) { projectList = sortedList }
            } else {
                withContext(Dispatchers.Main) { projectList = emptyList() }
            }
        }
    }

    // --- Action Logic ---
    fun togglePin(folderName: String) {
        val newPinned = pinnedProjects.toMutableSet()
        if (newPinned.contains(folderName)) newPinned.remove(folderName) else newPinned.add(folderName)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putStringSet(KEY_PINNED_PROJECTS, newPinned) }
        refreshList()
    }

    fun changeSortOrder(newOrder: SortOrder) {
        currentSortOrder = newOrder
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putInt(KEY_SORT_ORDER, newOrder.ordinal) }
        refreshList()
    }

    fun deleteProject(folderName: String) {
        scope.launch(Dispatchers.IO) {
            val targetDir = File(projectDir, folderName)
            val success = targetDir.deleteRecursively()
            withContext(Dispatchers.Main) {
                if (success) {
                    if (pinnedProjects.contains(folderName)) togglePin(folderName) else refreshList()
                    snackbarHostState.showSnackbar(context.getString(R.string.project_deleted_msg))
                } else {
                    snackbarHostState.showSnackbar(context.getString(R.string.project_delete_fail))
                }
            }
        }
    }

    // Initialization
    LaunchedEffect(projectDir) {
        refreshList()
        loadHistory()
    }

    // --- Calculated Display List ---
    val displayList = remember(projectList, isSearchActive, searchQuery) {
        if (isSearchActive && searchQuery.isNotEmpty()) {
            projectList.filter { it.name.contains(searchQuery, ignoreCase = true) }
        } else {
            projectList
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AnimatedContent(
                targetState = isSearchActive,
                transitionSpec = {
                    if (targetState) {
                        (fadeIn(tween(300)) + slideInVertically(tween(300)) { -it }).togetherWith(fadeOut(tween(300)))
                    } else {
                        fadeIn(tween(300)).togetherWith(fadeOut(tween(300)) + slideOutVertically(tween(300)) { -it })
                    }
                },
                label = "TopBarAnimation"
            ) { active ->
                if (active) {
                    // --- Search Mode TopBar ---
                    TopAppBar(
                        title = {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text(stringResource(R.string.project_search_placeholder), style = MaterialTheme.typography.bodyLarge) },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                textStyle = MaterialTheme.typography.bodyLarge,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = {
                                    saveSearchHistory(searchQuery)
                                    focusManager.clearFocus()
                                }),
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                isSearchActive = false
                                searchQuery = ""
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                            }
                        },
                        actions = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, null)
                                }
                            }
                        }
                    )
                } else {
                    // --- Normal Mode TopBar ---
                    LargeTopAppBar(
                        title = { Text(stringResource(R.string.project_list_title)) },
                        scrollBehavior = scrollBehavior,
                        actions = {
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(Icons.Default.Search, null)
                            }
                            Box {
                                IconButton(onClick = { showSortMenu = true }) {
                                    Icon(Icons.AutoMirrored.Filled.Sort, null)
                                }
                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false }
                                ) {
                                    SortOrder.entries.forEach { order ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (order == currentSortOrder) {
                                                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                                                        Spacer(Modifier.width(8.dp))
                                                    } else {
                                                        Spacer(Modifier.width(24.dp))
                                                    }
                                                    Text(stringResource(order.stringRes))
                                                }
                                            },
                                            onClick = {
                                                changeSortOrder(order)
                                                showSortMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                            IconButton(onClick = { navController.safeNavigate("settings") }) {
                                Icon(Icons.Default.Settings, null)
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !isSearchActive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = { navController.safeNavigate("new_project") },
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text(stringResource(R.string.project_create_title)) },
                    expanded = isFabExpanded
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            val showHistory = isSearchActive && searchQuery.isEmpty()
            AnimatedContent(
                targetState = showHistory,
                transitionSpec = { fadeIn(tween(300)).togetherWith(fadeOut(tween(300))) },
                label = "ContentAnim"
            ) { isHistory ->
                // --- Scene 1: Search Mode with no input -> Show History ---
                if (isHistory) {
                if (searchHistory.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.project_no_history), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        item {
                            Text(
                                stringResource(R.string.project_search_history),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(searchHistory) { historyItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        searchQuery = historyItem
                                        saveSearchHistory(historyItem)
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(historyItem, style = MaterialTheme.typography.bodyLarge)
                                }
                                IconButton(
                                    onClick = { deleteHistoryItem(historyItem) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                        item {
                            TextButton(
                                onClick = {
                                    searchHistory = emptyList()
                                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                                        .edit { remove(KEY_SEARCH_HISTORY) }
                                },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            ) {
                                Text(stringResource(R.string.project_clear_history), color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }
            // --- Scene 2: Project List (Normal or Search Filtering) ---
            else {
                if (displayList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isSearchActive) stringResource(R.string.project_no_match) else stringResource(R.string.project_no_projects),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = 16.dp,
                            end = 16.dp,
                            bottom = if (isSearchActive) 16.dp else 100.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(displayList, key = { it.name }) { item ->
                            ProjectCard(
                                modifier = Modifier.animateItem(
                                    placementSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                ),
                                folderName = item.name,
                                isPinned = pinnedProjects.contains(item.name),
                                onClick = {
                                    navController.safeNavigate("code_edit/${item.name}")
                                },
                                onTogglePin = { togglePin(item.name) },
                                onDelete = {
                                    projectToDelete = item.name
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
            }
        }

        if (showDeleteDialog && projectToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(stringResource(R.string.project_delete_title)) },
                text = { Text(stringResource(R.string.project_delete_msg, projectToDelete!!)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            deleteProject(projectToDelete!!)
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text(stringResource(R.string.action_delete)) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.action_cancel)) }
                }
            )
        }
    }
}

@Composable
fun ProjectCard(
    modifier: Modifier = Modifier,
    folderName: String,
    isPinned: Boolean,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(85.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = folderName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, stringResource(R.string.welcome_theme_title))
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (isPinned) stringResource(R.string.project_unpin) else stringResource(R.string.project_pin)) },
                            leadingIcon = {
                                Icon(if (isPinned) Icons.Default.PushPin else Icons.Default.VerticalAlignTop, null)
                            },
                            onClick = {
                                menuExpanded = false
                                onTogglePin()
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            if (isPinned) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = "Pinned",
                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 5.dp, end = 5.dp)
                        .size(16.dp)
                        .rotate(45f)
                )
            }
        }
    }
}