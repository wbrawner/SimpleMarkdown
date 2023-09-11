package com.wbrawner.simplemarkdown.ui

import android.content.res.Configuration
import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.preference.PreferenceManager
import com.wbrawner.simplemarkdown.BuildConfig
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.utility.toHtml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MarkdownPreview(modifier: Modifier = Modifier, markdown: String) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var style by remember { mutableStateOf("") }
    LaunchedEffect(context) {
        val isNightMode = AppCompatDelegate.getDefaultNightMode() ==
                AppCompatDelegate.MODE_NIGHT_YES
                || context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        val defaultCssId = if (isNightMode) {
            R.string.pref_custom_css_default_dark
        } else {
            R.string.pref_custom_css_default
        }
        val css = withContext(Dispatchers.IO) {
            @Suppress("ConstantConditionIf")
            if (!BuildConfig.ENABLE_CUSTOM_CSS) {
                context.getString(defaultCssId)
            } else {
                PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(
                        context.getString(R.string.pref_custom_css),
                        context.getString(defaultCssId)
                    )
            }
        }
        style = "<style>$css</style>"
    }
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context)
        },
        update = { preview ->
            coroutineScope.launch {
                preview.loadDataWithBaseURL(null,
                    style + markdown.toHtml(),
                    "text/html",
                    "UTF-8", null
                )
                preview.setBackgroundColor(0x01000000)
            }
        }
    )
}