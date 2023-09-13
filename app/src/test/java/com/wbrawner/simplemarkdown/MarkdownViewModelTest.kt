package com.wbrawner.simplemarkdown

import com.wbrawner.simplemarkdown.utility.Preference
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.URI

class MarkdownViewModelTest {
    private lateinit var fileHelper: FakeFileHelper
    private lateinit var preferenceHelper: FakePreferenceHelper
    private lateinit var viewModel: MarkdownViewModel

    @Before
    fun setup() {
        fileHelper = FakeFileHelper()
        preferenceHelper = FakePreferenceHelper()
        viewModel = MarkdownViewModel(fileHelper, preferenceHelper)
    }

    @Test
    fun testMarkdownUpdate() = runBlocking {
        assertEquals("", viewModel.markdown.value)
        viewModel.updateMarkdown("Updated content")
        assertEquals("Updated content", viewModel.markdown.value)
    }

    @Test
    fun testLoadWithNoPathAndNoAutosaveUri() = runBlocking {
        viewModel.load(null)
        assertTrue(fileHelper.openedUris.isEmpty())
    }

    @Test
    fun testLoadWithNoPathAndAutosaveUri() = runBlocking {
        val uri = URI.create("file:///home/user/Untitled.md")
        preferenceHelper[Preference.AUTOSAVE_URI] = uri.toString()
        viewModel.load(null)
        assertEquals(uri, fileHelper.openedUris.firstOrNull())
        val (fileName, contents) = fileHelper.file
        assertEquals(fileName, viewModel.fileName.value)
        assertEquals(contents, viewModel.markdown.value)
    }

    @Test
    fun testLoadWithPath() = runBlocking {
        val uri = URI.create("file:///home/user/Untitled.md")
        viewModel.load(uri.toString())
        assertEquals(uri, fileHelper.openedUris.firstOrNull())
        val (fileName, contents) = fileHelper.file
        assertEquals(fileName, viewModel.fileName.value)
        assertEquals(contents, viewModel.markdown.value)
    }
}