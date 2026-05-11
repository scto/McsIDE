/*
 * WebIDE - A powerful IDE for Android web development.
 */

package com.scto.mcside.ui.projects

import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController

import com.scto.mcside.R
import com.scto.mcside.core.utils.WorkspaceManager
import com.scto.mcside.ui.components.ColorPickerDialog

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

import androidx.core.graphics.toColorInt

// --- Data Structures ---
enum class ProjectType { NORMAL, WEBAPP, WEBSITE }

data class SigningConfig(
    val enabled: Boolean,
    val path: String,
    val alias: String,
    val storePass: String,
    val keyPass: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewProjectScreen(navController: NavController) {
    // === 1. Core States ===
    var projectName by remember { mutableStateOf("") }
    var targetUrl by remember { mutableStateOf("https://") }
    var selectedType by remember { mutableStateOf(ProjectType.NORMAL) }
    var createIndexPhp by remember { mutableStateOf(false) }

    // === 2. Visual Configuration States (Full Mapping to webapp.json) ===
    var packageName by remember { mutableStateOf("com.example.myapp") }
    var versionName by remember { mutableStateOf("1.0.0") }
    var versionCode by remember { mutableStateOf("1") }
    var encryptionEnabled by remember { mutableStateOf(true) }
    var iconPath by remember { mutableStateOf("") }

    // [Display & Status Bar]
    var orientation by remember { mutableStateOf("portrait") }
    var isFullscreen by remember { mutableStateOf(false) }
    var statusBarColor by remember { mutableStateOf("#FFFFFF") }
    var isDarkStatusText by remember { mutableStateOf(true) }

    // [Webview]
    var zoomEnabled by remember { mutableStateOf(false) }

    // [Signing]
    var enableSigning by remember { mutableStateOf(false) }
    var keystorePath by remember { mutableStateOf("") }
    var keystoreAlias by remember { mutableStateOf("") }
    var storePassword by remember { mutableStateOf("") }
    var keyPassword by remember { mutableStateOf("") }

    // [UI Control]
    var showColorPicker by remember { mutableStateOf(false) }

    // Page entrance animation state
    var isScreenVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isScreenVisible = true }

    // === 3. Source & Control ===
    var jsonContent by remember { mutableStateOf("") }
    var jsonError by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var isInternalUpdate by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // === 4. Bidirectional Sync Logic ===
    fun syncJsonFromUi() {
        if (isInternalUpdate) return
        isInternalUpdate = true
        try {
            val root = JSONObject()
            root.put("name", projectName.ifBlank { "MyApp" })
            root.put("package", packageName)
            root.put("versionName", versionName)
            root.put("versionCode", versionCode.toIntOrNull() ?: 1)
            root.put("encryption", encryptionEnabled)

            val finalUrl = if (selectedType == ProjectType.WEBSITE) targetUrl else "index.html"
            root.put("targetUrl", finalUrl)
            root.put("icon", "icon.png")

            root.put("orientation", orientation)
            root.put("fullscreen", isFullscreen)

            val sb = JSONObject()
            sb.put("backgroundColor", statusBarColor)
            sb.put("style", if (isDarkStatusText) "dark" else "light")
            root.put("statusBar", sb)

            val wv = JSONObject()
            wv.put("zoomEnabled", zoomEnabled)
            root.put("webview", wv)

            if (enableSigning) {
                val sign = JSONObject()
                val ksName = if (keystorePath.endsWith(".jks", true)) "keystore.jks" else "keystore.keystore"
                sign.put("keystore", ksName)
                sign.put("alias", keystoreAlias)
                sign.put("storePassword", storePassword)
                sign.put("keyPassword", keyPassword)
                root.put("signing", sign)
            }

            root.put("permissions", org.json.JSONArray().put("android.permission.INTERNET"))
            jsonContent = root.toString(2)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isInternalUpdate = false
        }
    }

    fun syncUiFromJson(newJson: String) {
        jsonContent = newJson
        if (isInternalUpdate) return

        try {
            val root = JSONObject(newJson)
            jsonError = null
            isInternalUpdate = true

            if(root.has("name")) projectName = root.optString("name")
            if(root.has("package")) packageName = root.optString("package")
            if(root.has("encryption")) encryptionEnabled = root.optBoolean("encryption", true)
            if(root.has("versionName")) versionName = root.optString("versionName")
            if(root.has("versionCode")) versionCode = root.optInt("versionCode").toString()
            if(root.has("targetUrl")) targetUrl = root.optString("targetUrl")
            if(root.has("orientation")) orientation = root.optString("orientation")
            if(root.has("fullscreen")) isFullscreen = root.optBoolean("fullscreen")

            val sb = root.optJSONObject("statusBar")
            if (sb != null) {
                if(sb.has("backgroundColor")) statusBarColor = sb.optString("backgroundColor")
                if(sb.has("style")) isDarkStatusText = sb.optString("style") == "dark"
            }

            val wv = root.optJSONObject("webview")
            if (wv != null) {
                if(wv.has("zoomEnabled")) zoomEnabled = wv.optBoolean("zoomEnabled")
            }

            val sign = root.optJSONObject("signing")
            enableSigning = sign != null
            if (sign != null) {
                keystoreAlias = sign.optString("alias")
                storePassword = sign.optString("storePassword")
                keyPassword = sign.optString("keyPassword")
            }
            isInternalUpdate = false
        } catch (e: Exception) {
            jsonError = e.message
        }
    }

    LaunchedEffect(selectedType) {
        if (selectedType == ProjectType.WEBAPP) targetUrl = "index.html"
        else if (selectedType == ProjectType.WEBSITE && !targetUrl.startsWith("http")) targetUrl = "https://"
        if (selectedType != ProjectType.NORMAL) syncJsonFromUi()
    }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { if(it!=null) { iconPath=it.toString(); syncJsonFromUi() } }
    val keystoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { if(it!=null) { keystorePath=it.toString(); syncJsonFromUi() } }

    fun handleCreate() {
        if (projectName.isBlank()) { scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.new_project_err_name)) }; return }
        if (selectedType != ProjectType.NORMAL) {
            if (jsonError != null) { selectedTab = 1; scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.new_project_err_json)) }; return }
            if (enableSigning && keystorePath.isBlank()) { selectedTab = 0; scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.new_project_err_keystore)) }; return }
        }

        isLoading = true
        focusManager.clearFocus()
        if (selectedType != ProjectType.NORMAL) syncJsonFromUi()

        createNewProject(
            context, projectName,
            targetUrl,
            selectedType,
            SigningConfig(enableSigning, keystorePath, keystoreAlias, storePassword, keyPassword),
            iconPath,
            createIndexPhp,
            finalJsonContent = jsonContent,
            onSuccess = { dir ->
                isLoading = false
                scope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.new_project_success, dir.name))
                    delay(800)
                    navController.popBackStack()
                }
            },
            onError = { msg -> 
                isLoading = false
                scope.launch { snackbarHostState.showSnackbar(if (msg == "Project already exists") context.getString(R.string.new_project_err_exists) else msg) } 
            }
        )
    }

    if (showColorPicker) {
        val initialColorObj = try {
            Color(statusBarColor.toColorInt())
        } catch (_: Exception) {
            Color.White
        }

        ColorPickerDialog(
            initialColor = initialColorObj,
            onDismiss = { showColorPicker = false },
            onColorSelected = { color ->
                statusBarColor = String.format("#%06X", (0xFFFFFF and color.toArgb()))
                syncJsonFromUi()
                showColorPicker = false
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.new_project_title), fontSize = 18.sp, fontWeight = FontWeight.Medium) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = isScreenVisible,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                Surface(modifier = Modifier.fillMaxWidth().imePadding(), color = MaterialTheme.colorScheme.background) {
                    BouncyButton(
                        onClick = { handleCreate() },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().padding(20.dp).navigationBarsPadding().height(54.dp)
                    ) {
                        if (isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text(stringResource(R.string.new_project_create_btn), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        AnimatedVisibility(
            visible = isScreenVisible,
            enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(500, easing = FastOutSlowInEasing)) + fadeIn(tween(500)),
            modifier = Modifier.padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .animateContentSize(animationSpec = tween(300, easing = FastOutSlowInEasing))
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(stringResource(R.string.new_project_type), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MinimalTypeCard("Web", Icons.Default.Html, selectedType == ProjectType.NORMAL, Modifier.weight(1f)) { selectedType = ProjectType.NORMAL }
                    MinimalTypeCard("WebApp", Icons.Default.Android, selectedType == ProjectType.WEBAPP, Modifier.weight(1f)) { selectedType = ProjectType.WEBAPP }
                    MinimalTypeCard("Shell", Icons.Default.Link, selectedType == ProjectType.WEBSITE, Modifier.weight(1f)) { selectedType = ProjectType.WEBSITE }
                }
                Spacer(modifier = Modifier.height(24.dp))

                CleanTextField(
                    value = projectName,
                    onValueChange = {
                        projectName = it
                        if (selectedType != ProjectType.NORMAL && packageName.startsWith("com.example.")) {
                            val clean = it.filter { c -> c.isLetter() }.lowercase(Locale.ROOT)
                            if (clean.isNotEmpty()) {
                                packageName = "com.example.$clean"; syncJsonFromUi()
                            }
                        } else if (selectedType != ProjectType.NORMAL) {
                            syncJsonFromUi()
                        }
                    },
                    placeholder = stringResource(R.string.new_project_name), icon = Icons.Outlined.Edit
                )

                AnimatedVisibility(
                    visible = selectedType == ProjectType.NORMAL,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        SwitchRow(stringResource(R.string.new_project_index_php), createIndexPhp) { createIndexPhp = it }
                    }
                }

                AnimatedVisibility(
                    visible = selectedType == ProjectType.WEBSITE,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        CleanTextField(targetUrl, { targetUrl = it; syncJsonFromUi() }, stringResource(R.string.new_project_url), Icons.Outlined.Link, KeyboardType.Uri)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                AnimatedVisibility(
                    visible = selectedType != ProjectType.NORMAL,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(8.dp)
                    ) {
                        SecondaryTabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary,
                            indicator = {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(selectedTab),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            divider = {},
                            tabs = {
                                Tab(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    text = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Tune, null, Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.new_project_config)) } }
                                )
                                Tab(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    text = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Code, null, Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.new_project_source)) } }
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    (slideInHorizontally { width -> width } + fadeIn()).togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
                                } else {
                                    (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(slideOutHorizontally { width -> width } + fadeOut())
                                }
                            },
                            label = "TabContent"
                        ) { tab ->
                            if (tab == 0) {
                                Column(Modifier.padding(horizontal = 8.dp)) {
                                    ConfigSectionTitle(stringResource(R.string.config_section_app))
                                    CleanTextField(packageName, { packageName = it; syncJsonFromUi() }, stringResource(R.string.config_package), icon = null, isSmall = true, keyboardType = KeyboardType.Ascii)
                                    Spacer(Modifier.height(8.dp))
                                    Row {
                                        CleanTextField(versionName, { versionName = it; syncJsonFromUi() }, stringResource(R.string.config_ver_name), icon = null, isSmall = true, modifier = Modifier.weight(1f))
                                        Spacer(Modifier.width(8.dp))
                                        CleanTextField(versionCode, { versionCode = it; syncJsonFromUi() }, stringResource(R.string.config_ver_code), icon = null, isSmall = true, modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number)
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    FileSelectorRow(stringResource(R.string.config_icon), iconPath, { imageLauncher.launch("image/*") }, icon = Icons.Outlined.Image)
                                    Spacer(Modifier.height(4.dp))
                                    SwitchRow(stringResource(R.string.config_encryption), encryptionEnabled) { encryptionEnabled = it; syncJsonFromUi() }

                                    ConfigSectionTitle(stringResource(R.string.config_section_display))
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                                        Text(stringResource(R.string.config_orientation), style = MaterialTheme.typography.bodyMedium)
                                        Spacer(Modifier.width(16.dp))
                                        Box(Modifier.weight(1f)) {
                                            AnimatedOrientationSelector(currentValue = orientation, onValueChange = { orientation = it; syncJsonFromUi() })
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    SwitchRow(stringResource(R.string.config_fullscreen), isFullscreen) { isFullscreen = it; syncJsonFromUi() }
                                    SwitchRow(stringResource(R.string.config_zoom), zoomEnabled) { zoomEnabled = it; syncJsonFromUi() }

                                    Spacer(Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CleanTextField(statusBarColor, { statusBarColor = it; syncJsonFromUi() }, stringResource(R.string.config_status_color), Icons.Outlined.Palette, isSmall = true, modifier = Modifier.weight(1f))
                                        Spacer(Modifier.width(8.dp))
                                        Box(Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(try { Color(statusBarColor.toColorInt()) } catch (_: Exception) { Color.Transparent }).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)).clickable { showColorPicker = true })
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    SwitchRow(stringResource(R.string.config_status_dark_text), isDarkStatusText) { isDarkStatusText = it; syncJsonFromUi() }

                                    ConfigSectionTitle(stringResource(R.string.config_section_signing))
                                    SwitchRow(stringResource(R.string.config_enable_signing), enableSigning) { enableSigning = it; syncJsonFromUi() }
                                    AnimatedVisibility(visible = enableSigning, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                                        Column {
                                            Spacer(Modifier.height(8.dp))
                                            FileSelectorRow(stringResource(R.string.config_keystore_file), keystorePath, { keystoreLauncher.launch("*/*") })
                                            Spacer(Modifier.height(8.dp))
                                            CleanTextField(keystoreAlias, { keystoreAlias = it; syncJsonFromUi() }, "Alias", Icons.Outlined.Badge, isSmall = true)
                                            Spacer(Modifier.height(8.dp))
                                            CleanTextField(storePassword, { storePassword = it; syncJsonFromUi() }, "Store Pwd", Icons.Outlined.Lock, isSmall = true, isPassword = true)
                                            Spacer(Modifier.height(8.dp))
                                            CleanTextField(keyPassword, { keyPassword = it; syncJsonFromUi() }, "Key Pwd", Icons.Outlined.VpnKey, isSmall = true, isPassword = true)
                                        }
                                    }
                                    Spacer(Modifier.height(16.dp))
                                }
                            } else {
                                Column(Modifier.padding(horizontal = 4.dp)) {
                                    OutlinedTextField(
                                        value = jsonContent,
                                        onValueChange = { syncUiFromJson(it) },
                                        modifier = Modifier.fillMaxWidth().height(500.dp).border(1.dp, if (jsonError == null) MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) else MaterialTheme.colorScheme.error, RoundedCornerShape(8.dp)),
                                        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, lineHeight = 16.sp),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent, focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface)
                                    )
                                    Text(
                                        text = if (jsonError != null) stringResource(R.string.new_project_json_error, jsonError!!) else stringResource(R.string.new_project_json_synced),
                                        color = if (jsonError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun BouncyButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, content: @Composable RowScope.() -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "buttonScale")

    Button(onClick = onClick, modifier = modifier.scale(scale), shape = CircleShape, enabled = enabled, interactionSource = interactionSource, content = content)
}

@Composable
fun AnimatedOrientationSelector(currentValue: String, onValueChange: (String) -> Unit) {
    val options = listOf("Portrait" to "portrait", "Landscape" to "landscape", "Auto" to "auto")
    val selectedIndex = options.indexOfFirst { it.second == currentValue }.coerceAtLeast(0)

    BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))) {
        val segmentWidth = maxWidth / options.size
        val indicatorOffset by animateDpAsState(targetValue = segmentWidth * selectedIndex, animationSpec = tween(300, easing = FastOutSlowInEasing), label = "indicator")

        Box(modifier = Modifier.width(segmentWidth).fillMaxHeight().offset(x = indicatorOffset).padding(4.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.primary))
        Row(modifier = Modifier.fillMaxSize()) {
            options.forEachIndexed { index, (label, value) ->
                Box(modifier = Modifier.weight(1f).fillMaxHeight().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onValueChange(value) }, contentAlignment = Alignment.Center) {
                    val isSelected = index == selectedIndex
                    val textColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, animationSpec = tween(200), label = "text")
                    Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = textColor)
                }
            }
        }
    }
}

