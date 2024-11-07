package com.wbrawner.simplemarkdown.robot

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick

class MarkdownInfoScreenRobot(private val composeTestRule: ComposeTestRule) :
    TopAppBarRobot by ComposeTopAppBarRobot(composeTestRule),
    WebViewRobot by EspressoWebViewRobot() {
    fun pressBack() = composeTestRule.onNodeWithContentDescription("Back").performClick()

    infix fun onMainScreen(block: MainScreenRobot.() -> Unit) =
        MainScreenRobot(composeTestRule).apply(block)
}