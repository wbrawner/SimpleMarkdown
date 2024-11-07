package com.wbrawner.simplemarkdown

import android.app.ComponentCaller
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wbrawner.simplemarkdown.MarkdownApplication.Companion.fileHelper
import com.wbrawner.simplemarkdown.MarkdownApplication.Companion.preferenceHelper
import com.wbrawner.simplemarkdown.ui.MainScreen
import com.wbrawner.simplemarkdown.ui.MarkdownInfoScreen
import com.wbrawner.simplemarkdown.ui.SettingsScreen
import com.wbrawner.simplemarkdown.ui.SupportScreen
import com.wbrawner.simplemarkdown.ui.theme.SimpleMarkdownTheme
import com.wbrawner.simplemarkdown.utility.Preference
import kotlinx.coroutines.launch
import org.acra.ACRA

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {
    private val viewModel: MarkdownViewModel by viewModels {
        MarkdownViewModel.factory(
            fileHelper,
            preferenceHelper
        )
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val autosaveEnabled by preferenceHelper.observe<Boolean>(Preference.AUTOSAVE_ENABLED)
                .collectAsState()
            val darkModePreference by preferenceHelper.observe<String>(Preference.DARK_MODE)
                .collectAsState()
            LaunchedEffect(darkModePreference) {
                val darkMode = when {
                    darkModePreference.equals(
                        getString(R.string.pref_value_light),
                        ignoreCase = true
                    ) -> AppCompatDelegate.MODE_NIGHT_NO

                    darkModePreference.equals(
                        getString(R.string.pref_value_dark),
                        ignoreCase = true
                    ) -> AppCompatDelegate.MODE_NIGHT_YES

                    else -> {
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                        } else {
                            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                        }
                    }
                }
                AppCompatDelegate.setDefaultNightMode(darkMode)
            }
            val errorReporterPreference by preferenceHelper.observe<Boolean>(Preference.ERROR_REPORTS_ENABLED)
                .collectAsState()
            LaunchedEffect(errorReporterPreference) {
                ACRA.errorReporter.setEnabled(errorReporterPreference)
            }
            val intentData = remember(intent) { intent?.data }
            LaunchedEffect(intentData) {
                viewModel.load(intentData?.toString())
            }
            val windowSizeClass = calculateWindowSizeClass(this)
            SimpleMarkdownTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Route.EDITOR.path,
                    enterTransition = {
                        fadeIn(
                            animationSpec = tween(
                                300, easing = LinearEasing
                            )
                        ) + slideIntoContainer(
                            animationSpec = tween(300, easing = EaseIn),
                            towards = AnimatedContentTransitionScope.SlideDirection.Start
                        )
                    },
                    popEnterTransition = { fadeIn() },
                    popExitTransition = {
                        scaleOut(targetScale = 0.9f) + slideOutOfContainer(
                            animationSpec = tween(300, easing = EaseIn),
                            towards = AnimatedContentTransitionScope.SlideDirection.End
                        )
                    }
                ) {
                    composable(Route.EDITOR.path) {
                        MainScreen(
                            navController = navController,
                            viewModel = viewModel,
                            enableWideLayout = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded,
                            enableAutosave = autosaveEnabled,
                        )
                    }
                    composable(Route.SETTINGS.path) {
                        SettingsScreen(navController = navController, preferenceHelper)
                    }
                    composable(Route.SUPPORT.path) {
                        SupportScreen(navController = navController)
                    }
                    composable(Route.HELP.path) {
                        MarkdownInfoScreen(
                            title = stringResource(Route.HELP.title),
                            file = "Cheatsheet.md",
                            navController = navController
                        )
                    }
                    composable(Route.ABOUT.path) {
                        MarkdownInfoScreen(
                            title = stringResource(Route.ABOUT.title),
                            file = "Libraries.md",
                            navController = navController
                        )
                    }
                    composable(Route.PRIVACY.path) {
                        MarkdownInfoScreen(
                            title = stringResource(Route.PRIVACY.title),
                            file = "Privacy Policy.md",
                            navController = navController
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent, caller)
        lifecycleScope.launch {
            intent.data?.let {
                viewModel.load(it.toString())
            }
        }
    }
}

enum class Route(
    val path: String,
    @StringRes
    val title: Int,
    val icon: ImageVector
) {
    EDITOR("/", R.string.title_editor, Icons.Default.Edit),
    SETTINGS("/settings", R.string.title_settings, Icons.Default.Settings),
    SUPPORT("/support", R.string.support_title, Icons.Default.Favorite),
    HELP("/help", R.string.title_help, Icons.AutoMirrored.Filled.Help),
    ABOUT("/about", R.string.title_about, Icons.Default.Info),
    PRIVACY("/privacy", R.string.action_privacy, Icons.Default.PrivacyTip),
}