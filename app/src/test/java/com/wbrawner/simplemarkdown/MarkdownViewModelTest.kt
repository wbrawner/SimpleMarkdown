package com.wbrawner.simplemarkdown

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.wbrawner.simplemarkdown.utility.Preference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
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

@OptIn(ExperimentalCoroutinesApi::class)
class MarkdownViewModelTest {
    private lateinit var fileHelper: FakeFileHelper
    private lateinit var preferenceHelper: FakePreferenceHelper
    private lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: MarkdownViewModel
    private lateinit var viewModelScope: TestScope

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Timber.plant(object: Timber.Tree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                println("$tag/$priority: $message")
                t?.printStackTrace()
            }
        })
        val scheduler = StandardTestDispatcher()
        Dispatchers.setMain(scheduler)
        viewModelScope = TestScope(scheduler)
        fileHelper = FakeFileHelper()
        preferenceHelper = FakePreferenceHelper()
        viewModelFactory = MarkdownViewModel.factory(fileHelper, preferenceHelper)
        viewModel = viewModelFactory.create(MarkdownViewModel::class.java, CreationExtras.Empty)
        viewModelScope.advanceUntilIdle()
    }

    @Test
    fun testMarkdownUpdate() = runTest {
        assertEquals("", viewModel.state.value.markdown)
        viewModel.updateMarkdown("Updated content")
        assertEquals("Updated content", viewModel.state.value.markdown)
    }

    @Test
    fun testLoadWithNoPathAndNoAutosaveUri() = runTest {
        viewModel.load(null)
        assertTrue(fileHelper.openedUris.isEmpty())
    }

    @Test
    fun testAutoLoad() = runTest {
        val uri = URI.create("file:///home/user/Untitled.md")
        preferenceHelper[Preference.AUTOSAVE_URI] = uri.toString()
        viewModel = viewModelFactory.create(MarkdownViewModel::class.java, CreationExtras.Empty)
        viewModelScope.advanceUntilIdle()
        assertEquals(uri, fileHelper.openedUris.firstOrNull())
        val (fileName, contents) = fileHelper.file
        assertEquals(fileName, viewModel.state.value.fileName)
        assertEquals(contents, viewModel.state.value.markdown)
    }

    @Test
    fun testLoadWithPath() = runTest {
        val uri = URI.create("file:///home/user/Untitled.md")
        viewModel.load(uri.toString())
        assertEquals(uri, fileHelper.openedUris.firstOrNull())
        val (fileName, contents) = fileHelper.file
        assertEquals(fileName, viewModel.state.value.fileName)
        assertEquals(contents, viewModel.state.value.markdown)
    }

    @Test
    fun testLoadWithEmptyPath() = runTest {
        preferenceHelper[Preference.AUTOSAVE_URI] = ""
        viewModel.load("")
        assertEquals(null, preferenceHelper[Preference.AUTOSAVE_URI])
        assertTrue(fileHelper.openedUris.isEmpty())
    }

    @Test
    fun testLoadWithInvalidUri() = runTest {
        viewModel.load(":/:/")
    }

    @Test
    fun testLoadWithError() = runTest {
        fileHelper.errorOnOpen = true
        val uri = URI.create("file:///home/user/Untitled.md")
        viewModel.load(uri.toString())
        assertNotNull(viewModel.state.value.alert)
    }

    @Test
    fun testSaveWithNullPath() = runTest {
        assertFalse(viewModel.save(null, false))
        assertNull(viewModel.state.value.alert)
        assertNull(viewModel.state.value.saveCallback)
    }

    @Test
    fun testSaveWithNullPathAndPrompt() = runTest {
        assertFalse(viewModel.save(null, true))
        assertNotNull(viewModel.state.value.saveCallback)
        viewModel.state.value.saveCallback!!.invoke()
        assertNull(viewModel.state.value.saveCallback)
    }

    @Test
    fun testSaveWithValidPath() = runTest {
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
    fun testSaveWithException() = runTest {
        val uri = URI.create("file:///home/user/Untitled.md")
        val testMarkdown = "# Test"
        viewModel.updateMarkdown(testMarkdown)
        assertEquals(testMarkdown, viewModel.state.value.markdown)
        fileHelper.errorOnSave = true
        assertNull(viewModel.state.value.alert)
        assertFalse(viewModel.save(uri))
        assertNotNull(viewModel.state.value.alert)
        requireNotNull(viewModel.state.value.alert?.confirmButton?.onClick).invoke()
        assertNull(viewModel.state.value.alert)
    }

    @Test
    fun testResetWithSavedChanges() = runTest {
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
    fun testResetWithUnsavedChanges() = runTest {
        viewModel.updateMarkdown("# Test")
        assertTrue(viewModel.state.value.dirty)
        assertNull(viewModel.state.value.alert)
        viewModel.reset("Untitled.md")
        with(viewModel.state.value.alert) {
            assertNotNull(this)
            requireNotNull(this)
            val onClick = dismissButton?.onClick
            assertNotNull(onClick)
            requireNotNull(onClick)
            onClick.invoke()
        }
        assertEquals(viewModel.state.value, EditorState(reloadToggle = 0.inv()))
    }

    @Test
    fun testResetWithUnsavedChangesAndPrompt() = runTest {
        viewModel.updateMarkdown("# Test")
        assertTrue(viewModel.state.value.dirty)
        assertNull(viewModel.state.value.alert)
        viewModel.reset("Untitled.md")
        assertNull(viewModel.state.value.saveCallback)
        with(viewModel.state.value.alert) {
            assertNotNull(this)
            requireNotNull(this)
            confirmButton.onClick.invoke()
        }
        val uri = URI.create("file:///home/user/Saved.md")
        viewModel.save(uri)
        assertNotNull(viewModel.state.value.saveCallback)
        requireNotNull(viewModel.state.value.saveCallback).invoke()
        assertEquals(viewModel.state.value, EditorState(reloadToggle = 0.inv()))
    }

    @Test
    fun testForceResetWithUnsavedChanges() = runTest {
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
    fun testAutosaveWithPreferenceDisabled() = runTest {
        preferenceHelper[Preference.AUTOSAVE_ENABLED] = false
        viewModel.updateMarkdown("# Test")
        assertTrue(viewModel.state.value.dirty)
        viewModel.autosave()
        assertEquals(0, fileHelper.savedData.count())
    }

    @Test
    fun testAutosaveWithNoNewData() = runTest {
        viewModel.updateMarkdown("# Test")
        val uri = URI.create("file:///home/user/Saved.md")
        assertTrue(viewModel.save(uri))
        assertEquals(1, fileHelper.savedData.count())
        assertFalse(viewModel.state.value.dirty)
        viewModel.autosave()
        assertEquals(1, fileHelper.savedData.count())
    }

    @Test
    fun testAutosaveWithSaveInProgress() = runTest {
        viewModel.updateMarkdown("# Test")
        val uri = URI.create("file:///home/user/Saved.md")
        val coroutineScope = TestScope(StandardTestDispatcher())
        coroutineScope.launch {
            assertTrue(viewModel.save(uri))
        }
        coroutineScope.advanceTimeBy(500)
        assertEquals(0, fileHelper.savedData.count())
        assertTrue(viewModel.state.value.dirty)
        viewModel.autosave()
        assertEquals(0, fileHelper.savedData.count())
        coroutineScope.advanceTimeBy(1000)
        assertEquals(1, fileHelper.savedData.count())
    }

    @Test
    fun testAutosaveWithUnknownUri() = runTest {
        viewModel.updateMarkdown("# Test")
        assertTrue(viewModel.state.value.dirty)
        viewModel.autosave()
        assertTrue(viewModel.state.value.dirty)
        assertEquals(1, fileHelper.savedData.count())
        assertEquals(
            File(fileHelper.defaultDirectory, "Untitled.md").toURI(),
            fileHelper.savedData.first().uri
        )
    }

    @Test
    fun testAutosaveWithKnownUri() = runTest {
        viewModel.updateMarkdown("# Test")
        assertTrue(viewModel.state.value.dirty)
        val uri = URI.create("file:///home/user/Saved.md")
        assertTrue(viewModel.save(uri))
        assertEquals(1, fileHelper.savedData.count())
        assertFalse(viewModel.state.value.dirty)
        viewModel.updateMarkdown("# Test\n\nDirty changes")
        assertTrue(viewModel.state.value.dirty)
        viewModel.autosave()
        assertEquals(2, fileHelper.savedData.count())
    }

    @Test
    fun testSetLockSwiping() = runTest {
        preferenceHelper[Preference.LOCK_SWIPING] = false
        assertFalse(viewModel.state.value.lockSwiping)
        viewModel.setLockSwiping(true)
        viewModelScope.advanceUntilIdle()
        assertTrue(preferenceHelper.preferences[Preference.LOCK_SWIPING] as Boolean)
        assertTrue(viewModel.state.value.lockSwiping)
        viewModel.setLockSwiping(false)
        viewModelScope.advanceUntilIdle()
        assertFalse(preferenceHelper.preferences[Preference.LOCK_SWIPING] as Boolean)
        assertFalse(viewModel.state.value.lockSwiping)
    }
}