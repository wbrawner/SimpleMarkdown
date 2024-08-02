package com.wbrawner.simplemarkdown

import android.app.Activity.RESULT_OK
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.printToLog
import androidx.core.content.FileProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.containsString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.Reader

class MarkdownTests {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    @get:Rule
    val intentsRule = IntentsRule()

    private lateinit var file: File

    @Before
    fun setup() {
        file = File(getApplicationContext<Context>().filesDir.absolutePath + "/tmp", "temp.md")
        assertTrue(requireNotNull(file.parentFile).mkdirs())
        file.delete()
    }

    @Test
    @Throws(Exception::class)
    fun openAppTest() {
        val context = getInstrumentation().targetContext
        context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            .apply { context.startActivity(this) }
    }

    @Test
    fun editAndPreviewMarkdownTest() {
        ActivityScenario.launch(MainActivity::class.java)
        composeRule.typeMarkdown("# Header test")
        composeRule.checkMarkdownEquals("# Header test")
        composeRule.openPreview()
        onWebView(isAssignableFrom(WebView::class.java))
            .forceJavascriptEnabled()
            .withElement(findElement(Locator.TAG_NAME, "h1"))
            .check(webMatches(getText(), containsString("Header test")))
    }

    @Test
    fun openThenNewMarkdownTest() {
        val markdownText = "# UI Testing\n\nThe quick brown fox jumped over the lazy dog."
        file.outputStream().writer().use { it.write(markdownText) }
        val activityResult = Instrumentation.ActivityResult(RESULT_OK, Intent().apply {
            data = Uri.fromFile(file)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        })
        intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(activityResult)
        ActivityScenario.launch(MainActivity::class.java)
        composeRule.openMenu()
        composeRule.clickOpenMenuItem()
        composeRule.checkMarkdownEquals(markdownText)
        composeRule.openMenu()
        composeRule.clickNewMenuItem()
        composeRule.verifyDialogIsNotShown()
        composeRule.checkMarkdownEquals("")
    }

    @Test
    fun editThenNewMarkdownTest() {
        ActivityScenario.launch(MainActivity::class.java)
        val markdownText = "# UI Testing\n\nThe quick brown fox jumped over the lazy dog."
        composeRule.typeMarkdown(markdownText)
        composeRule.openMenu()
        composeRule.clickNewMenuItem()
        composeRule.onNode(isDialog()).printToLog("TestDebugging")
        composeRule.verifyDialogIsShown("Would you like to save your changes?")
        composeRule.discardChanges()
        composeRule.checkMarkdownEquals("")
    }

    @Test
    fun saveMarkdownWithFileUriTest() = runTest {
        ActivityScenario.launch(MainActivity::class.java)
        composeRule.checkTitleEquals("Untitled.md")
        val markdownText = "# UI Testing\n\nThe quick brown fox jumped over the lazy dog."
        composeRule.typeMarkdown(markdownText)
        val activityResult = Instrumentation.ActivityResult(RESULT_OK, Intent().apply {
            data = Uri.fromFile(file)
        })
        intending(hasAction(Intent.ACTION_CREATE_DOCUMENT)).respondWith(activityResult)
        composeRule.openMenu()
        composeRule.clickSaveMenuItem()
        composeRule.awaitIdle()
        assertEquals(markdownText, file.inputStream().reader().use(Reader::readText))
        composeRule.checkTitleEquals("temp.md")
    }

    @Test
    fun saveMarkdownWithContentUriTest() = runTest {
        ActivityScenario.launch(MainActivity::class.java)
        composeRule.checkTitleEquals("Untitled.md")
        val markdownText = "# UI Testing\n\nThe quick brown fox jumped over the lazy dog."
        composeRule.typeMarkdown(markdownText)
        val activityResult = Instrumentation.ActivityResult(RESULT_OK, Intent().apply {
            data = FileProvider.getUriForFile(
                getApplicationContext(),
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                file
            )
        })
        intending(hasAction(Intent.ACTION_CREATE_DOCUMENT)).respondWith(activityResult)
        composeRule.openMenu()
        composeRule.clickSaveMenuItem()
        composeRule.awaitIdle()
        assertEquals(markdownText, file.inputStream().reader().use(Reader::readText))
        composeRule.checkTitleEquals("temp.md")
    }

    @Test
    fun loadMarkdownWithFileUriTest() = runTest {
        ActivityScenario.launch(MainActivity::class.java)
        composeRule.checkTitleEquals("Untitled.md")
        val markdownText = "# UI Testing\n\nThe quick brown fox jumped over the lazy dog."
        file.outputStream().writer().use { it.write(markdownText) }
        val activityResult = Instrumentation.ActivityResult(RESULT_OK, Intent().apply {
            data = Uri.fromFile(file)
        })
        intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(activityResult)
        composeRule.openMenu()
        composeRule.clickOpenMenuItem()
        composeRule.awaitIdle()
        composeRule.checkMarkdownEquals(markdownText)
        composeRule.checkTitleEquals("temp.md")
    }

