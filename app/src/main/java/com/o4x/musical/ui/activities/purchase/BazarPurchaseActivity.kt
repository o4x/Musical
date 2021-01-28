package com.o4x.musical.ui.activities.purchase

//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import com.o4x.musical.prefs.AppPref
//import ir.cafebazaar.poolakey.Connection
//import ir.cafebazaar.poolakey.Payment
//import ir.cafebazaar.poolakey.config.PaymentConfiguration
//import ir.cafebazaar.poolakey.config.SecurityCheck
//import ir.cafebazaar.poolakey.entity.PurchaseInfo
//import ir.cafebazaar.poolakey.entity.PurchaseState
//import ir.cafebazaar.poolakey.request.PurchaseRequest
//
//class BazarPurchaseActivity : AbsPurchaseActivity() {
//
//    companion object {
//        private const val TAG = "BazarPurchaseActivity"
//        private const val SKU_CLEAN = "clean" // you can find it on bazar dashboard
//        private const val RC_REQUEST = 1003
//    }
//
//    private lateinit var localSecurityCheck: SecurityCheck.Enable
//    private lateinit var paymentConfiguration: PaymentConfiguration
//    private lateinit var payment: Payment
//    private lateinit var paymentConnection: Connection
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding.restoreButton.apply {
//            isEnabled = false
//            setOnClickListener { restore() }
//        }
//        binding.purchaseButton.apply {
//            isEnabled = false
//            setOnClickListener { purchase() }
//        }
//        setupConnection()
//    }
//
//    public override fun onDestroy() {
//        paymentConnection.disconnect()
//        super.onDestroy()
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        payment.onActivityResult(requestCode, resultCode, data) {
//            purchaseSucceed { purchaseEntity ->
//                enableClean(purchaseEntity)
//                toastProductPurchased()
//            }
//            purchaseCanceled {
//
//            }
//            purchaseFailed { throwable ->
//                Log.e(TAG, throwable.stackTraceToString())
//            }
//        }
//    }
//
//    private fun setupConnection() {
//        val base64EncodedPublicKey =
//            "MIHNMA0GCSqGSIb3DQEBAQUAA4G7ADCBtwKBrwDiDBMb1QjhuplIqRPi7NnWxtxwMu7Ek7XRxbC2b7qPx3zxcrCmtWVmBYz0bZbIavHU3/l2xSZ1WTiCXplNqDW983Zx7H5e2IJd7TxdaOU4FbKw7/pRRLsGPPT0UEhGlFWI1NYQn/9zVJOAjZddvaiRUYCtpJomPUWkZUjaToSkORWokDqe9I4eSYTeiTlH7+llbzuDZf2y32rb5HFntdVqs0A5ZjNR6co6q4p6vRcCAwEAAQ=="
//
//        localSecurityCheck = SecurityCheck.Enable(
//            rsaPublicKey = base64EncodedPublicKey
//        )
//        paymentConfiguration = PaymentConfiguration(
//            localSecurityCheck = localSecurityCheck
//        )
//        payment = Payment(context = this, config = paymentConfiguration)
//        paymentConnection = payment.connect {
//            connectionSucceed {
//                binding.restoreButton.isEnabled = true
//                binding.purchaseButton.isEnabled = true
//            }
//            connectionFailed { throwable ->
//                Log.e(TAG, throwable.stackTraceToString())
//            }
//            disconnected {
//
//            }
//        }
//    }
//
//    private fun purchase() {
//        val purchaseRequest = PurchaseRequest(
//            productId = SKU_CLEAN,
//            requestCode = RC_REQUEST,
//            payload = ""
//        )
//
//        payment.purchaseProduct(
//            activity = this,
//            request = purchaseRequest
//        ) {
//            purchaseFlowBegan {
//
//            }
//            failedToBeginFlow { throwable ->
//                Log.e(TAG, throwable.stackTraceToString())
//            }
//        }
//    }
//
//    private fun restore() {
//        toastRestoringPurchase()
//        payment.getPurchasedProducts {
//            querySucceed { purchasedProducts ->
//                val product = purchasedProducts.find {
//                    it.productId == SKU_CLEAN
//                }
//                product?.let {
//                    enableClean(it)
//                }
//                toastPurchaseHistoryRestored()
//            }
//            queryFailed { throwable ->
//                Log.e(TAG, throwable.stackTraceToString())
//                toastPurchaseHistoryRestored()
//            }
//        }
//    }
//
//    private fun enableClean(purchaseInfo: PurchaseInfo) {
//        if (purchaseInfo.productId == SKU_CLEAN &&
//            purchaseInfo.purchaseState == PurchaseState.PURCHASED) {
//            AppPref.isCleanVersion = true
//            finish()
//        }
//    }
//}