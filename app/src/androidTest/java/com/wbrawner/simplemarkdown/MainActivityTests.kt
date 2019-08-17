package com.wbrawner.simplemarkdown

import android.content.pm.ActivityInfo
import androidx.test.InstrumentationRegistry
import androidx.test.InstrumentationRegistry.getInstrumentation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import com.wbrawner.simplemarkdown.view.activity.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTests {

    @Rule
    var mActivityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setup() {
        mActivityRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    @Test
    @Throws(Exception::class)
    fun openAppTest() {
        val mDevice = UiDevice.getInstance(getInstrumentation())
        mDevice.pressHome()
        // Bring up the default launcher by searching for a UI component
        // that matches the content description for the launcher button.
        val allAppsButton = mDevice
                .findObject(UiSelector().description("Apps"))

        // Perform a click on the button to load the launcher.
        allAppsButton.clickAndWaitForNewWindow()
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()

        assertEquals("com.wbrawner.simplemarkdown", appContext.packageName)
        val appView = UiScrollable(UiSelector().scrollable(true))
        val simpleMarkdownSelector = UiSelector().text("Simple Markdown")
        appView.scrollIntoView(simpleMarkdownSelector)
        mDevice.findObject(simpleMarkdownSelector).clickAndWaitForNewWindow()
    }

    @Test
    fun openFileWithoutFilesTest() {
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext())
        onView(withText("Open")).perform(click())
    }
}
