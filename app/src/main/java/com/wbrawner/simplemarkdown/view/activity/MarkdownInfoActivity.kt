package com.wbrawner.simplemarkdown.view.activity

import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.wbrawner.simplemarkdown.R
import kotlinx.android.synthetic.main.activity_markdown_info.*

class MarkdownInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_markdown_info)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val intent = intent
        if (intent == null || !intent.hasExtra("title") || !intent.hasExtra("html")) {
            finish()
            return
        }
        title = intent.getStringExtra("title")
        val isNightMode = AppCompatDelegate.getDefaultNightMode() ==
                AppCompatDelegate.MODE_NIGHT_YES
                || resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        val defaultCssId = if (isNightMode) {
            R.string.pref_custom_css_default_dark
        } else {
            R.string.pref_custom_css_default
        }
        val css: String? = getString(defaultCssId)

        infoWebview.loadDataWithBaseURL(null,
                String.format(FORMAT_CSS, css) + intent.getStringExtra("html"),
                "text/html",
                "UTF-8", null
        )
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        var FORMAT_CSS = "<style>" +
                "%s" +
                "</style>"
    }
}
