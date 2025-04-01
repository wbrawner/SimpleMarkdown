package com.wbrawner.simplemarkdown

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.wbrawner.simplemarkdown.utility.FileHelper
import com.wbrawner.simplemarkdown.utility.Preference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import timber.log.Timber
import java.io.File
import java.net.URI
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class MarkdownViewModelTest {
    private lateinit var fileHelper: FakeFileHelper
    private lateinit var preferenceHelper: FakePreferenceHelper
    private lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: MarkdownViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Timber.plant(object : Timber.Tree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                println("$tag/$priority: $message")
                t?.printStackTrace()
            }
        })
        fileHelper = FakeFileHelper()
        preferenceHelper = FakePreferenceHelper()
    }

    @Test
    fun testMarkdownUpdate() = runTestWithViewModel {
        assertEquals("", viewModel.state.value.markdown)
        viewModel.updateMarkdown("Updated content")
        assertEquals("Updated content", viewModel.state.value.markdown)
    }

    @Test
    fun testLoadWithNoPathAndNoAutosaveUri() = runTestWithViewModel {
        viewModel.load(null)
        assertTrue(fileHelper.openedUris.isEmpty())
    }

    @Test
    fun testAutoLoad() = runTestWithViewModel {
        val uri = URI.create("file:///home/user/Untitled.md")
        preferenceHelper[Preference.AUTOSAVE_URI] = uri.toString()
        viewModel = viewModelFactory.create(MarkdownViewModel::class.java, CreationExtras.Empty)
        viewModel.load(null)
        assertEquals(uri, fileHelper.openedUris.firstOrNull())
        val (fileName, _, contents) = fileHelper.file
        assertEquals(fileName, viewModel.state.value.fileName)
        assertEquals(contents, viewModel.state.value.markdown)
    }

    @Test
    fun testLoadWithPath() = runTestWithViewModel {
        val uri = URI.create("file:///home/user/Untitled.md")
        viewModel.load(uri.toString())
        assertEquals(uri, fileHelper.openedUris.firstOrNull())
        val (fileName, _, contents) = fileHelper.file
        assertEquals(fileName, viewModel.state.value.fileName)
        assertEquals(contents, viewModel.state.value.markdown)
    }

    @Test
    fun testLoadWithEmptyPath() = runTestWithViewModel {
        preferenceHelper[Preference.AUTOSAVE_URI] = ""
        viewModel.load("")
        assertEquals(null, preferenceHelper[Preference.AUTOSAVE_URI])
        assertTrue(fileHelper.openedUris.isEmpty())
    }

    @Test
    fun testLoadWithInvalidUri() = runTestWithViewModel {
        viewModel.load(":/:/")
    }

    @Test
    fun testLoadWithError() = runTestWithViewModel {
        fileHelper.errorOnOpen = true
        val uri = URI.create("file:///home/user/Untitled.md")
        viewModel.load(uri.toString())
        assertNotNull(viewModel.state.value.alert)
    }

    @Test
    fun testLoadNonTextFile() = runTestWithViewModel {
        val uri = URI.create("file:///home/user/Untitled.bin")
        fileHelper.file = FileHelper.FileData(
            name = "Untitled.bin",
            type = "application/octet-stream",
            content = "aisNO++GXmaXKErKGqd+cQ=="
        )
        assertEquals("", viewModel.state.value.markdown)
        viewModel.load(uri.toString())
        val alert = viewModel.state.value.alert
        assertNotNull(alert)
        requireNotNull(alert)
        val secondaryButton = alert.secondaryButton
        assertNotNull(secondaryButton)
        requireNotNull(secondaryButton)
        secondaryButton.onClick.invoke()
        assertNull(viewModel.state.value.alert)
        assertEquals("", viewModel.state.value.markdown)
    }

    @Test
    fun testLoadTextFileWithUnrecognizedType() = runTestWithViewModel {
        val uri = URI.create("file:///home/user/Untitled.bin")
        fileHelper.file = FileHelper.FileData(
            name = "Untitled.bin",
            type = "application/octet-stream",
            content = "This is actually a markdown file that the serving app didn't recognize"
        )
        assertEquals("", viewModel.state.value.markdown)
        viewModel.load(uri.toString())
        val alert = viewModel.state.value.alert
        assertNotNull(alert)
        requireNotNull(alert)
        alert.primaryButton.onClick.invoke()
        assertNull(viewModel.state.value.alert)
        assertEquals(
            "This is actually a markdown file that the serving app didn't recognize",
            viewModel.state.value.markdown
        )
    }

    @Test
    fun testSaveWithNullPath() = runTestWithViewModel {
        assertFalse(viewModel.save(null, false))
        assertNull(viewModel.state.value.alert)
        assertNull(viewModel.state.value.saveCallback)
    }

    @Test
    fun testSaveWithNullPathAndPrompt() = runTestWithViewModel {
        assertFalse(viewModel.save(null, true))
        assertNotNull(viewModel.state.value.saveCallback)
        viewModel.state.value.saveCallback!!.invoke()
        assertNull(viewModel.state.value.saveCallback)
    }

    @Test
    fun testSaveWithValidPath() = runTestWithViewModel {
        val uri = URI.create("file:///home/user/Saved.md")
        val testMarkdown = "# Test"
        viewModel.updateMarkdown(testMarkdown)
        assertEquals(testMarkdown, viewModel.state.value.markdown)
        assertTrue(viewModel.save(uri))
        assertEquals("Saved.md", viewModel.state.value.fileName)
        assertEquals(uri, fileHelper.savedData.last().uri)
        assertEquals(testMarkdown, fileHelper.savedData.last().content)
        assertEquals(uri, preferenceHelper[Preference.AUTOSAVE_URI])
    }

    @Test
    fun testSaveWithException() = runTestWithViewModel {
        val uri = URI.create("file:///home/user/Untitled.md")
        val testMarkdown = "# Test"
        viewModel.updateMarkdown(testMarkdown)
        assertEquals(testMarkdown, viewModel.state.value.markdown)
        fileHelper.errorOnSave = true
        assertNull(viewModel.state.value.alert)
        assertFalse(viewModel.save(uri))
        assertNotNull(viewModel.state.value.alert)
        requireNotNull(viewModel.state.value.alert?.primaryButton?.onClick).invoke()
        assertNull(viewModel.state.value.alert)
    }

    @Test
    fun testResetWithSavedChanges() = runTestWithViewModel {
        viewModel.updateMarkdown("# Test")
        val uri = URI.create("file:///home/user/Saved.md")
        assertTrue(viewModel.save(uri))
        assertFalse(viewModel.state.value.dirty)
        assertNull(viewModel.state.value.alert)
        viewModel.reset("New.md")
        assertNull(viewModel.state.value.alert)
        with(viewModel.state.value) {
            assertEquals("New.md", fileName)
            assertEquals("", markdown)
            assertNull(path)
            assertNull(saveCallback)
            assertNull(alert)
            assertFalse(dirty)
        }
    }

    @Test
    fun testResetWithUnsavedChanges() = runTestWithViewModel {
        viewModel.updateMarkdown("# Test")
        assertTrue(viewModel.state.value.dirty)
        assertNull(viewModel.state.value.alert)
        viewModel.reset("Untitled.md")
        with(viewModel.state.value.alert) {
            assertNotNull(this)
            requireNotNull(this)
            val onClick = secondaryButton?.onClick
            assertNotNull(onClick)
            requireNotNull(onClick).invoke()
        }
        assertEquals(viewModel.state.value.toString(), EditorState().toString())
    }

    @Test
    fun testResetWithUnsavedChangesAndPrompt() = runTestWithViewModel {
        viewModel.updateMarkdown("# Test")
        assertTrue(viewModel.state.value.dirty)
        assertNull(viewModel.state.value.alert)
        viewModel.reset("Untitled.md")
        assertNull(viewModel.state.value.saveCallback)
        with(viewModel.state.value.alert) {
            assertNotNull(this)
            requireNotNull(this)
            primaryButton.onClick.invoke()
        }
        val uri = URI.create("file:///home/user/Saved.md")
        viewModel.save(uri)
        assertNotNull(viewModel.state.value.saveCallback)
        requireNotNull(viewModel.state.value.saveCallback).invoke()
        assertEquals(viewModel.state.value.toString(), EditorState().toString())
    }

    @Test
    fun testForceResetWithUnsavedChanges() = runTestWithViewModel {
        viewModel.updateMarkdown("# Test")
        assertTrue(viewModel.state.value.dirty)
        val uri = URI.create("file:///home/user/Saved.md")
        assertTrue(viewModel.save(uri))
        assertFalse(viewModel.state.value.dirty)
        viewModel.updateMarkdown("# Test\n\nDirty changes")
        assertTrue(viewModel.state.value.dirty)
        assertNull(viewModel.state.value.alert)
        viewModel.reset("Unsaved.md", true)
        assertNull(viewModel.state.value.alert)
        with(viewModel.state.value) {
            assertEquals("Unsaved.md", fileName)
            assertEquals("", markdown)
            assertNull(path)
            assertNull(saveCallback)
            assertNull(alert)
            assertFalse(dirty)
        }
        assertNull(preferenceHelper[Preference.AUTOSAVE_URI])
    }

    @Test
    fun testAutosaveWithPreferenceDisabled() = runTestWithViewModel {
        preferenceHelper[Preference.AUTOSAVE_ENABLED] = false
        viewModel.updateMarkdown("# Test")
        assertTrue(viewModel.state.value.dirty)
        assertEquals(0, fileHelper.savedData.count())
    }

    @Test
    fun testAutosaveWithNoNewData() = runTestWithViewModel {
        viewModel.updateMarkdown("# Test")
        val uri = URI.create("file:///home/user/Saved.md")
        assertTrue(viewModel.save(uri))
        assertEquals(1, fileHelper.savedData.count())
        assertFalse(viewModel.state.value.dirty)
        assertEquals(1, fileHelper.savedData.count())
    }

    @Test
    fun testAutosaveWithSaveInProgress() = runTestWithViewModel {
        viewModel.updateMarkdown("# Test")
        val uri = URI.create("file:///home/user/Saved.md")
        assertEquals(0, fileHelper.savedData.count())
        assertTrue(viewModel.state.value.dirty)
        assertTrue(viewModel.save(uri))
        assertEquals(1, fileHelper.savedData.count())
        advanceTimeBy(2_000)
        assertEquals(1, fileHelper.savedData.count())
    }

    @Test
    fun testAutosaveWithUnknownUri() = runTestWithViewModel {
        viewModel.updateMarkdown("# Test")
        assertTrue(viewModel.state.value.dirty)
        advanceTimeBy(2_000)
        assertEquals(1, fileHelper.savedData.count())
        assertEquals(
            File(fileHelper.defaultDirectory, "Untitled.md").toURI(),
            fileHelper.savedData.first().uri
        )
    }

    @Test
    fun testAutosaveWithKnownUri() = runTestWithViewModel {
        viewModel.updateMarkdown("# Test")
        assertTrue(viewModel.state.value.dirty)
        val uri = URI.create("file:///home/user/Saved.md")
        assertTrue(viewModel.save(uri))
        assertEquals(1, fileHelper.savedData.count())
        assertFalse(viewModel.state.value.dirty)
        viewModel.updateMarkdown("# Test\n\nDirty changes")
        assertTrue(viewModel.state.value.dirty)
        advanceTimeBy(2_000)
        assertEquals(2, fileHelper.savedData.count())
    }

    @Test
    fun testSetLockSwiping() = runTestWithViewModel {
        preferenceHelper[Preference.LOCK_SWIPING] = false
        assertFalse(viewModel.state.value.lockSwiping)
        viewModel.setLockSwiping(true)
        advanceUntilIdle()
        assertTrue(preferenceHelper.preferences[Preference.LOCK_SWIPING] as Boolean)
        assertTrue(viewModel.state.value.lockSwiping)
        viewModel.setLockSwiping(false)
        advanceUntilIdle()
        assertFalse(preferenceHelper.preferences[Preference.LOCK_SWIPING] as Boolean)
        assertFalse(viewModel.state.value.lockSwiping)
    }

    @Test
    fun testBackPress() = runTestWithViewModel {
        assertFalse(viewModel.state.value.exitApp)
        assertNull(viewModel.state.value.toast)
        viewModel.onBackPressed()
        assertFalse(viewModel.state.value.exitApp)
        assertEquals(ParameterizedText.ConfirmExitOnBack, viewModel.state.value.toast)
        viewModel.dismissToast()
        assertFalse(viewModel.state.value.exitApp)
        assertNull(viewModel.state.value.toast)
        viewModel.onBackPressed()
        assertFalse(viewModel.state.value.exitApp)
        assertEquals(ParameterizedText.ConfirmExitOnBack, viewModel.state.value.toast)
        viewModel.onBackPressed()
        assertTrue(viewModel.state.value.exitApp)
    }

    @Test
    fun testDismissShare() = runTestWithViewModel {
        assertNull(viewModel.state.value.alert)
        assertNull(viewModel.state.value.shareText)
        viewModel.share()
        assertNotNull(viewModel.state.value.alert)
        viewModel.dismissShare()
        assertNull(viewModel.state.value.alert)
        assertNull(viewModel.state.value.shareText)
    }

    @Test
    fun testShareMarkdown() = runTestWithViewModel {
        assertNull(viewModel.state.value.alert)
        assertNull(viewModel.state.value.shareText)
        viewModel.updateMarkdown("# Test")
        viewModel.share()
        val alertDialog = viewModel.state.value.alert
        assertNotNull(alertDialog)
        requireNotNull(alertDialog)
        assertEquals(R.string.title_share_as, alertDialog.text.text)
        assertEquals(R.string.share_markdown, alertDialog.primaryButton.text.text)
        alertDialog.primaryButton.onClick()
        val shareText = viewModel.state.value.shareText
        assertNotNull(shareText)
        requireNotNull(shareText)
        assertEquals("# Test", shareText.text)
        assertEquals("text/plain", shareText.contentType)
    }

    @Test
    fun testShareHtml() = runTestWithViewModel {
        assertNull(viewModel.state.value.alert)
        assertNull(viewModel.state.value.shareText)
        viewModel.updateMarkdown("# Test")
        viewModel.share()
        val alertDialog = viewModel.state.value.alert
        assertNotNull(alertDialog)
        requireNotNull(alertDialog)
        assertEquals(R.string.title_share_as, alertDialog.text.text)
        val htmlButton = alertDialog.secondaryButton
        assertNotNull(htmlButton)
        requireNotNull(htmlButton)
        assertEquals(R.string.share_html, htmlButton.text.text)
        htmlButton.onClick()
        val shareText = viewModel.state.value.shareText
        assertNotNull(shareText)
        requireNotNull(shareText)
        assertEquals("<h1 id=\"test\">Test</h1>\n", shareText.text)
        assertEquals("text/plain", shareText.contentType)
    }

    fun runTestWithViewModel(
        context: CoroutineContext = EmptyCoroutineContext,
        timeout: Duration = 60.seconds,
        testBody: suspend TestScope.() -> Unit
    ): TestResult = runTest(context, timeout) {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        viewModelFactory = MarkdownViewModel.factory(
            fileHelper,
            preferenceHelper,
            testDispatcher
        )
        viewModel = viewModelFactory.create(MarkdownViewModel::class.java, CreationExtras.Empty)
        testBody()
    }

    fun MarkdownViewModel.updateMarkdown(markdown: String?) {
        state.value.textFieldState.setTextAndPlaceCursorAtEnd(markdown.orEmpty())
        markdownUpdated()
    }
}
