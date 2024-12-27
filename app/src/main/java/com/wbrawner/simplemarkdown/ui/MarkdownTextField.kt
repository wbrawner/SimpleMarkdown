package com.wbrawner.simplemarkdown.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.model.Readability

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkdownTextField(
    modifier: Modifier = Modifier,
    markdown: String,
    setMarkdown: (String?) -> Unit,
    enableReadability: Boolean,
    showPlaceHolder: Boolean,
) {
    val colors = TextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        disabledIndicatorColor = Color.Transparent,
        errorIndicatorColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent
    )
    val interactionSource = remember { MutableInteractionSource() }
    val textStyle = TextStyle.Default.copy(
        fontFamily = FontFamily.Monospace,
        color = MaterialTheme.colorScheme.onSurface
    )
    var selection: TextRange by remember { mutableStateOf(TextRange.Zero) }
    var composition: TextRange? by remember { mutableStateOf(null) }
    val (localMarkdown) = remember(markdown, selection, composition, enableReadability) {
        mutableStateOf(
            TextFieldValue(
                markdown.annotate(enableReadability),
                selection = selection,
                composition = composition
            )
        )
    }
    CompositionLocalProvider(LocalTextSelectionColors provides colors.textSelectionColors) {
        BasicTextField(
            value = localMarkdown,
            modifier = modifier.onKeyEvent {
                if (it.key == Key.Backspace && localMarkdown.text.isEmpty()) {
                    setMarkdown(null)
                    true
                } else {
                    false
                }
            },
            onValueChange = {
                selection = it.selection
                composition = it.composition
                setMarkdown(it.text)
            },
            enabled = true,
            readOnly = false,
            textStyle = textStyle,
            cursorBrush = SolidColor(colors.cursorColor),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            keyboardActions = KeyboardActions.Default,
            interactionSource = interactionSource,
            singleLine = false,
            maxLines = Int.MAX_VALUE,
            minLines = 1,
            decorationBox = @Composable { innerTextField ->
                // places leading icon, text field with label and placeholder, trailing icon
                TextFieldDefaults.DecorationBox(
                    value = localMarkdown.text,
                    visualTransformation = VisualTransformation.None,
                    innerTextField = innerTextField,
                    placeholder = if (showPlaceHolder) {
                        { Text(stringResource(R.string.markdown_here)) }
                    } else {
                        null
                    },
                    singleLine = false,
                    enabled = true,
                    interactionSource = interactionSource,
                    colors = colors,
                    contentPadding = PaddingValues(8.dp)
                )
            },
        )
    }
}

private fun String.annotate(enableReadability: Boolean): AnnotatedString {
    if (!enableReadability) return AnnotatedString(this)
    val readability = Readability(this)
    val annotated = AnnotatedString.Builder(this)
    for (sentence in readability.sentences()) {
        var color = Color.Transparent
        if (sentence.syllableCount() > 25) color = Color(229, 232, 42, 100)
        if (sentence.syllableCount() > 35) color = Color(193, 66, 66, 100)
        annotated.addStyle(SpanStyle(background = color), sentence.start(), sentence.end())
    }
    return annotated.toAnnotatedString()
}
