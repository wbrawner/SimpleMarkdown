package com.wbrawner.simplemarkdown

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.text.TextRange
import com.wbrawner.simplemarkdown.ui.ListInputTransformation
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class ListInputTransformationTest {
    private lateinit var inputTransformation: ListInputTransformation
    private lateinit var textField: TextFieldState


    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Timber.plant(object : Timber.Tree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                println("$tag/$priority: $message")
                t?.printStackTrace()
            }
        })
        inputTransformation = ListInputTransformation()
        textField = TextFieldState()
    }

    @Test
    fun `test enter on empty text`() {
        pressEnter()
        assertEquals("\n", textField.text)
    }

    @Test
    fun `test enter on normal text`() {
        appendText("An item")
        pressEnter()
        assertEquals("An item\n", textField.text)
    }

    @Test
    fun `test new list item`() {
        appendText("- An item")
        pressEnter()
        assertEquals("- An item\n- ", textField.text)
    }

    @Test
    fun `test new indented list item`() {
        appendText("- An item\n  - An indented item")
        pressEnter()
        assertEquals("- An item\n  - An indented item\n  - ", textField.text)
    }

    @Test
    fun `test non list item with hyphen`() {
        appendText("Just a line - not an item")
        pressEnter()
        assertEquals("Just a line - not an item\n", textField.text)
    }

    @Test
    fun `test enter on middle of list item`() {
        appendText("- An item")
        with(inputTransformation) {
            textField.edit {
                selection = TextRange(4)
                transformInput()
            }
        }
        pressEnter()
        assertEquals("- An\n-  item", textField.text)
    }

    @Test
    fun `test invalid checkbox item`() {
        appendText("- [] An item")
        pressEnter()
        assertEquals("- [] An item\n- ", textField.text)
    }

    @Test
    fun `test new checkbox item`() {
        appendText("- [ ] An item")
        pressEnter()
        assertEquals("- [ ] An item\n- [ ] ", textField.text)
    }

    @Test
    fun `test new indented checkbox item`() {
        appendText("- [ ] An item\n  - [ ] An indented item")
        pressEnter()
        assertEquals("- [ ] An item\n  - [ ] An indented item\n  - [ ] ", textField.text)
    }

    @Test
    fun `test non checkbox item with checkbox in middle`() {
        appendText("Just a line - [ ] not an item")
        pressEnter()
        assertEquals("Just a line - [ ] not an item\n", textField.text)
    }

    private fun appendText(text: String) {
        with(inputTransformation) {
            textField.edit {
                replace(selection.start, selection.end, text)
                transformInput()
            }
        }
    }

    private fun pressEnter() = appendText("\n")
}