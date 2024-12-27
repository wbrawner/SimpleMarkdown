package com.wbrawner.simplemarkdown

import com.wbrawner.simplemarkdown.ui.replace
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ListReplaceTest(
    private val original: List<String>,
    private val replacementIndex: Int,
    private val replacementText: String?,
    private val expected: List<String>
) {
    @Test
    fun replacementTest() {
        assertEquals(expected, original.replace(replacementIndex, replacementText))
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun args(): Array<Array<Any?>> = arrayOf(
            arrayOf(listOf<String>(), 0, "test", listOf("test")),
            arrayOf(listOf<String>(""), 0, "test", listOf("test")),
            arrayOf(listOf<String>(""), 0, "\n", listOf("", "")),
            arrayOf(listOf<String>("old"), 0, "test", listOf("test")),
            arrayOf(listOf<String>("some", "text"), 0, "test", listOf("test", "text")),
            arrayOf(listOf<String>("some", "text"), 1, "test", listOf("some", "test")),
            arrayOf(
                listOf<String>("some", "more", "text"),
                1,
                "test",
                listOf("some", "test", "text")
            ),
            arrayOf(
                listOf<String>("some", "test", "text"),
                0,
                "even\nmore",
                listOf("even", "more", "test", "text")
            ),
            arrayOf(listOf<String>("some", "test", "text"), 0, null, listOf("some", "text")),
        )
    }
}