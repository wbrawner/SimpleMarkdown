package com.wbrawner.simplemarkdown.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer

private val checkboxElements = Regex("(\\s*)- \\[([xX\\s])]\\s")
private val listElements = Regex("\\s*([\\-*])\\s")

@OptIn(ExperimentalFoundationApi::class)
class ListInputTransformation : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        // If we're deleting text, then abort
        if (originalText.length >= length) return
        // If we're just moving the cursor, abort
        if (changes.changeCount == 0) return
        val textRange = changes.getRange(0)
        val changedRange = asCharSequence().slice(textRange.start until textRange.end)
        // If we did anything other than just inserting a newline, abort
        if (changedRange != "\n") return
        val currentLineBuilder = StringBuilder()
        var startIndex = selection.start - 2
        while (startIndex > -1) {
            val char = charAt(startIndex)
            if (char == '\n') {
                break
            }
            currentLineBuilder.append(char)
            startIndex--
        }
        val currentLine = currentLineBuilder.reverse().toString()
        val checkboxMatch = checkboxElements.find(currentLine)
        if (checkboxMatch != null && checkboxMatch.range.first == 0) {
            replace(
                selection.start,
                selection.end,
                "${checkboxMatch.groups[1]?.value.orEmpty()}- [ ] "
            )
            return
        }
        val listMatch = listElements.find(currentLine)
        if (listMatch != null && listMatch.range.first == 0) {
            replace(selection.start, selection.end, listMatch.groups.first()?.value.orEmpty())
            return
        }
    }
}