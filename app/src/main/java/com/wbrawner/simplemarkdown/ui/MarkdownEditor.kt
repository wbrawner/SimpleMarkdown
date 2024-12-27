package com.wbrawner.simplemarkdown.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.wbrawner.simplemarkdown.ui.theme.SimpleMarkdownTheme

@Composable
fun MarkdownEditor(
    lines: List<String>,
    updateLine: (Int, String?) -> Unit,
    enableReadability: Boolean,
    modifier: Modifier = Modifier,
) {
    var selection by remember { mutableIntStateOf(0) }
    LazyColumn(modifier = modifier.imePadding()) {
        itemsIndexed(items = lines, key = { index: Int, line: String -> index }) { index, line ->
            val focusRequester = remember { FocusRequester() }
            LaunchedEffect(selection) {
                if (selection == index) {
                    focusRequester.requestFocus()
                }
            }
            MarkdownTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                markdown = line,
                setMarkdown = {
                    updateLine(index, it)
                    if (it == null) {
                        selection--
                    } else if (it.endsWith("\n")) {
                        selection++
                    } else {
                        selection = index
                    }
                },
                enableReadability = enableReadability,
                showPlaceHolder = lines.size == 1
            )
        }
    }
}

@Composable
@PreviewLightDark
fun MarkdownEditor_Preview() {
    var input by remember { mutableStateOf(listOf<String>("")) }
    SimpleMarkdownTheme {
        Surface {
            MarkdownEditor(
                modifier = Modifier.fillMaxSize(),
                lines = input,
                updateLine = { index, line ->
                    input.replace(index, line)
                },
                enableReadability = false
            )
        }
    }
}

fun List<String>.replace(index: Int, replacement: String?): List<String> {
    val adjustedReplacement = replacement?.split("\n").orEmpty()
    return when {
        index >= size -> plus(adjustedReplacement)
        index == 0 -> adjustedReplacement + takeLast(size - 1)
        index == lastIndex -> take(size - 1).plus(adjustedReplacement)
        else -> subList(0, index).plus(adjustedReplacement) + subList(index + 1, size)
    }
}
