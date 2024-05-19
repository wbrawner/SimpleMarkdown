package com.wbrawner.simplemarkdown.utility

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.QueryProductDetailsParams
import com.wbrawner.simplemarkdown.R
import timber.log.Timber

@Composable
fun SupportLinks() {
    val context = LocalContext.current
    var products by remember { mutableStateOf(emptyList<ProductDetails>()) }
    var billingClient by remember { mutableStateOf<BillingClient?>(null) }
    DisposableEffect(context) {
        billingClient = BillingClient.newBuilder(context.applicationContext)
            .setListener { _, purchases ->
                purchases?.forEach { purchase ->
                    val consumeParams = ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient?.consumeAsync(consumeParams) { _, _ ->
                        Toast.makeText(
                            context,
                            context.getString(R.string.support_thank_you),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .enablePendingPurchases()
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                billingClient?.startConnection(this)
            }

            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                    return
                }
                val productsQuery = listOf("support_the_developer", "tip_coffee", "tip_beer")
                    .map {
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(it)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()
                    }
                val productDetailsQuery = QueryProductDetailsParams.newBuilder()
                    .setProductList(productsQuery)
                    .build()
                billingClient?.queryProductDetailsAsync(productDetailsQuery) { result, productDetails ->
                    if (result.responseCode != BillingClient.BillingResponseCode.OK || productDetails.isEmpty()) {
                        Timber.w("Failed to load product details: ${result.debugMessage}")
                        return@queryProductDetailsAsync
                    }
                    products =
                        productDetails.sortedBy { it.oneTimePurchaseOfferDetails?.priceAmountMicros }
                            .toList()
                }
            }
        })

        onDispose {
            billingClient?.endConnection()
        }
    }

    products.forEach { product ->
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val productDetails = ProductDetailsParams.newBuilder()
                    .setProductDetails(product)
                    .build()
                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(listOf(productDetails))
                    .build()
                billingClient?.launchBillingFlow(context as Activity, flowParams)
            }
        ) {
            Text(
                context.getString(
                    R.string.support_button_purchase,
                    product.name,
                    product.oneTimePurchaseOfferDetails?.formattedPrice
                )
            )
        }
    }
}