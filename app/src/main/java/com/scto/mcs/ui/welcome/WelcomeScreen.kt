package com.scto.mcside.ui.welcome

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

import com.web.webide.core.utils.PermissionManager
import com.web.webide.ui.ThemeViewModel
import com.web.webide.ui.components.ColorPickerDialog
import com.web.webide.ui.components.WebIDE_Icon
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalPermissionsApi::class)
@Composable
fun WelcomeScreen(
    themeViewModel: ThemeViewModel,
    onWelcomeFinished: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val themeState by themeViewModel.themeState.collectAsState()
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })

    // Moderne Berechtigungsverwaltung initialisieren
    val permissionState = PermissionManager.rememberPermissionRequest(
        onAllGranted = { /* Optional: Automatisch zur nächsten Seite springen */ }
    )

    // Status-Variablen für die UI-Anzeige
    var storageGranted by remember { mutableStateOf(PermissionManager.hasAllFilesAccess()) }
    var installGranted by remember { 
        mutableStateOf(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else true)
    }

    var showColorPicker by remember { mutableStateOf(false) }
    var customColor by remember { mutableStateOf(themeState.customColor) }
    var selectedModeIndex by remember { mutableIntStateOf(themeState.selectedModeIndex) }
    var selectedThemeIndex by remember {
        mutableIntStateOf(if (themeState.isCustomTheme) themeColors.size else themeState.selectedThemeIndex)
    }
    var isMonetEnabled by remember { mutableStateOf(themeState.isMonetEnabled) }

    val systemDark = isSystemInDarkTheme()
    val isDarkTheme = remember(selectedModeIndex, systemDark) {
        when (selectedModeIndex) {
            1 -> false
            2 -> true
            else -> systemDark
        }
    }

    // --- Themen-Vorschau Logik ---
    val currentPreviewTheme: ThemeColor? = remember(selectedThemeIndex, customColor, isMonetEnabled, isDarkTheme) {
        if (isMonetEnabled) {
            null
        } else if (selectedThemeIndex < themeColors.size) {
            themeColors[selectedThemeIndex]
        } else {
            // Benutzerdefinierter Modus
            val bgDark = Color(0xFF121212)
            val bgLight = Color(0xFFF8F9FA)

            val customSpecDark = ThemeColorSpec(
                background = bgDark,
                surface = Color(0xFF1E1E1E),
                primary = customColor,
                accent = customColor
            )
            val customSpecLight = ThemeColorSpec(
                background = bgLight,
                surface = Color.White,
                primary = customColor,
                accent = customColor
            )
            ThemeColor("Custom", customSpecDark, customSpecLight)
        }
    }

    val targetBg = if (isMonetEnabled) {
        if (isDarkTheme) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerLowest
    } else if (selectedThemeIndex < themeColors.size) {
        val theme = themeColors[selectedThemeIndex]
        if (isDarkTheme) theme.dark.background else theme.light.background
    } else {
        MaterialTheme.colorScheme.background
    }

    val animatedBgColor by animateColorAsState(targetBg, tween(600), label = "bg_color")
    val contentColor by animateColorAsState(
        if (animatedBgColor.luminance() > 0.45f) Color.Black else Color.White,
        tween(600),
        label = "content_color"
    )

    // Beobachtet Rückkehr aus den Systemeinstellungen
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                storageGranted = PermissionManager.hasAllFilesAccess()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    installGranted = context.packageManager.canRequestPackageInstalls()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                val activeColor = when {
                    isMonetEnabled -> MaterialTheme.colorScheme.primary
                    selectedThemeIndex == themeColors.size -> customColor
                    else -> if (isDarkTheme) themeColors[selectedThemeIndex].dark.primary else themeColors[selectedThemeIndex].light.primary
                }

                WelcomeBottomBar(
                    pagerState = pagerState,
                    activeColor = activeColor,
                    isLastPage = pagerState.currentPage == 2,
                    onBack = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                    onNext = {
                        if (pagerState.currentPage < 2) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else {
                            themeViewModel.saveThemeConfig(
                                selectedModeIndex, selectedThemeIndex, customColor, isMonetEnabled,
                                selectedThemeIndex == themeColors.size
                            )
                            onWelcomeFinished()
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                WelcomeBackground(
                    currentTheme = currentPreviewTheme,
                    isDarkTheme = isDarkTheme,
                    monetPrimary = MaterialTheme.colorScheme.primary,
                    monetTertiary = MaterialTheme.colorScheme.tertiary
                )

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) { page ->
                    when (page) {
                        0 -> IntroContent()
                        1 -> PermissionsContent(
                            storageGranted = storageGranted,
                            installGranted = installGranted,
                            onRequestStoragePermission = { permissionState.requestPermissions() },
                            onRequestInstallPermission = { permissionState.requestInstallPackages() }
                        )
                        2 -> ThemeSetupContent(
                            selectedModeIndex = selectedModeIndex,
                            selectedThemeIndex = selectedThemeIndex,
                            isMonetEnabled = isMonetEnabled,
                            isDarkTheme = isDarkTheme,
                            onMonetToggle = { isMonetEnabled = it },
                            onModeSelected = { selectedModeIndex = it },
                            onThemeSelected = { selectedThemeIndex = it },
                            onCustomColorClick = {
                                selectedThemeIndex = themeColors.size
                                showColorPicker = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = customColor,
            onDismiss = { showColorPicker = false },
            onColorSelected = { color ->
                customColor = color
                showColorPicker = false
                selectedThemeIndex = themeColors.size
            }
        )
    }
}

@Composable
private fun IntroContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(250.dp)) { WebIDE_Icon() }
            Text(
                "WebIDE",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Zukunft auf Android bauen",
                style = MaterialTheme.typography.titleMedium,
                color = LocalContentColor.current.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun PermissionsContent(
    storageGranted: Boolean,
    installGranted: Boolean,
    onRequestStoragePermission: () -> Unit,
    onRequestInstallPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Erforderliche Rechte",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "WebIDE benötigt Zugriff auf den Speicher, um Projekte zu verwalten und Apps zu installieren.",
            style = MaterialTheme.typography.bodyMedium,
            color = LocalContentColor.current.copy(alpha = 0.8f)
        )

        Spacer(Modifier.height(32.dp))

        PermissionCard(
            Icons.Default.Folder,
            "Dateizugriff",
            "Wichtig für Projektdateien",
            storageGranted,
            onRequestStoragePermission
        )
        Spacer(Modifier.height(12.dp))
        PermissionCard(
            Icons.Default.Download,
            "App-Installation",
            "Erlaubt das Testen erstellter APKs",
            installGranted,
            onRequestInstallPermission
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSetupContent(
    selectedModeIndex: Int,
    selectedThemeIndex: Int,
    isMonetEnabled: Boolean,
    isDarkTheme: Boolean,
    onMonetToggle: (Boolean) -> Unit,
    onModeSelected: (Int) -> Unit,
    onThemeSelected: (Int) -> Unit,
    onCustomColorClick: () -> Unit
) {
    val modeOptions = listOf("System", "Hell", "Dunkel")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Design & Stil",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(32.dp))

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            modeOptions.forEachIndexed { index, label ->
                SegmentedButton(
                    selected = selectedModeIndex == index,
                    onClick = { onModeSelected(index) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = modeOptions.size),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        activeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        inactiveContainerColor = Color.Transparent,
                        inactiveContentColor = LocalContentColor.current
                    )
                ) { Text(label) }
            }
        }

        Spacer(Modifier.height(24.dp))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ListItem(
                headlineContent = { Text("Dynamische Farben (Monet)") },
                trailingContent = { Switch(checked = isMonetEnabled, onCheckedChange = onMonetToggle) },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                    headlineColor = LocalContentColor.current,
                    trailingIconColor = LocalContentColor.current
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        AnimatedVisibility(visible = !isMonetEnabled) {
            Column {
                Spacer(Modifier.height(24.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(themeColors) { index, theme ->
                        ThemePreviewCard(
                            theme = theme,
                            isSelected = selectedThemeIndex == index,
                            isDarkTheme = isDarkTheme,
                            onClick = { onThemeSelected(index) }
                        )
                    }
                    item {
                        CustomThemeCard(
                            isSelected = selectedThemeIndex == themeColors.size,
                            onClick = onCustomColorClick
                        )
                    }
                }
            }
        }
    }
}