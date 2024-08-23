package com.wbrawner.simplemarkdown.robot

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import com.wbrawner.simplemarkdown.waitUntilIsDisplayed

interface TopAppBarRobot {
    fun checkTitleEquals(title: String): SemanticsNodeInteraction
}

class ComposeTopAppBarRobot(private val composeTestRule: ComposeTestRule) : TopAppBarRobot {
    override fun checkTitleEquals(title: String) =
        composeTestRule.onNode(
            hasAnySibling(
                hasContentDescription("Main Menu") or hasContentDescription(
                    "Back"
                )
            ).and(hasText(title))
        )
            .waitUntilIsDisplayed()
}