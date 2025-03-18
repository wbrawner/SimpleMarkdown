package com.wbrawner.simplemarkdown.ui

import android.annotation.SuppressLint
import android.graphics.Color.TRANSPARENT
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.wbrawner.simplemarkdown.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.front.matter.YamlFrontMatterExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension
import org.commonmark.ext.image.attributes.ImageAttributesExtension
import org.commonmark.ext.task.list.items.TaskListItemsExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import timber.log.Timber

private val markdownExtensions = listOf(
    AutolinkExtension.create(),
    StrikethroughExtension.create(),
    TablesExtension.create(),
    HeadingAnchorExtension.create(),
    YamlFrontMatterExtension.create(),
    ImageAttributesExtension.create(),
    TaskListItemsExtension.create(),
)

private val markdownParser = try {
    Parser.builder()
        .extensions(markdownExtensions)
        .build()
} catch (t: Throwable) {
    Timber.e(t, "Failed to initialize markdown parser")
    null
}

private val renderer = try {
    HtmlRenderer.builder()
        .extensions(markdownExtensions)
        .build()
} catch (t: Throwable) {
    Timber.e(t, "Failed to initialize markdown renderer")
    null
}

@Composable
fun MarkdownText(modifier: Modifier = Modifier, markdown: String) {
    if (markdownParser == null || renderer == null) {
        Text(modifier = modifier, text = markdown)
        return
    }

    val (html, setHtml) = remember { mutableStateOf("") }
    LaunchedEffect(markdown) {
        withContext(Dispatchers.IO) {
            val parsedHtml = renderer.render(
                markdownParser.parse(markdown)
            )
            setHtml(parsedHtml)
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top
    ) {
        HtmlText(html = html)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalStdlibApi::class)
@Composable
fun HtmlText(html: String, modifier: Modifier = Modifier) {
    val materialColors = MaterialTheme.colorScheme
    val style = remember(isSystemInDarkTheme()) {
        """body {
            |   background: #${materialColors.surface.toArgb().toHexString().substring(2)};
            |   color: #${materialColors.onSurface.toArgb().toHexString().substring(2)};
            |}
            |a {
            |   color: #${materialColors.secondary.toArgb().toHexString().substring(2)};
            |}
            |pre {
            |   background: #${materialColors.surfaceVariant.toArgb().toHexString().substring(2)};
            |   color: #${materialColors.onSurfaceVariant.toArgb().toHexString().substring(2)};
            |}""".trimMargin().wrapTag("style")
    }
    AndroidView(
        modifier = modifier,
        factory = { context ->
            FrameLayout(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                addView(
                    WebView(context).apply {
                        tag = WEBVIEW_TAG
                        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        setBackgroundColor(TRANSPARENT)
                        isNestedScrollingEnabled = false
                        settings.javaScriptEnabled = true
                        loadDataWithBaseURL(null, style + html, "text/html", "UTF-8", null)
                    }
                )
            }
        },
        update = { frameLayout ->
            frameLayout.findViewWithTag<WebView>(WEBVIEW_TAG)
                .loadDataWithBaseURL(null, style + html, "text/html", "UTF-8", null)
        }
    )
}

private const val WEBVIEW_TAG = "com.wbrawner.simplemarkdown.MarkdownText#WebView"

private fun String.wrapTag(tag: String) = "<$tag>$this</$tag>"