    @Test
    fun loadMarkdownWithContentUriTest() = runTest {
        ActivityScenario.launch(MainActivity::class.java)
        composeRule.checkTitleEquals("Untitled.md")
        val markdownText = "# UI Testing\n\nThe quick brown fox jumped over the lazy dog."
        file.outputStream().writer().use { it.write(markdownText) }
        val activityResult = Instrumentation.ActivityResult(RESULT_OK, Intent().apply {
            data = FileProvider.getUriForFile(
                getApplicationContext(),
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                file
            )
        })
        intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(activityResult)
        composeRule.openMenu()
        composeRule.clickOpenMenuItem()
        composeRule.awaitIdle()
        composeRule.checkMarkdownEquals(markdownText)
        composeRule.checkTitleEquals("temp.md")
    }


    @Test
    fun openEditAndSaveMarkdownTest() = runTest {
        val markdownText = "# UI Testing\n\nThe quick brown fox jumped over the lazy dog."
        file.outputStream().writer().use { it.write(markdownText) }
        val activityResult = Instrumentation.ActivityResult(RESULT_OK, Intent().apply {
            data = Uri.fromFile(file)
        })
        intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(activityResult)
        ActivityScenario.launch(MainActivity::class.java)
        composeRule.checkTitleEquals("Untitled.md")
        composeRule.openMenu()
        composeRule.clickOpenMenuItem()
        composeRule.awaitIdle()
        composeRule.verifyTextIsShown("Successfully loaded temp.md")
        composeRule.checkMarkdownEquals(markdownText)
        composeRule.checkTitleEquals("temp.md")
        val additionalText = "# More info\n\nThis is some additional text"
        composeRule.typeMarkdown(additionalText)
        composeRule.openMenu()
        composeRule.clickSaveMenuItem()
        composeRule.awaitIdle()
        composeRule.verifyTextIsShown("Successfully saved temp.md")
        assertEquals(additionalText, file.inputStream().reader().use(Reader::readText))
        composeRule.checkTitleEquals("temp.md")
    }

    private fun ComposeTestRule.checkTitleEquals(title: String) =
        onNode(hasAnySibling(hasContentDescription("Main Menu")).and(hasText(title)))
            .waitUntilIsDisplayed()

    private fun ComposeTestRule.typeMarkdown(markdown: String) =
        onNode(hasSetTextAction()).performTextReplacement(markdown)


    private fun ComposeTestRule.checkMarkdownEquals(markdown: String) {
        val markdownMatcher = SemanticsMatcher("Markdown = [$markdown]") {
            it.config.getOrNull(SemanticsProperties.EditableText)?.text == markdown
        }
        onNode(hasSetTextAction()).waitUntil {
            assert(markdownMatcher)
        }
    }

    private fun ComposeTestRule.openPreview() = onNodeWithText("Preview").performClick()

    private fun ComposeTestRule.openMenu() =
        onNodeWithContentDescription("Editor Actions").performClick()

    private fun ComposeTestRule.clickOpenMenuItem() = onNodeWithText("Open").performClick()

    private fun ComposeTestRule.clickNewMenuItem() = onNodeWithText("New").performClick()

    private fun ComposeTestRule.clickSaveMenuItem() = onNodeWithText("Save").performClick()

    private fun ComposeTestRule.verifyDialogIsShown(text: String) =
        onNode(isDialog().and(hasAnyDescendant(hasText(text)))).waitUntilIsDisplayed()

    private fun ComposeTestRule.verifyDialogIsNotShown() =
        onNode(isDialog()).waitUntilIsNotDisplayed()

    private fun ComposeTestRule.discardChanges() = onNodeWithText("No").performClick()

    private fun ComposeTestRule.verifyTextIsShown(text: String) =
        onNodeWithText(text).waitUntilIsDisplayed()

    private val ASSERTION_TIMEOUT = 5_000L

    private fun SemanticsNodeInteraction.waitUntil(assertion: SemanticsNodeInteraction.() -> Unit) {
        val start = System.currentTimeMillis()
        lateinit var assertionError: AssertionError
        while (System.currentTimeMillis() - start < ASSERTION_TIMEOUT) {
            try {
                assertion()
                return
            } catch (e: AssertionError) {
                assertionError = e
                Thread.sleep(10)
            }
        }
        throw assertionError
    }

    private fun SemanticsNodeInteraction.waitUntilIsDisplayed() = waitUntil {
        assertIsDisplayed()
    }

    private fun SemanticsNodeInteraction.waitUntilIsNotDisplayed() = waitUntil {
        assertIsNotDisplayed()
    }
}
