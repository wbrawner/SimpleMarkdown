package com.wbrawner.simplemarkdown.view.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.Observer
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.utility.SupportLinkProvider
import kotlinx.android.synthetic.main.activity_support.*


class SupportActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support)
        setSupportActionBar(toolbar)
        setTitle(R.string.support_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        githubButton.setOnClickListener {
            CustomTabsIntent.Builder()
                    .addDefaultShareMenuItem()
                    .build()
                    .launchUrl(this@SupportActivity, Uri.parse("https://github.com/wbrawner/SimpleMarkdown"))
        }
        rateButton.setOnClickListener {
            val playStoreIntent = Intent(Intent.ACTION_VIEW)
                    .apply {
                        data = Uri.parse("market://details?id=${packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                                Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                    }
            try {
                startActivity(playStoreIntent)
            } catch (ignored: ActivityNotFoundException) {
                playStoreIntent.data = Uri.parse("https://play.google.com/store/apps/details?id=${packageName}")
                startActivity(playStoreIntent)
            }
        }
        SupportLinkProvider(this).supportLinks.observe(this, Observer { links ->
            links.forEach {
                supportButtons.addView(it)
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}