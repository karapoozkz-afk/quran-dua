package app.qurandua.android.platform

import android.app.Activity
import android.content.Context
import app.qurandua.android.ui.SupportController
import app.qurandua.android.ui.SupportOption
import app.qurandua.android.ui.SupportResult
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.lang.ref.WeakReference

/**
 * "Support the project" through Google Play: consumable one-time products whose money goes to
 * the developer's own Play merchant account. The app never sees card data and never holds money.
 *
 * The products must exist in Play Console (Monetize → In-app products) with the ids in
 * [PRODUCT_IDS]. Until they do, or when the app was not installed from Play, [options] stays
 * empty and the screen shows no buttons.
 */
class PlayBillingSupport(context: Context) : SupportController {

    companion object {
        val PRODUCT_IDS = listOf("support_small", "support_medium", "support_large")
    }

    private val _options = MutableStateFlow<List<SupportOption>>(emptyList())
    override val options: StateFlow<List<SupportOption>> = _options

    private val _result = MutableStateFlow<SupportResult?>(null)
    override val result: StateFlow<SupportResult?> = _result

    private var details: Map<String, ProductDetails> = emptyMap()

    /** The activity on screen; MainActivity sets it, the purchase sheet needs one. */
    var activity: WeakReference<Activity>? = null

    private val client: BillingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener { billing, purchases -> onPurchases(billing, purchases) }
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection()
        .build()

    override fun connect() {
        if (client.isReady) {
            loadProducts()
            return
        }
        runCatching {
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billing: BillingResult) {
                    if (billing.responseCode == BillingClient.BillingResponseCode.OK) {
                        loadProducts()
                        finishOldPurchases()
                    }
                }

                override fun onBillingServiceDisconnected() = Unit
            })
        }
    }

    private fun loadProducts() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                PRODUCT_IDS.map {
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(it)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                },
            )
            .build()
        client.queryProductDetailsAsync(params) { billing, found ->
            if (billing.responseCode != BillingClient.BillingResponseCode.OK) return@queryProductDetailsAsync
            val list = found.productDetailsList
            details = list.associateBy { it.productId }
            _options.value = PRODUCT_IDS.mapNotNull { id ->
                val product = details[id] ?: return@mapNotNull null
                @Suppress("DEPRECATION")
                val price = product.oneTimePurchaseOfferDetails?.formattedPrice ?: return@mapNotNull null
                SupportOption(id, price)
            }
        }
    }

    override fun buy(productId: String) {
        val product = details[productId]
        val screen = activity?.get()
        if (product == null || screen == null || screen.isFinishing) {
            _result.value = SupportResult.Failed
            return
        }
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(product).build()),
            )
            .build()
        val launch = client.launchBillingFlow(screen, params)
        if (launch.responseCode != BillingClient.BillingResponseCode.OK &&
            launch.responseCode != BillingClient.BillingResponseCode.USER_CANCELED
        ) {
            _result.value = SupportResult.Failed
        }
    }

    override fun clearResult() {
        _result.value = null
    }

    private fun onPurchases(billing: BillingResult, purchases: List<Purchase>?) {
        when (billing.responseCode) {
            BillingClient.BillingResponseCode.OK -> purchases.orEmpty().forEach { handle(it, announce = true) }
            BillingClient.BillingResponseCode.USER_CANCELED -> Unit
            else -> _result.value = SupportResult.Failed
        }
    }

    /** A purchase paid while the app was closed, or a pending one that went through later. */
    private fun finishOldPurchases() {
        val params = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        client.queryPurchasesAsync(params) { billing, purchases ->
            if (billing.responseCode == BillingClient.BillingResponseCode.OK) purchases.forEach { handle(it, announce = false) }
        }
    }

    private fun handle(purchase: Purchase, announce: Boolean) {
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                // Consuming acknowledges the payment (otherwise Play refunds it after 3 days)
                // and lets the person support again with the same product.
                val consume = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
                client.consumeAsync(consume) { result, _ ->
                    if (announce && result.responseCode == BillingClient.BillingResponseCode.OK) {
                        _result.value = SupportResult.Thanks
                    }
                }
            }
            Purchase.PurchaseState.PENDING -> if (announce) _result.value = SupportResult.Pending
            else -> Unit
        }
    }
}