@Composable
fun ConfigSectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
}

@Composable
fun SwitchRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().height(40.dp)) {
        Text(title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange, modifier = Modifier.scale(0.8f))
    }
}

@Composable
fun FileSelectorRow(label: String, path: String, onPick: () -> Unit, icon: ImageVector? = Icons.Outlined.Folder) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CleanTextField(path, { }, label, icon, isSmall = true, modifier = Modifier.weight(1f))
        IconButton(onClick = onPick) { Icon(Icons.Default.FolderOpen, null, tint = MaterialTheme.colorScheme.primary) }
    }
}

@Composable
fun MinimalTypeCard(title: String, icon: ImageVector, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val bg by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow, label = "bg")
    val contentColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant, label = "content")
    val scale by animateFloatAsState(if (isSelected) 1.05f else 1f, label = "scale")

    Column(modifier.graphicsLayer { scaleX = scale; scaleY = scale }.clip(RoundedCornerShape(20.dp)).background(bg).clickable { onClick() }.padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = contentColor, modifier = Modifier.size(26.dp))
        Spacer(Modifier.height(6.dp))
        Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = contentColor)
    }
}

@Composable
fun CleanTextField(value: String, onValueChange: (String) -> Unit, placeholder: String, icon: ImageVector?, keyboardType: KeyboardType = KeyboardType.Text, isSmall: Boolean = false, isPassword: Boolean = false, @SuppressLint("ModifierParameter") modifier: Modifier = Modifier) {
    TextField(
        value = value, onValueChange = onValueChange,
        placeholder = { Text(placeholder, style = if (isSmall) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline) },
        leadingIcon = if (icon != null) { { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(if (isSmall) 18.dp else 24.dp)) } } else null,
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)),
        colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
        textStyle = if (isSmall) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge
    )
}

