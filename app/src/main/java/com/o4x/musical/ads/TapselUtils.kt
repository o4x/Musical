package com.o4x.musical.ads

import android.app.Activity
import android.app.Application
import android.util.Log
import android.view.ViewGroup
import com.o4x.musical.App
import com.o4x.musical.extensions.hide
import com.o4x.musical.extensions.show
import com.o4x.musical.helper.MusicPlayerRemote
import ir.tapsell.plus.AdRequestCallback
import ir.tapsell.plus.AdShowListener
import ir.tapsell.plus.TapsellPlus
import ir.tapsell.plus.TapsellPlusBannerType


class TapselUtils(val activity: Activity) {

    companion object {
        private const val TAG = "TapselUtils"
        private const val TAPSEL_KEY = "dofbirfkifktctjjeqoasigdkpdoqloqkoethclgiegjbbeislgcralndiihdkblistdic"
        private const val INTERSTITIAL_VIDEO_ID = "5fe442a85f5f670001a734c7"
        private const val INTERSTITIAL_BANNER_ID = "5fe4db57ebc784000124f473"
        private const val STANDARD_BANNER_ID = "5fe4dd79ebc784000124f474"

        fun initialize(app: Application) {
            TapsellPlus.initialize(app, TAPSEL_KEY)
        }
    }

    private val isNotClean = !App.isCleanVersion()

    fun showInterstitialVideo() {
        if (isNotClean)
        TapsellPlus.requestInterstitial(activity,
            INTERSTITIAL_VIDEO_ID,
            object : AdRequestCallback() {
                override fun response() {
                    super.response()
                    TapsellPlus.showAd(activity,
                        INTERSTITIAL_VIDEO_ID,
                        object : AdShowListener() {
                            override fun onOpened() {}
                            override fun onClosed() {}
                            override fun onError(message: String) {
                                Log.e(TAG, message)
                            }

                            override fun onRewarded() {}
                        })
                }

                override fun error(p0: String?) {
                    super.error(p0)
                    Log.e(TAG, p0.toString())
                }
            })
    }

    fun showInterstitialBanner() {
        if (isNotClean)
            TapsellPlus.requestInterstitial(activity,
                INTERSTITIAL_BANNER_ID,
                object : AdRequestCallback() {
                    override fun response() {
                        super.response()
                        TapsellPlus.showAd(activity,
                            INTERSTITIAL_BANNER_ID,
                            object : AdShowListener() {
                                override fun onOpened() {}
                                override fun onClosed() {}
                                override fun onError(message: String) {
                                    Log.e(TAG, message)
                                }

                                override fun onRewarded() {}
                            })
                    }

                    override fun error(p0: String?) {
                        super.error(p0)
                        Log.e(TAG, p0.toString())
                    }
                })
    }

    fun loadStandardBanner(banner: ViewGroup) {
        if (isNotClean) {
            TapsellPlus.showBannerAd(
                activity,
                banner,
                STANDARD_BANNER_ID,
                TapsellPlusBannerType.BANNER_320x50,
                object : AdRequestCallback() {
                    override fun response() {}

                    override fun error(message: String?) {
                        Log.e(TAG, message.toString())
                    }
                })
        }
    }

    fun showSmartInterstitial() {
        if (MusicPlayerRemote.isPlaying) {
            showInterstitialBanner()
        } else {
            showInterstitialVideo()
        }
    }
}