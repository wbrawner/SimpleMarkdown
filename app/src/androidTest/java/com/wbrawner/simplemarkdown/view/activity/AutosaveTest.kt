package com.wbrawner.simplemarkdown.view.activity


import android.Manifest
import android.view.View
import android.view.ViewGroup
import androidx.test.InstrumentationRegistry.getInstrumentation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import com.wbrawner.simplemarkdown.R
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class AutosaveTest {

    @Rule
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Rule
    var permissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Test
    fun autosaveTest() {
        val dummyFileName = "dummy-autosave.md"
        val realFileName = "test-autosave.md"
        val testText = "This should be automatically saved"

        // Create a dummy file that we'll later use to provoke the autosave
        saveFile(dummyFileName)

        // Then create our actual file that we expect to be automatically saved.
        saveFile(realFileName)

        val appCompatEditText3 = onView(
                allOf(withId(R.id.markdown_edit),
                        childAtPosition(
                                withParent(withId(R.id.pager)),
                                0),
                        isDisplayed()))
        appCompatEditText3.perform(click())

        val appCompatEditText4 = onView(
                allOf(withId(R.id.markdown_edit),
                        childAtPosition(
                                withParent(withId(R.id.pager)),
                                0),
                        isDisplayed()))
        appCompatEditText4.perform(replaceText(testText), closeSoftKeyboard())

        // Jump back to the dummy file. This should provoke the autosave
        openFile(dummyFileName)

        val editText = onView(
                allOf(withId(
                        R.id.markdown_edit),
                        childAtPosition(
                                withParent(withId(R.id.pager)), 0),
                        isDisplayed())
        )

        // Assert that the text is empty
        editText.check(matches(withText("")))

        // Then re-open the actual file
        openFile(realFileName)

        // And assert that we have our expected text (a newline is appended upon reading the file
        // so we'll need to account for that here as well)
        editText.check(matches(withText(testText + "\n")))
    }

    private fun saveFile(fileName: String) {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)

        // TODO: Rewrite this test
//        val appCompatTextView = onView(
//                allOf(withId(R.id.title), withText("Save"),
//                        childAtPosition(
//                                childAtPosition(
//                                        withClassName(`is`("android.support.v7.view.menu.ListMenuItemView")),
//                                        0),
//                                0),
//                        isDisplayed()))
//        appCompatTextView.perform(click())
//
//        val appCompatEditText = onView(
//                allOf(withId(R.id.file_name),
//                        childAtPosition(
//                                childAtPosition(
//                                        withId(android.R.id.content),
//                                        0),
//                                3),
//                        isDisplayed()))
//        appCompatEditText.perform(replaceText(fileName))
//
//        appCompatEditText.perform(closeSoftKeyboard())
//
//        val appCompatButton = onView(
//                allOf(withId(R.id.button_save),
//                        childAtPosition(
//                                childAtPosition(
//                                        withId(android.R.id.content),
//                                        0),
//                                4),
//                        isDisplayed()))
//        appCompatButton.perform(click())
    }

    private fun openFile(fileName: String) {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)

        val openMenuItem = onView(
                allOf(withId(R.id.title), withText("Open"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(`is`("android.support.v7.view.menu.ListMenuItemView")),
                                        0),
                                0),
                        isDisplayed()))
        openMenuItem.perform(click())

        onView(withText(fileName))
                .perform(click())
    }

    private fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return (parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position))
            }
        }
    }
}
