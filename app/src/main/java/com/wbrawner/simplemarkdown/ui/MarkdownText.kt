package com.wbrawner.simplemarkdown.ui

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.wbrawner.md4k.MD4K
import com.wbrawner.simplemarkdown.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MarkdownText(modifier: Modifier = Modifier, markdown: String) {
    val (html, setHtml) = remember { mutableStateOf("") }
    LaunchedEffect(markdown) {
        withContext(Dispatchers.IO) {
            setHtml(MD4K.toHtml(markdown))
        }
    }
    HtmlText(modifier = modifier, html = html)
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun HtmlText(html: String, modifier: Modifier = Modifier) {
    val materialColors = MaterialTheme.colorScheme
    val style = remember(isSystemInDarkTheme()) {
        """body {
            |   background: #${materialColors.surface.toArgb().toHexString().substring(2)};
            |   color: #${materialColors.onSurface.toArgb().toHexString().substring(2)};
            |}
            |pre {
            |   background: #${materialColors.surfaceVariant.toArgb().toHexString().substring(2)};
            |   color: #${materialColors.onSurfaceVariant.toArgb().toHexString().substring(2)};
            |}""".trimMargin().wrapTag("style")
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setBackgroundColor(Color.Transparent.toArgb())
                isNestedScrollingEnabled = false
                settings.javaScriptEnabled = true
                loadDataWithBaseURL(null, style + html, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(null, style + html, "text/html", "UTF-8", null)
        }
    )
}

private fun String.wrapTag(tag: String) = "<$tag>$this</$tag>"