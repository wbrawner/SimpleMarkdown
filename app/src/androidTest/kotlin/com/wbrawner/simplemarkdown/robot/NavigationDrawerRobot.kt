package com.wbrawner.simplemarkdown.robot

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.performClick
import com.wbrawner.simplemarkdown.waitUntilIsDisplayed

class NavigationDrawerRobot(private val composeTestRule: ComposeTestRule) {
    fun openHelpPage() = composeTestRule.onNode(hasClickAction() and hasText("Help"))
        .waitUntilIsDisplayed()
        .performClick()

    infix fun onHelpScreen(block: MarkdownInfoScreenRobot.() -> Unit) =
        MarkdownInfoScreenRobot(composeTestRule).apply(block)
}