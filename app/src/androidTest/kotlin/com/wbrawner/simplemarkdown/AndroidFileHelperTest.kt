package com.wbrawner.simplemarkdown

import androidx.test.platform.app.InstrumentationRegistry
import com.wbrawner.simplemarkdown.utility.AndroidFileHelper
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.File

class AndroidFileHelperTest {

    private lateinit var fileHelper: AndroidFileHelper

    @Before
    fun setup() {
        fileHelper = AndroidFileHelper(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Test
    fun untitledFileNameOffsetTest() = runTest {
        assertEquals(0, fileHelper.untitledOffset)
        File(fileHelper.defaultDirectory, "Untitled.md").createNewFile()
        assertEquals(1, fileHelper.untitledOffset)
        File(fileHelper.defaultDirectory, "Untitled (1).md").createNewFile()
        assertEquals(2, fileHelper.untitledOffset)
        File(fileHelper.defaultDirectory, "Untitled (5).md").createNewFile()
        assertEquals(6, fileHelper.untitledOffset)
    }
}