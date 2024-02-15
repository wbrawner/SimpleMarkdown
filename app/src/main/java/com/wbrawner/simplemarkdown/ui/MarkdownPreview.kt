package com.wbrawner.simplemarkdown.ui

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.wbrawner.simplemarkdown.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.Reader

private const val container = "<main id=\"content\"></main>"

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun MarkdownPreview(modifier: Modifier = Modifier, markdown: String) {
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
    var marked by remember { mutableStateOf("") }
    var markedHighlight by remember { mutableStateOf("") }
    var highlightJs by remember { mutableStateOf("") }
    var highlightCss by remember { mutableStateOf("") }
    var markdownJs by remember { mutableStateOf("") }
    val markdownUpdateJs by remember(markdown) {
        mutableStateOf(
            "setMarkdown(`${
                markdown.replace(
                    "`",
                    "\\`"
                )
            }`)".wrapTag("script")
        )
    }
    val context = LocalContext.current
    LaunchedEffect(context) {
        withContext(Dispatchers.IO) {
            marked = context.assetToString("marked.js").wrapTag("script")
            markedHighlight = context.assetToString("marked-highlight.js").wrapTag("script")
            highlightJs = context.assetToString("highlight.js").wrapTag("script")
            highlightCss = context.assetToString("highlight.css").wrapTag("style")
            markdownJs = context.assetToString("markdown.js").wrapTag("script")
        }
    }
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val content =
                highlightCss + style + container + marked + markedHighlight + highlightJs + markdownJs + markdownUpdateJs

            WebView(context).apply {
                WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setBackgroundColor(Color.Transparent.toArgb())
                isNestedScrollingEnabled = false
                settings.javaScriptEnabled = true
                loadDataWithBaseURL(null, content, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            val content =
                highlightCss + style + container + marked + markedHighlight + highlightJs + markdownJs + markdownUpdateJs
            webView.loadDataWithBaseURL(null, content, "text/html", "UTF-8", null)
        }
    )
}

private fun String.wrapTag(tag: String) = "<$tag>$this</$tag>"

private fun Context.assetToString(fileName: String): String =
    assets.open(fileName).reader().use(Reader::readText)