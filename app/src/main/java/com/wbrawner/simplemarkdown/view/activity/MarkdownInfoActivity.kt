package com.wbrawner.simplemarkdown.view.activity

import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.wbrawner.simplemarkdown.MarkdownApplication
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.utility.readAssetToString
import com.wbrawner.simplemarkdown.utility.toHtml
import kotlinx.android.synthetic.main.activity_markdown_info.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MarkdownInfoActivity : AppCompatActivity(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_markdown_info)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val title = intent?.getStringExtra(EXTRA_TITLE)
        val fileName = intent?.getStringExtra(EXTRA_FILE)
        if (title.isNullOrBlank() || fileName.isNullOrBlank()) {
            finish()
            return
        }

        val isNightMode = AppCompatDelegate.getDefaultNightMode() ==
                AppCompatDelegate.MODE_NIGHT_YES
                || resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        val defaultCssId = if (isNightMode) {
            R.string.pref_custom_css_default_dark
        } else {
            R.string.pref_custom_css_default
        }
        val css: String? = getString(defaultCssId)
        launch {
            try {
                val html = assets?.readAssetToString(fileName)
                        ?.toHtml()
                        ?: throw RuntimeException("Unable to open stream to $fileName")
                infoWebview.loadDataWithBaseURL(null,
                        String.format(FORMAT_CSS, css) + html,
                        "text/html",
                        "UTF-8", null
                )
            } catch (e: Exception) {
                (application as MarkdownApplication).errorHandler.reportException(e)
                Toast.makeText(this@MarkdownInfoActivity, R.string.file_load_error, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        coroutineContext[Job]?.let {
            cancel()
        }
        super.onDestroy()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val FORMAT_CSS = "<style>" +
                "%s" +
                "</style>"
        const val EXTRA_TITLE = "title"
        const val EXTRA_FILE = "file"
    }
}
