package com.wbrawner.simplemarkdown

import android.app.Activity.RESULT_OK
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.core.content.FileProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.NoActivityResumedException
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.wbrawner.simplemarkdown.robot.onMainScreen
import com.wbrawner.simplemarkdown.utility.readAssetToString
import kotlinx.coroutines.test.runTest
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
        onMainScreen(composeRule) {
            typeMarkdown("# Header test")
            checkMarkdownEquals("# Header test")
            openPreview()
        } onPreview {
            verifyH1("Header test")
        }
    }

    @Test
    fun openThenNewMarkdownTest() {
        val markdownText = "# UI Testing\n\nThe quick brown fox jumped over the lazy dog."
        file.outputStream().writer().use { it.write(markdownText) }
        val activityResult = Instrumentation.ActivityResult(RESULT_OK, Intent().apply {
            setDataAndType(Uri.fromFile(file), "text/markdown")
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        })
        intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(activityResult)
        ActivityScenario.launch(MainActivity::class.java)
        onMainScreen(composeRule) {
            openMenu()
            clickOpenMenuItem()
            checkMarkdownEquals(markdownText)
            openMenu()
            clickNewMenuItem()
            verifyDialogIsNotShown()
            checkMarkdownEquals("")
        }
    }

    @Test
    fun editThenNewMarkdownTest() {
        ActivityScenario.launch(MainActivity::class.java)
        val markdownText = "# UI Testing\n\nThe quick brown fox jumped over the lazy dog."
        onMainScreen(composeRule) {
            typeMarkdown(markdownText)
            openMenu()
            clickNewMenuItem()
            verifyDialogIsShown("Would you like to save your changes?")
            discardChanges()
            checkMarkdownEquals("")
        }
    }

    @Test
    fun typingTest() = runTest {
        val markdownText =
            getApplicationContext<Context>().assets.readAssetToString("Cheatsheet.md")
        val additionalText =
            "\n\nThis is some additional text added to an already fairly long file to see if the input lag is resolved"
        ActivityScenario.launch(MainActivity::class.java)
        onMainScreen(composeRule) {
            typeMarkdown(markdownText)
            typeMarkdown(additionalText, replace = false)
            checkMarkdownEquals(markdownText + additionalText)
        }
    }

    @Test
    fun saveMarkdownWithFileUriTest() = runTest {
        ActivityScenario.launch(MainActivity::class.java)
        onMainScreen(composeRule) {
            checkTitleEquals("Untitled.md")
            val markdownText = "# UI Testing\n\nThe quick brown fox jumped over the lazy dog."
            typeMarkdown(markdownText)
            val activityResult = Instrumentation.ActivityResult(RESULT_OK, Intent().apply {
                data = Uri.fromFile(file)
            })
            intending(hasAction(Intent.ACTION_CREATE_DOCUMENT)).respondWith(activityResult)
            openMenu()
            clickSaveMenuItem()
            awaitIdle()
            assertEquals(markdownText, file.inputStream().reader().use(Reader::readText))
            checkTitleEquals("temp.md")
        }
    }

    @Test
    fun saveMarkdownWithContentUriTest() = runTest {
        ActivityScenario.launch(MainActivity::class.java)
        onMainScreen(composeRule) {
            checkTitleEquals("Untitled.md")
            val markdownText = "# UI Testing\n\nThe quick brown fox jumped over the lazy dog."
            typeMarkdown(markdownText)
            val activityResult = Instrumentation.ActivityResult(RESULT_OK, Intent().apply {
                data = FileProvider.getUriForFile(
                    getApplicationContext(),
                    "${BuildConfig.APPLICATION_ID}.fileprovider",
                    file
                )
            })
            intending(hasAction(Intent.ACTION_CREATE_DOCUMENT)).respondWith(activityResult)
            openMenu()
            clickSaveMenuItem()
            awaitIdle()
            assertEquals(markdownText, file.inputStream().reader().use(Reader::readText))
            checkTitleEquals("temp.md")
        }
    }

    @Test
    fun loadMarkdownWithFileUriTest() = runTest {
        ActivityScenario.launch(MainActivity::class.java)
        onMainScreen(composeRule) {
            checkTitleEquals("Untitled.md")
            val markdownText = "# UI Testing\n\nThe quick brown fox jumped over the lazy dog."
            file.outputStream().writer().use { it.write(markdownText) }
            val activityResult = Instrumentation.ActivityResult(RESULT_OK, Intent().apply {
                setDataAndType(Uri.fromFile(file), "text/markdown")
            })
            intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(activityResult)
            openMenu()
            clickOpenMenuItem()
            awaitIdle()
            checkMarkdownEquals(markdownText)
            checkTitleEquals("temp.md")
        }
    }

    @Test
    fun loadMarkdownWithContentUriTest() = runTest {
        ActivityScenario.launch(MainActivity::class.java)
        onMainScreen(composeRule) {
            checkTitleEquals("Untitled.md")
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
            openMenu()
            clickOpenMenuItem()
            awaitIdle()
            checkMarkdownEquals(markdownText)
            checkTitleEquals("temp.md")
        }
    }

    @Test
    fun launchWithContentUriTest() = runTest {
        val markdownText = "# UI Testing\n\nThe quick brown fox jumped over the lazy dog."
        file.outputStream().writer().use { it.write(markdownText) }
        val fileUri = FileProvider.getUriForFile(
            getApplicationContext(),
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            file
        )
        ActivityScenario.launch<MainActivity>(
            Intent(
                Intent.ACTION_VIEW,
                fileUri,
                getInstrumentation().targetContext,
                MainActivity::class.java
            )
        )
        onMainScreen(composeRule) {
            awaitIdle()
            checkMarkdownEquals(markdownText)
            checkTitleEquals("temp.md")
        }
    }

    @Test
    fun openEditAndSaveMarkdownTest() = runTest {
        val markdownText = "# UI Testing\n\nThe quick brown fox jumped over the lazy dog."
        file.outputStream().writer().use { it.write(markdownText) }
        val activityResult = Instrumentation.ActivityResult(RESULT_OK, Intent().apply {
            setDataAndType(Uri.fromFile(file), "text/markdown")
        })
        intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(activityResult)
        ActivityScenario.launch(MainActivity::class.java)
        onMainScreen(composeRule) {
            checkTitleEquals("Untitled.md")
            openMenu()
            clickOpenMenuItem()
            awaitIdle()
            verifyTextIsShown("Successfully loaded temp.md")
            checkMarkdownEquals(markdownText)
            checkTitleEquals("temp.md")
            val additionalText = "# More info\n\nThis is some additional text"
            typeMarkdown(additionalText)
            openMenu()
            clickSaveMenuItem()
            awaitIdle()
            verifyTextIsShown("Successfully saved temp.md")
            assertEquals(additionalText, file.inputStream().reader().use(Reader::readText))
            checkTitleEquals("temp.md")
        }
    }

    @Test
    fun editAndViewHelpMarkdownTest() = runTest {
        ActivityScenario.launch(MainActivity::class.java)
        onMainScreen(composeRule) {
            checkTitleEquals("Untitled.md")
            typeMarkdown("# Header test")
            checkMarkdownEquals("# Header test")
            openDrawer()
        } onNavigationDrawer {
            openHelpPage()
        } onHelpScreen {
            checkTitleEquals("Help")
            verifyH1("Headings/Titles")
            pressBack()
        } onMainScreen {
            checkMarkdownEquals("# Header test")
        }
    }

    @Test(expected = NoActivityResumedException::class)
    fun confirmExitOnBackTest() = runTest {
        ActivityScenario.launch(MainActivity::class.java)
        onMainScreen(composeRule) {
            pressBack()
            checkTitleEquals("Untitled.md")
            verifyTextIsShown("Press back again to exit")
            pressBack()
        }
    }

    @Test
    fun loadNonTextFileAndDecline() = runTest {
        val bytes = Base64.decode("aisNO++GXmaXKErKGqd+cQ==", Base64.DEFAULT)
        file = File(getApplicationContext<Context>().filesDir.absolutePath + "/tmp", "temp.bin")
        file.outputStream().write(bytes)
        val activityResult = Instrumentation.ActivityResult(RESULT_OK, Intent().apply {
            setDataAndType(Uri.fromFile(file), "application/octet-stream")
        })
        intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(activityResult)
        ActivityScenario.launch(MainActivity::class.java)
        onMainScreen(composeRule) {
            checkTitleEquals("Untitled.md")
            openMenu()
            clickOpenMenuItem()
            awaitIdle()
            verifyTextIsShown("temp.bin does not appear to be a text file. Open anyway?")
            clickOnButtonWithText("No")
            verifyTextIsNotShown("temp.bin does not appear to be a text file. Open anyway?")
            checkTitleEquals("Untitled.md")
        }
    }

    @Test
    fun loadNonTextFileAndAccept() = runTest {
        file = File(getApplicationContext<Context>().filesDir.absolutePath + "/tmp", "temp.bin")
        file.outputStream().writer().use { it.write("Actually just text") }
        val activityResult = Instrumentation.ActivityResult(RESULT_OK, Intent().apply {
            setDataAndType(Uri.fromFile(file), "application/octet-stream")
        })
        intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(activityResult)
        ActivityScenario.launch(MainActivity::class.java)
        onMainScreen(composeRule) {
            checkTitleEquals("Untitled.md")
            openMenu()
            clickOpenMenuItem()
            awaitIdle()
            verifyTextIsShown("temp.bin does not appear to be a text file. Open anyway?")
            clickOnButtonWithText("Yes")
            verifyTextIsNotShown("temp.bin does not appear to be a text file. Open anyway?")
            checkTitleEquals("temp.bin")
            checkMarkdownEquals("Actually just text")
        }
    }
}
