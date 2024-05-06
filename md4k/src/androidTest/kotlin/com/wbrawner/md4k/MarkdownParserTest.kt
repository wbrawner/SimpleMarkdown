package com.wbrawner.md4k

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class MarkdownParserTest(private val markdown: String, private val html: String) {

    @Test
    fun testMarkdownToHtmlConversion() {
        val parsedHtml = markdown.toHtml()
        assert(parsedHtml == html) {
            """expected "$html", got "$parsedHtml""""
        }
    }

    companion object {
        @JvmStatic
        @Parameters(name = "Markdown: {0}")
        fun data(): Array<Array<String>> = arrayOf(
                arrayOf("# Test", "<h1>Test</h1>\n"),
                arrayOf("- [ ] Check this", "<ul>\n<li class=\"task-list-item\"><input type=\"checkbox\" class=\"task-list-item-checkbox\" disabled>Check this</li>\n</ul>\n"),
                arrayOf("- [x] Checked!", "<ul>\n<li class=\"task-list-item\"><input type=\"checkbox\" class=\"task-list-item-checkbox\" disabled checked>Checked!</li>\n</ul>\n"),
        )
    }
}