package com.wbrawner.simplemarkdown.robot

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import com.wbrawner.simplemarkdown.waitUntil
import com.wbrawner.simplemarkdown.waitUntilIsDisplayed
import com.wbrawner.simplemarkdown.waitUntilIsNotDisplayed
import kotlinx.coroutines.CoroutineScope

fun onMainScreen(composeRule: ComposeTestRule, block: MainScreenRobot.() -> Unit) =
    MainScreenRobot(composeRule).apply(block)

@Suppress("UnusedReceiverParameter") // Used to avoid import ambiguity for tests
suspend fun CoroutineScope.onMainScreen(
    composeRule: ComposeTestRule,
    block: suspend MainScreenRobot.() -> Unit
): MainScreenRobot {
    val mainScreenRobot = MainScreenRobot(composeRule)
    block.invoke(mainScreenRobot)
    return mainScreenRobot
}

class MainScreenRobot(private val composeRule: ComposeTestRule) :
    TopAppBarRobot by ComposeTopAppBarRobot(composeRule) {
    fun typeMarkdown(markdown: String, replace: Boolean = true) =
        composeRule.onNode(hasSetTextAction())
            .apply {
                if (replace) {
                    performTextReplacement(markdown)
                } else {
                    markdown.forEach {
                        performTextInput(it.toString())
                    }
                }
            }

    fun checkMarkdownEquals(markdown: String) {
        val markdownMatcher = SemanticsMatcher("Markdown = [$markdown]") {
            it.config.getOrNull(SemanticsProperties.EditableText)?.text == markdown
        }
        composeRule.onNode(hasSetTextAction()).waitUntil {
            assert(markdownMatcher)
        }
    }

    fun openPreview() = composeRule.onNodeWithText("Preview").performClick()

    fun openMenu() = composeRule.onNodeWithContentDescription("Editor Actions").performClick()

    fun clickOpenMenuItem() = composeRule.onNodeWithText("Open").performClick()

    fun clickNewMenuItem() = composeRule.onNodeWithText("New").performClick()

    fun clickSaveMenuItem() = composeRule.onNodeWithText("Save").performClick()

    fun verifyDialogIsShown(text: String) =
        composeRule.onNode(isDialog().and(hasAnyDescendant(hasText(text)))).waitUntilIsDisplayed()

    fun verifyDialogIsNotShown() = composeRule.onNode(isDialog()).waitUntilIsNotDisplayed()

    fun discardChanges() = composeRule.onNodeWithText("No").performClick()

    fun verifyTextIsShown(text: String) = composeRule.onNodeWithText(text).waitUntilIsDisplayed()

    fun openDrawer() = composeRule.onNode(hasClickAction() and hasContentDescription("Main Menu"))
        .waitUntilIsDisplayed()
        .performClick()

    suspend fun awaitIdle() = composeRule.awaitIdle()

    infix fun onPreview(block: WebViewRobot.() -> Unit) = EspressoWebViewRobot().apply(block)

    infix fun onNavigationDrawer(block: NavigationDrawerRobot.() -> Unit): NavigationDrawerRobot =
        NavigationDrawerRobot(composeRule).apply(block = block)
}