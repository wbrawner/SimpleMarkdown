package com.wbrawner.simplemarkdown.view.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.android.billingclient.api.*
import com.google.android.material.button.MaterialButton
import com.wbrawner.simplemarkdown.R
import kotlinx.android.synthetic.main.activity_support.*


class SupportActivity : AppCompatActivity(), BillingClientStateListener, PurchasesUpdatedListener {
    private lateinit var billingClient: BillingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support)
        setSupportActionBar(toolbar)
        window.decorView.apply {
            systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    )
        }
        setTitle(R.string.support_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        billingClient = BillingClient.newBuilder(applicationContext)
                .setListener(this)
                .enablePendingPurchases()
                .build()
        billingClient.startConnection(this)
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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBillingSetupFinished(result: BillingResult) {
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            return
        }

        val skuDetails = SkuDetailsParams.newBuilder()
                .setSkusList(listOf("support_the_developer", "tip_coffee", "tip_beer"))
                .setType(BillingClient.SkuType.INAPP)
                .build()
        billingClient.querySkuDetailsAsync(skuDetails) { skuDetailsResponse, skuDetailsList ->
            // Process the result.
            if (skuDetailsResponse.responseCode != BillingClient.BillingResponseCode.OK || skuDetailsList.isNullOrEmpty()) {
                return@querySkuDetailsAsync
            }

            skuDetailsList.sortedBy { it.priceAmountMicros }.forEach { skuDetails ->
                val supportButton = MaterialButton(this@SupportActivity)
                supportButton.text = getString(
                        R.string.support_button_purchase,
                        skuDetails.title,
                        skuDetails.price
                )
                supportButton.setOnClickListener {
                    val flowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(skuDetails)
                            .build()
                    billingClient.launchBillingFlow(this, flowParams)
                }
                supportButtons.addView(supportButton)
            }
        }
    }

    override fun onBillingServiceDisconnected() {
        billingClient.startConnection(this)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        purchases?.forEach { purchase ->
            val consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
            billingClient.consumeAsync(consumeParams) { _, _ ->
                Toast.makeText(
                        this@SupportActivity,
                        getString(R.string.support_thank_you),
                        Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}