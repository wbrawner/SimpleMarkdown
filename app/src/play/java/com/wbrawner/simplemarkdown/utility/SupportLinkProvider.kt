package com.wbrawner.simplemarkdown.utility

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import com.google.android.material.button.MaterialButton
import com.wbrawner.simplemarkdown.R

class SupportLinkProvider(private val activity: Activity) : BillingClientStateListener,
        PurchasesUpdatedListener {
    val supportLinks = MutableLiveData<List<MaterialButton>>()

    private val billingClient: BillingClient = BillingClient.newBuilder(activity.applicationContext)
            .setListener(this)
            .enablePendingPurchases()
            .build()

    init {
        billingClient.startConnection(this)
        activity.application.registerActivityLifecycleCallbacks(
                object : Application.ActivityLifecycleCallbacks {
                    override fun onActivityPaused(activity: Activity) {
                    }

                    override fun onActivityStarted(activity: Activity) {
                    }

                    override fun onActivityDestroyed(activity: Activity) {
                        billingClient.endConnection()
                    }

                    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                    }

                    override fun onActivityStopped(activity: Activity) {
                    }

                    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    }

                    override fun onActivityResumed(activity: Activity) {
                    }
                }
        )
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

            skuDetailsList.sortedBy { it.priceAmountMicros }
                    .map { skuDetails ->
                        val supportButton = MaterialButton(activity)
                        supportButton.text = activity.getString(
                                R.string.support_button_purchase,
                                skuDetails.title,
                                skuDetails.price
                        )
                        supportButton.setOnClickListener {
                            val flowParams = BillingFlowParams.newBuilder()
                                    .setSkuDetails(skuDetails)
                                    .build()
                            billingClient.launchBillingFlow(activity, flowParams)
                        }
                        supportButton
                    }
                    .let {
                        supportLinks.postValue(it)
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
                        activity,
                        activity.getString(R.string.support_thank_you),
                        Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}