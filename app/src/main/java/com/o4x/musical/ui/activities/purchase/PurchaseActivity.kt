/*
 * Copyright (c) 2020 Hemanth Savarla.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package com.o4x.musical.ui.activities.purchase

//import android.content.Intent
//import android.os.AsyncTask
//import android.os.Bundle
//import android.util.Log
//import android.widget.Toast
//import com.anjlab.android.iab.v3.BillingProcessor
//import com.anjlab.android.iab.v3.TransactionDetails
//import com.o4x.musical.BuildConfig
//import com.o4x.musical.Constants.PRO_VERSION_PRODUCT_ID
//import com.o4x.musical.R
//import java.lang.ref.WeakReference
//
//class PurchaseActivity : AbsPurchaseActivity(), BillingProcessor.IBillingHandler {
//
//    companion object {
//        private const val TAG: String = "PurchaseActivity"
//    }
//
//    private lateinit var billingProcessor: BillingProcessor
//    private var restorePurchaseAsyncTask: AsyncTask<*, *, *>? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding.restoreButton.isEnabled = false
//        binding.purchaseButton.isEnabled = false
//
//        billingProcessor =
//            BillingProcessor(this, BuildConfig.GOOGLE_PLAY_LICENSING_KEY, this)
//
//        binding.restoreButton.setOnClickListener {
//            if (restorePurchaseAsyncTask == null || restorePurchaseAsyncTask!!.status != AsyncTask.Status.RUNNING) {
//                restorePurchase()
//            }
//        }
//        binding.purchaseButton.setOnClickListener {
//            billingProcessor.purchase(this@PurchaseActivity, PRO_VERSION_PRODUCT_ID)
//        }
//    }
//
//    private fun restorePurchase() {
//        if (restorePurchaseAsyncTask != null) {
//            restorePurchaseAsyncTask!!.cancel(false)
//        }
//        restorePurchaseAsyncTask = RestorePurchaseAsyncTask(this).execute()
//    }
//
//    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
//        toastProductPurchased()
//        setResult(RESULT_OK)
//    }
//
//    override fun onPurchaseHistoryRestored() {
//        toastPurchaseHistoryRestored()
//    }
//
//    override fun onBillingError(errorCode: Int, error: Throwable?) {
//        Log.e(TAG, "Billing error: code = $errorCode", error)
//    }
//
//    override fun onBillingInitialized() {
//        binding.restoreButton.isEnabled = true
//        binding.purchaseButton.isEnabled = true
//    }
//
//    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (!billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
//            super.onActivityResult(requestCode, resultCode, data)
//        }
//    }
//
//    override fun onDestroy() {
//        billingProcessor.release()
//        super.onDestroy()
//    }
//
//    private inner class RestorePurchaseAsyncTask(purchaseActivity: PurchaseActivity) :
//        AsyncTask<Void, Void, Boolean>() {
//
//        private val buyActivityWeakReference: WeakReference<PurchaseActivity> = WeakReference(
//            purchaseActivity
//        )
//
//        override fun onPreExecute() {
//            super.onPreExecute()
//            val purchaseActivity = buyActivityWeakReference.get()
//            if (purchaseActivity != null) {
//                toastRestoringPurchase()
//            } else {
//                cancel(false)
//            }
//        }
//
//        override fun doInBackground(vararg params: Void): Boolean? {
//            val purchaseActivity = buyActivityWeakReference.get()
//            if (purchaseActivity != null) {
//                return purchaseActivity.billingProcessor.loadOwnedPurchasesFromGoogle()
//            }
//            cancel(false)
//            return null
//        }
//
//        override fun onPostExecute(b: Boolean?) {
//            super.onPostExecute(b)
//            val purchaseActivity = buyActivityWeakReference.get()
//            if (purchaseActivity == null || b == null) {
//                return
//            }
//
//            if (b) {
//                purchaseActivity.onPurchaseHistoryRestored()
//            } else {
//                toastCantRestorePurchase()
//            }
//        }
//    }
//}
