package com.wbrawner.simplemarkdown

import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import com.wbrawner.simplemarkdown.robot.onMainScreen
import org.junit.Rule
import org.junit.Test

class HelpTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Test
    fun openHelpPageTest() {
        ActivityScenario.launch(MainActivity::class.java)
        onMainScreen(composeRule) {
            checkTitleEquals("Untitled.md")
            checkMarkdownEquals("")
            openDrawer()
        } onNavigationDrawer {
            openHelpPage()
        } onHelpScreen {
            checkTitleEquals("Help")
            verifyH1("Headings/Titles")
        }
    }
}