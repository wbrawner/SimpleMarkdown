package com.wbrawner.simplemarkdown.view.activity;

import android.Manifest;
import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.widget.TextView;

import com.wbrawner.simplemarkdown.R;
import com.wbrawner.simplemarkdown.utility.Constants;

import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class ExplorerActivityTest {
    @Rule
    public ActivityTestRule<ExplorerActivity> explorerActivityActivityTestRule
            = new ActivityTestRule<>(ExplorerActivity.class, false, false);
    @Rule
    public GrantPermissionRule readFilesPermissionRule =
            GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE);
    @Rule
    public GrantPermissionRule writeFilesPermissionRule =
            GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Test
    public void firstItemInDirectoryIsGoUpTest() {
        Intent startIntent = new Intent();
        startIntent.putExtra(Constants.EXTRA_REQUEST_CODE, Constants.REQUEST_OPEN_FILE);
        explorerActivityActivityTestRule.launchActivity(startIntent);
        // We should start in the Documents directory
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText("Documents")));

        // Check that the first item in the list is our navigate up a directory item and click it
        onData(is(instanceOf(HashMap.class)))
                .atPosition(0)
                .check(matches(withText(
                        explorerActivityActivityTestRule.getActivity().getString(R.string.directory_up)
                )))
                .perform(click());

        // Check that we're in the parent of the Documents directory
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText("0")));

        // Enter the Android/data/data directory, which should almost certainly have other
        // directories, and check that each directory shows the "Go up" option as the first option
        // in the list
        onView(withText("Android"))
                .perform(click());
        onData(is(instanceOf(HashMap.class)))
                .atPosition(0)
                .check(matches(withText(
                        explorerActivityActivityTestRule.getActivity().getString(R.string.directory_up)
                )));

        onView(withText("data"))
                .perform(click());
        onData(is(instanceOf(HashMap.class)))
                .atPosition(0)
                .check(matches(withText(
                        explorerActivityActivityTestRule.getActivity().getString(R.string.directory_up)
                )));
    }
}