package com.scto.mcs

import androidx.compose.animation.*
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

import com.scto.mcs.core.utils.LogConfigRepository
import com.scto.mcs.core.utils.LogConfigState
import com.scto.mcs.core.utils.WorkspaceManager
import com.scto.mcs.ui.ThemeViewModel
import com.scto.mcs.ui.editor.CodeEditScreen
import com.scto.mcs.ui.editor.viewmodel.EditorViewModel
import com.scto.mcs.ui.preview.WebPreviewScreen
import com.scto.mcs.ui.projects.NewProjectScreen
import com.scto.mcs.ui.projects.ProjectConfigScreen
import com.scto.mcs.ui.projects.ProjectListScreen
import com.scto.mcs.ui.projects.WorkspaceSelectionScreen
import com.scto.mcs.ui.settings.AboutScreen
import com.scto.mcs.ui.settings.SettingsScreen
import com.scto.mcs.ui.terminal.TerminalScreen
import com.scto.mcs.ui.welcome.WelcomeScreen
import com.scto.mcs.ui.editor.doc.JsInterfaceDocScreen

import kotlinx.coroutines.launch

@Composable
fun App(
    navController: NavHostController,
    themeViewModel: ThemeViewModel,
    logConfigRepository: LogConfigRepository,
    logConfigState: LogConfigState
) {
    val mainViewModel: EditorViewModel = viewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        mainViewModel.initializePermissions(context)
    }

    val startDestination = if (
        WorkspaceManager.getWorkspacePath(context) != WorkspaceManager.getDefaultPath(context)
    ) {
        "project_list"
    } else {
        "workspace_selection"
    }
    val themeState by themeViewModel.themeState.collectAsState()

    // 优化后的动画配置
    // 使用更自然的缓动曲线（类似iOS的平滑感）
    val predictiveEasing = CubicBezierEasing(0.2f, 0.0f, 0.2f, 1.0f)
    val duration = 350 // 稍微缩短时间，让响应更快

    // 进入动画（向前导航）
    val enterTransition = {
        slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(duration, easing = predictiveEasing)
        ) + fadeIn(
            animationSpec = tween(duration, easing = predictiveEasing)
        )
    }

    // 退出动画（向前导航时）
    val exitTransition = {
        slideOutHorizontally(
            targetOffsetX = { -(it * 0.3f).toInt() },
            animationSpec = tween(duration, easing = predictiveEasing)
        ) + fadeOut(
            targetAlpha = 0.7f,
            animationSpec = tween(duration, easing = predictiveEasing)
        )
    }

    // 返回进入动画（返回时底层页面重新出现）
    val popEnterTransition = {
        slideInHorizontally(
            initialOffsetX = { -(it * 0.3f).toInt() },
            animationSpec = tween(duration, easing = predictiveEasing)
        ) + fadeIn(
            initialAlpha = 0.7f,
            animationSpec = tween(duration, easing = predictiveEasing)
        ) + scaleIn(
            initialScale = 0.95f,
            animationSpec = tween(duration, easing = predictiveEasing)
        )
    }

    // 返回退出动画（返回时当前页面消失）
    val popExitTransition = {
        slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(duration, easing = predictiveEasing)
        ) + fadeOut(
            targetAlpha = 0f,
            animationSpec = tween(duration, easing = predictiveEasing)
        ) + scaleOut(
            targetScale = 1.1f,
            animationSpec = tween(duration, easing = predictiveEasing)
        )
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,

        // 应用优化后的动画
        enterTransition = { enterTransition() },
        exitTransition = { exitTransition() },
        popEnterTransition = { popEnterTransition() },
        popExitTransition = { popExitTransition() }
    ) {
        composable("workspace_selection") {
            WorkspaceSelectionScreen(navController = navController)
        }

        composable("project_list") {
            ProjectListScreen(navController = navController)
        }

        composable(
            route = "code_edit/{folderName}",
            arguments = listOf(navArgument("folderName") { type = NavType.StringType })
        ) { backStackEntry ->
            val folderName = backStackEntry.arguments?.getString("folderName")
            if (folderName != null) {
                CodeEditScreen(folderName, navController, mainViewModel)
            }
        }

        composable(
            route = "preview/{folderName}",
            arguments = listOf(navArgument("folderName") { type = NavType.StringType })
        ) { backStackEntry ->
            val folderName = backStackEntry.arguments?.getString("folderName")
            if (folderName != null) {
                WebPreviewScreen(folderName, navController, mainViewModel)
            }
        }

        composable("new_project") {
            NewProjectScreen(navController = navController)
        }

        composable(
            route = "project_config/{filePath}",
            arguments = listOf(navArgument("filePath") { type = NavType.StringType })
        ) { backStackEntry ->
            val filePath = backStackEntry.arguments?.getString("filePath")
            if (filePath != null) {
                ProjectConfigScreen(navController, filePath, mainViewModel)
            }
        }

        composable("settings") {
            SettingsScreen(
                navController = navController,
                currentThemeState = themeState,
                logConfigState = logConfigState,
                onThemeChange = { modeIndex, themeIndex, customColor, isMonet, isCustom ->
                    themeViewModel.saveThemeConfig(modeIndex, themeIndex, customColor, isMonet, isCustom)
                },
                onLogConfigChange = { enabled, path ->
                    scope.launch { logConfigRepository.saveLogConfig(enabled, path) }
                },
                editorViewModel = mainViewModel
            )
        }
        
        composable("welcome") {
            WelcomeScreen(
                themeViewModel = themeViewModel,
                onWelcomeFinished = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("js_interface_doc") {
            JsInterfaceDocScreen(navController)
        }

        composable("about") {
            AboutScreen(navController = navController)
        }
        composable("terminal") {
            TerminalScreen(navController = navController)
        }
    }
}

fun NavController.safeNavigate(route: String) {
    val current = currentBackStackEntry?.destination?.route
    if (current != route) {
        navigate(route) {
            launchSingleTop = true
        }
    }
}