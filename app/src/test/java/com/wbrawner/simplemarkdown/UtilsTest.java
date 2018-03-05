package com.wbrawner.simplemarkdown;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.wbrawner.simplemarkdown.utility.Constants;
import com.wbrawner.simplemarkdown.utility.Utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
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
    public void getDocsPath() throws Exception {
        sharedPreferences.edit().putString(Constants.KEY_DOCS_PATH, rootDir).apply();
        assertEquals(rootDir, Utils.getDocsPath(context));
    }

    @Test
    public void getDefaultFileName() throws Exception {
        sharedPreferences.edit().putString(Constants.KEY_DOCS_PATH, rootDir).apply();

        new File(rootDir, "dummy.md").createNewFile();
        new File(rootDir, "dummy1.md").createNewFile();
        new File(rootDir, "Untitled-a.md").createNewFile();

        String firstDefaultName = Utils.getDefaultFileName(context);
        assertEquals("Untitled.md", firstDefaultName);
        File firstFile = new File(rootDir, firstDefaultName);
        firstFile.createNewFile();

        String secondDefaultName = Utils.getDefaultFileName(context);
        assertEquals("Untitled-1.md", secondDefaultName);
        File secondFile = new File(rootDir, secondDefaultName);
        secondFile.createNewFile();

        String thirdDefaultName = Utils.getDefaultFileName(context);
        assertEquals("Untitled-2.md", thirdDefaultName);
    }

    @Test
    public void getDefaultFileNameDoubleDigitTest() throws IOException {
        sharedPreferences.edit().putString(Constants.KEY_DOCS_PATH, rootDir).apply();

        for (int i = 0; i < 11; i++) {
            new File(rootDir, "Untitled-" + i + ".md").createNewFile();
        }
        assertTrue(new File(rootDir, "Untitled-10.md").exists());
        String defaultName = Utils.getDefaultFileName(context);
        assertEquals("Untitled-11.md", defaultName);
    }

    @Test
    public void isAutosaveEnabled() throws Exception {
        assertTrue(Utils.isAutosaveEnabled(context));
        sharedPreferences.edit().putBoolean(Constants.KEY_AUTOSAVE, false).apply();
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