@OptIn(DelicateCoroutinesApi::class)
private fun createNewProject(context: Context, name: String, url: String, type: ProjectType, signing: SigningConfig, iconPath: String, createIndexPhp: Boolean, finalJsonContent: String, onSuccess: (File) -> Unit, onError: (String) -> Unit) {
    val wsPath = WorkspaceManager.getWorkspacePath(context)
    val appPkg = context.packageName

    GlobalScope.launch(Dispatchers.IO) {
        try {
            val parentDir = if (wsPath.contains("/Android/data/$appPkg")) context.getExternalFilesDir(null)!! else File(wsPath)
            val projectDir = File(parentDir, name)
            if (projectDir.exists()) { withContext(Dispatchers.Main) { onError("Project already exists") }; return@launch }
            projectDir.mkdirs()

            fun copyFile(uriString: String, destName: String): String {
                if (uriString.isBlank()) return ""
                return try {
                    val destFile = File(projectDir, destName)
                    val uri = uriString.toUri()
                    if (uriString.startsWith("content://")) {
                        context.contentResolver.openInputStream(uri)?.use { input -> FileOutputStream(destFile).use { input.copyTo(it) } }
                    } else {
                        val src = File(uriString)
                        if (src.exists()) src.copyTo(destFile, overwrite = true)
                    }
                    if (destFile.exists()) destName else ""
                } catch (_: Exception) { "" }
            }

            if (type != ProjectType.NORMAL && signing.path.isNotBlank()) copyFile(signing.path, "keystore" + if (signing.path.endsWith(".jks", true)) ".jks" else ".keystore")
            if (type != ProjectType.NORMAL && iconPath.isNotBlank()) copyFile(iconPath, "icon.png")

            when (type) {
                ProjectType.NORMAL -> createNormalStructure(projectDir, createIndexPhp)
                ProjectType.WEBAPP -> createWebAppStructure(projectDir, finalJsonContent)
                ProjectType.WEBSITE -> createWebsiteStructure(projectDir, url, finalJsonContent)
            }
            withContext(Dispatchers.Main) { onSuccess(projectDir) }
        } catch (e: Exception) { withContext(Dispatchers.Main) { onError(e.message ?: "Unknown error") } }
    }
}

