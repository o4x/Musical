/*
 * Copyright (c) 2019 Hemanth Savarala.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by
 *  the Free Software Foundation either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package com.o4x.musical

import android.app.Application
import android.widget.Toast
import code.name.monkey.appthemehelper.util.VersionUtils
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.o4x.musical.ads.TapselUtils
import com.o4x.musical.appshortcuts.DynamicShortcutManager
import com.o4x.musical.prefs.AppPref
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    lateinit var billingProcessor: BillingProcessor

    override fun onCreate() {

        super.onCreate()
        TapselUtils.initialize(this)

        instance = this

        startKoin {
            androidContext(this@App)
            modules(appModules)
        }

        if (VersionUtils.hasNougatMR())
            DynamicShortcutManager(this).initDynamicShortcuts()

        // automatically restores purchases
        billingProcessor = BillingProcessor(
            this,
            Constants.GOOGLE_PLAY_LICENSING_KEY,
            object : BillingProcessor.IBillingHandler {
                override fun onProductPurchased(productId: String, details: TransactionDetails?) {}

                override fun onPurchaseHistoryRestored() {
                    Toast.makeText(
                        this@App,
                        R.string.restored_previous_purchase_please_restart,
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onBillingError(errorCode: Int, error: Throwable?) {}

                override fun onBillingInitialized() {}
            })
    }

    override fun onTerminate() {
        super.onTerminate()
        billingProcessor.release()
    }

    companion object {
        private var instance: App? = null

        fun getContext(): App {
            return instance!!
        }

        fun isCleanVersion(): Boolean {
//            return BuildConfig.DEBUG || instance?.billingProcessor!!.isPurchased(
//                Constants.CLEAN_VERSION_PRODUCT_ID
//            )
            return instance?.billingProcessor!!.isPurchased(
                Constants.CLEAN_VERSION_PRODUCT_ID
            )
        }
    }
}
