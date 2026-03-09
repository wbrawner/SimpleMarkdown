package com.wbrawner.simplemarkdown.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.insert
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import com.wbrawner.simplemarkdown.R

@Composable
fun MarkdownToolbar(
    textFieldState: TextFieldState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Bold
        IconButton(onClick = {
            textFieldState.edit {
                if (selection.length > 0) {
                    val text = toString().substring(selection.start, selection.end)
                    replace(selection.start, selection.end, "**$text**")
                } else {
                    insert(selection.start, "****")
                    selection = TextRange(selection.start - 2)
                }
            }
        }) {
            Icon(Icons.Default.FormatBold, contentDescription = stringResource(R.string.action_bold))
        }

        // Italic
        IconButton(onClick = {
            textFieldState.edit {
                if (selection.length > 0) {
                    val text = toString().substring(selection.start, selection.end)
                    replace(selection.start, selection.end, "*$text*")
                } else {
                    insert(selection.start, "**")
                    selection = TextRange(selection.start - 1)
                }
            }
        }) {
            Icon(Icons.Default.FormatItalic, contentDescription = stringResource(R.string.action_italic))
        }

        // Heading
        IconButton(onClick = {
            textFieldState.edit {
                if (selection.length > 0) {
                    val text = toString().substring(selection.start, selection.end)
                    replace(selection.start, selection.end, "# $text")
                } else {
                    insert(selection.start, "# ")
                }
            }
        }) {
            Icon(Icons.Default.Title, contentDescription = stringResource(R.string.action_heading))
        }

        // Bullet list
        IconButton(onClick = {
            textFieldState.edit {
                if (selection.length > 0) {
                    val text = toString().substring(selection.start, selection.end)
                    val newText = text.lines().joinToString("\n") { "* $it" }
                    replace(selection.start, selection.end, newText)
                } else {
                    insert(selection.start, "* ")
                }
            }
        }) {
            Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = stringResource(R.string.action_list_bulleted))
        }

        // Link
        IconButton(onClick = {
            textFieldState.edit {
                val start = selection.start
                if (selection.length > 0) {
                    val text = toString().substring(selection.start, selection.end)
                    replace(selection.start, selection.end, "[$text](http://)")
                    // Move cursor to inside parentheses
                    selection = TextRange(start + text.length + 3)
                } else {
                    insert(selection.start, "[](http://)")
                    // Move cursor to inside brackets
                    selection = TextRange(start + 1)
                }
            }
        }) {
            Icon(Icons.Default.Link, contentDescription = stringResource(R.string.action_link))
        }

        // Code
        IconButton(onClick = {
            textFieldState.edit {
                if (selection.length > 0) {
                    val text = toString().substring(selection.start, selection.end)
                    replace(selection.start, selection.end, "`$text`")
                } else {
                    insert(selection.start, "``")
                    selection = TextRange(selection.start - 1)
                }
            }
        }) {
            Icon(Icons.Default.Code, contentDescription = stringResource(R.string.action_code))
        }

        // Quote
        IconButton(onClick = {
            textFieldState.edit {
                if (selection.length > 0) {
                    val text = toString().substring(selection.start, selection.end)
                    val newText = text.lines().joinToString("\n") { "> $it" }
                    replace(selection.start, selection.end, newText)
                } else {
                    insert(selection.start, "> ")
                }
            }
        }) {
            Icon(Icons.Default.FormatQuote, contentDescription = stringResource(R.string.action_quote))
        }
    }
}
