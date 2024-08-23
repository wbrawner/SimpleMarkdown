package com.wbrawner.simplemarkdown.robot

import android.webkit.WebView
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.Locator
import org.hamcrest.CoreMatchers.containsString

interface WebViewRobot {
    fun verifyH1(text: String)
}

class EspressoWebViewRobot : WebViewRobot {
    private fun findWebView() = onWebView(isAssignableFrom(WebView::class.java))
        .forceJavascriptEnabled()

    override fun verifyH1(text: String) {
        findWebView().withElement(findElement(Locator.TAG_NAME, "h1"))
            .check(webMatches(getText(), containsString(text)))
    }
}