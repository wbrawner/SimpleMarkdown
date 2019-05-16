package com.wbrawner.simplemarkdown.view.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import java.util.*

class SupportActivity : AppCompatActivity(), BillingClientStateListener, PurchasesUpdatedListener {
    private lateinit var billingClient: BillingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        billingClient = BillingClient.newBuilder(applicationContext)
                .setListener(this)
                .build()
        billingClient.startConnection(this)

    }

    override fun onBillingSetupFinished(responseCode: Int) {
        if (responseCode != BillingClient.BillingResponse.OK) {
            return
        }

        // The billing client is ready. You can query purchases here.
        val skuList = ArrayList<String>()
        skuList.add("support_the_developer")
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
        billingClient.querySkuDetailsAsync(params.build()) { responseCode1, skuDetailsList ->
            // Process the result.
            if (responseCode1 != BillingClient.BillingResponse.OK || skuDetailsList == null) {
                return@querySkuDetailsAsync
            }

            val skuDetails = skuDetailsList!!.get(0)
            val sku = skuDetails.getSku()
            val price = skuDetails.getPrice()
            Log.d("SimpleMarkdown",
                    "Got product with sku: " + sku + " and price: " + price + " " + skuDetails.getPriceCurrencyCode())
            val flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails)
                    .build()
            val responseCode2 = billingClient.launchBillingFlow(this, flowParams)
        }
    }

    override fun onBillingServiceDisconnected() {
        // TODO: Set a flag and just try again later
        billingClient.startConnection(this)
    }

    override fun onPurchasesUpdated(responseCode: Int, purchases: List<Purchase>?) {

    }

}