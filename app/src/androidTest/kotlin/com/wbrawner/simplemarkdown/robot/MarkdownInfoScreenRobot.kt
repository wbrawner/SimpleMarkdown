package com.wbrawner.simplemarkdown.robot

import androidx.compose.ui.test.junit4.ComposeTestRule

class MarkdownInfoScreenRobot(private val composeTestRule: ComposeTestRule) :
    TopAppBarRobot by ComposeTopAppBarRobot(composeTestRule),
    WebViewRobot by EspressoWebViewRobot()