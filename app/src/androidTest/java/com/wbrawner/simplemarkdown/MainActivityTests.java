package com.wbrawner.simplemarkdown;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;

import com.wbrawner.simplemarkdown.view.activity.MainActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTests {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setup() {
        mActivityRule.getActivity()
                .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Test
    public void openAppTest() throws Exception {
        UiDevice mDevice = UiDevice.getInstance(getInstrumentation());
        mDevice.pressHome();
        // Bring up the default launcher by searching for a UI component
        // that matches the content description for the launcher button.
        UiObject allAppsButton = mDevice
                .findObject(new UiSelector().description("Apps"));

        // Perform a click on the button to load the launcher.
        allAppsButton.clickAndWaitForNewWindow();
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.wbrawner.simplemarkdown", appContext.getPackageName());
        UiScrollable appView = new UiScrollable(new UiSelector().scrollable(true));
        UiSelector simpleMarkdownSelector = new UiSelector().text("Simple Markdown");
        appView.scrollIntoView(simpleMarkdownSelector);
        mDevice.findObject(simpleMarkdownSelector).clickAndWaitForNewWindow();
    }

    @Test
    public void openFileWithoutFilesTest() {
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText("Open")).perform(click());
    }
}
