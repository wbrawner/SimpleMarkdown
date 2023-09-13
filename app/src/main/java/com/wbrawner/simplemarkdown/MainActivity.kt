package com.wbrawner.simplemarkdown

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wbrawner.plausible.android.Plausible
import com.wbrawner.simplemarkdown.MarkdownApplication.Companion.fileHelper
import com.wbrawner.simplemarkdown.MarkdownApplication.Companion.preferenceHelper
import com.wbrawner.simplemarkdown.ui.MainScreen
import com.wbrawner.simplemarkdown.ui.MarkdownInfoScreen
import com.wbrawner.simplemarkdown.ui.SettingsScreen
import com.wbrawner.simplemarkdown.ui.SupportScreen
import com.wbrawner.simplemarkdown.ui.theme.SimpleMarkdownTheme
import com.wbrawner.simplemarkdown.utility.Preference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {
    private val viewModel: MarkdownViewModel by viewModels { MarkdownViewModel.factory(fileHelper, preferenceHelper) }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val darkMode = withContext(Dispatchers.IO) {
                val darkModeValue = preferenceHelper[Preference.DARK_MODE] as String

                return@withContext when {
                    darkModeValue.equals(
                        getString(R.string.pref_value_light),
                        ignoreCase = true
                    ) -> AppCompatDelegate.MODE_NIGHT_NO

                    darkModeValue.equals(
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
            }
            AppCompatDelegate.setDefaultNightMode(darkMode)
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val preferences = mutableMapOf<String, String>()
        preferences["Autosave"] = preferenceHelper[Preference.AUTOSAVE_ENABLED].toString()
        val usingCustomCss = !(preferenceHelper[Preference.CUSTOM_CSS] as String?).isNullOrBlank()
        preferences["Custom CSS"] = usingCustomCss.toString()
        val darkModeSetting = preferenceHelper[Preference.DARK_MODE].toString()
        preferences["Dark Mode"] = darkModeSetting
        preferences["Error Reports"] = preferenceHelper[Preference.ERROR_REPORTS_ENABLED].toString()
        preferences["Readability"] = preferenceHelper[Preference.READABILITY_ENABLED].toString()
        Plausible.event("settings", props = preferences, url = "/")
        setContent {
            val autosaveEnabled by preferenceHelper.observe<Boolean>(Preference.AUTOSAVE_ENABLED)
                .collectAsState()
            val readabilityEnabled by preferenceHelper.observe<Boolean>(Preference.READABILITY_ENABLED)
                .collectAsState()
            SimpleMarkdownTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Route.EDITOR.path,
                    enterTransition = { fadeIn(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideIntoContainer(
                        animationSpec = tween(300, easing = EaseIn),
                        towards = AnimatedContentTransitionScope.SlideDirection.Start
                    ) },
                    popEnterTransition = { EnterTransition.None },
                    popExitTransition = {
                        fadeOut(
                            animationSpec = tween(
                                300, easing = LinearEasing
                            )
                        ) + slideOutOfContainer(
                            animationSpec = tween(300, easing = EaseIn),
                            towards = AnimatedContentTransitionScope.SlideDirection.End
                        )
                    }
                ) {
                    composable(Route.EDITOR.path) {
                        MainScreen(
                            navController = navController,
                            viewModel = viewModel,
                            enableAutosave = autosaveEnabled,
                            enableReadability = readabilityEnabled
                        )
                    }
                    composable(Route.SETTINGS.path) {
                        SettingsScreen(navController = navController, preferenceHelper)
                    }
                    composable(Route.SUPPORT.path) {
                        SupportScreen(navController = navController)
                    }
                    composable(Route.HELP.path) {
                        MarkdownInfoScreen(title = Route.HELP.title, file = "Cheatsheet.md", navController = navController)
                    }
                    composable(Route.ABOUT.path) {
                        MarkdownInfoScreen(title = Route.ABOUT.title, file = "Libraries.md", navController = navController)
                    }
                    composable(Route.PRIVACY.path) {
                        MarkdownInfoScreen(title = Route.PRIVACY.title, file = "Privacy Policy.md", navController = navController)
                    }
                }
            }
        }
    }
}

enum class Route(
    val path: String,
    val title: String,
    val icon: ImageVector
) {
    EDITOR("/", "Editor", Icons.Default.Edit),
    SETTINGS("/settings", "Settings", Icons.Default.Settings),
    SUPPORT("/support", "Support SimpleMarkdown", Icons.Default.Favorite),
    HELP("/help", "Help", Icons.Default.Help),
    ABOUT("/about", "About", Icons.Default.Info),
    PRIVACY("/privacy", "Privacy", Icons.Default.PrivacyTip),
}