private fun createNormalStructure(dir: File, createIndexPhp: Boolean) {
    File(dir, "css").mkdirs(); File(dir, "js").mkdirs()
    safeWrite(File(dir, "index.html"), ProjectTemplates.normalIndexHtml)
    if (createIndexPhp) safeWrite(File(dir, "index.php"), ProjectTemplates.normalIndexPhp)
    safeWrite(File(dir, "css/style.css"), ProjectTemplates.normalCss)
    safeWrite(File(dir, "js/script.js"), ProjectTemplates.normalJs)
}
private fun createWebAppStructure(dir: File, jsonContent: String) {
    val assets = File(dir, "src/main/assets"); assets.mkdirs()
    File(assets, "js").mkdirs(); File(assets, "css").mkdirs()
    safeWrite(File(assets, "index.html"), ProjectTemplates.webAppIndexHtml)
    safeWrite(File(assets, "js/api.js"), ProjectTemplates.apiJs)
    safeWrite(File(assets, "js/index.js"), ProjectTemplates.webAppIndexJs)
    safeWrite(File(assets, "css/style.css"), ProjectTemplates.webAppCss)
    safeWrite(File(dir, "webapp.json"), jsonContent)
}
private fun createWebsiteStructure(dir: File, url: String, jsonContent: String) {
    val assets = File(dir, "src/main/assets"); assets.mkdirs()
    safeWrite(File(assets, "index.html"), "<!DOCTYPE html><html><body><script>window.location.replace('$url');</script>Redirecting...</body></html>")
    safeWrite(File(dir, "webapp.json"), jsonContent)
}
private fun safeWrite(file: File, content: String) { try { file.parentFile!!.mkdirs(); file.writeText(content) } catch (_: Exception) {} }