package com.wbrawner.simplemarkdown.view.activity

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.StringRes
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
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.wbrawner.plausible.android.Plausible
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.ui.MainScreen
import com.wbrawner.simplemarkdown.ui.MarkdownInfoScreen
import com.wbrawner.simplemarkdown.ui.SettingsScreen
import com.wbrawner.simplemarkdown.ui.theme.SimpleMarkdownTheme
import com.wbrawner.simplemarkdown.viewmodel.MarkdownViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val KEY_AUTOSAVE = "autosave"

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {
    private val viewModel: MarkdownViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val darkMode = withContext(Dispatchers.IO) {
                val darkModeValue = getStringPref(
                    R.string.pref_key_dark_mode,
                    getString(R.string.pref_value_auto)
                )

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
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val preferences = mutableMapOf<String, String>()
        preferences["Autosave"] = sharedPreferences.getBoolean("autosave", true).toString()
        val usingCustomCss = !getStringPref(R.string.pref_custom_css, null).isNullOrBlank()
        preferences["Custom CSS"] = usingCustomCss.toString()
        val darkModeSetting = getStringPref(R.string.pref_key_dark_mode, "auto").toString()
        preferences["Dark Mode"] = darkModeSetting
        preferences["Error Reports"] =
            getBooleanPref(R.string.pref_key_error_reports_enabled, true).toString()
        preferences["Readability"] = getBooleanPref(R.string.readability_enabled, false).toString()
        Plausible.event("settings", props = preferences, url = "/")
        setContent {
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
                        MainScreen(navController = navController, viewModel = viewModel)
                    }
                    composable(Route.SETTINGS.path) {
                        SettingsScreen(navController = navController)
                    }
                    composable(Route.SUPPORT.path) {
                        Text("To do")
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

    override fun onBackPressed() {
        if (!findNavController(R.id.content).navigateUp()) {
            super.onBackPressed()
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

fun Context.getBooleanPref(@StringRes key: Int, defaultValue: Boolean) =
    PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
        getString(key),
        defaultValue
    )

fun Context.getStringPref(@StringRes key: Int, defaultValue: String?) =
    PreferenceManager.getDefaultSharedPreferences(this).getString(
        getString(key),
        defaultValue
    )