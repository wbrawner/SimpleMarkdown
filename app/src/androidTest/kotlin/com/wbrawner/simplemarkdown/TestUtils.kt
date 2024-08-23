package com.wbrawner.simplemarkdown

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed

private const val ASSERTION_TIMEOUT = 5_000L

fun SemanticsNodeInteraction.waitUntilIsDisplayed() = waitUntil {
    assertIsDisplayed()
}

fun SemanticsNodeInteraction.waitUntilIsNotDisplayed() = waitUntil {
    assertIsNotDisplayed()
}

fun <T> SemanticsNodeInteraction.waitUntil(assertion: SemanticsNodeInteraction.() -> T): T {
    val start = System.currentTimeMillis()
    lateinit var assertionError: AssertionError
    while (System.currentTimeMillis() - start < ASSERTION_TIMEOUT) {
        try {
            return assertion()
        } catch (e: AssertionError) {
            assertionError = e
            Thread.sleep(10)
        }
    }
    throw assertionError
}
