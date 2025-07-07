package com.wbrawner.simplemarkdown.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        // The application id for the running build variant is read from the instrumentation arguments.
        rule.collect(
            packageName = InstrumentationRegistry.getArguments().getString("targetAppId")
                ?: throw Exception("targetAppId not passed as instrumentation runner arg"),

            // See: https://d.android.com/topic/performance/baselineprofiles/dex-layout-optimizations
            includeInStartupProfile = true
        ) {
            pressHome()
            startActivityAndWait()
            device.wait(Until.hasObject(By.textContains("Untitled.md")), 5_000)
            device.findObject(By.textContains("Markdown here…")).apply {
                click()
//                text = "# Simple Markdown" // This is the obvious choice for inputting text, but it doesn't actually seem to work...
            }
            device.executeShellCommand("""input text # Simple Markdown""")
            device.wait(Until.hasObject(By.textContains("Simple Markdown")), 5_000)
            device.findObject(By.text("Preview")).click()
        }
    }
}