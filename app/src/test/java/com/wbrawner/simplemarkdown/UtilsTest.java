package com.wbrawner.simplemarkdown;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.wbrawner.simplemarkdown.utility.Utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@SuppressWarnings("ResultOfMethodCallIgnored")
public class UtilsTest {
    private Context context;
    private SharedPreferences sharedPreferences;
    private String rootDir;

    @Before
    public void setup() {
        context = RuntimeEnvironment.application.getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().clear().apply();
        rootDir = System.getProperty("java.io.tmpdir") + "/SimpleMarkdown";
        new File(rootDir).mkdir();
    }

    @After
    public void tearDown() {
        rmdir(new File(rootDir));
    }

    @Test
    public void isAutosaveEnabled() throws Exception {
        assertTrue(Utils.isAutosaveEnabled(context));
        sharedPreferences.edit().putBoolean(Utils.KEY_AUTOSAVE, false).apply();
        assertFalse(Utils.isAutosaveEnabled(context));
    }

    private void rmdir(File dir) {
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                file.delete();
            } else {
                rmdir(file);
            }
        }

        dir.delete();
    }
}