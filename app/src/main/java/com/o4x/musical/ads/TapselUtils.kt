package com.o4x.musical.ads

import android.app.Application
import android.content.Context
import android.util.Log
import com.o4x.musical.extensions.hide
import com.o4x.musical.extensions.show
import com.o4x.musical.helper.MusicPlayerRemote
import com.o4x.musical.prefs.AppPref
import ir.tapsell.sdk.*
import ir.tapsell.sdk.bannerads.TapsellBannerType
import ir.tapsell.sdk.bannerads.TapsellBannerView
import ir.tapsell.sdk.bannerads.TapsellBannerViewEventListener
import java.util.*


class TapselUtils(val context: Context) {

    companion object {
        private const val TAG = "TapselUtils"
        private const val TAPSEL_KEY = "dofbirfkifktctjjeqoasigdkpdoqloqkoethclgiegjbbeislgcralndiihdkblistdic"
        private const val REWARD_BASED_ID = "5fe4eef05f5f670001a7351e"
        private const val INTERSTITIAL_VIDEO_ID = "5fe442a85f5f670001a734c7"
        private const val INTERSTITIAL_BANNER_ID = "5fe4db57ebc784000124f473"
        private const val STANDARD_BANNER_ID = "5fe4dd79ebc784000124f474"

        fun initialize(app: Application) {
            Tapsell.initialize(app, TAPSEL_KEY)
        }
    }

    private val isNotClean = !AppPref.isCleanVersion

    fun showRewardBased() {
        if (isNotClean)
        Tapsell.requestAd(context,
            REWARD_BASED_ID,
            TapsellAdRequestOptions(),
            object : TapsellAdRequestListener() {
                override fun onAdAvailable(adId: String) {
                    Tapsell.showAd(context,
                        REWARD_BASED_ID,
                        adId,
                        TapsellShowOptions(),
                        object : TapsellAdShowListener() {
                            override fun onOpened() {}
                            override fun onClosed() {}
                            override fun onError(message: String) {
                                Log.e(TAG, message)
                            }
                            override fun onRewarded(completed: Boolean) {}
                        })
                }

                override fun onError(message: String) {
                    Log.e(TAG, message)
                }
            })
    }

    fun showInterstitialVideo() {
        if (isNotClean)
        Tapsell.requestAd(context,
            INTERSTITIAL_VIDEO_ID,
            TapsellAdRequestOptions(),
            object : TapsellAdRequestListener() {
                override fun onAdAvailable(adId: String) {
                    Tapsell.showAd(context,
                        INTERSTITIAL_VIDEO_ID,
                        adId,
                        TapsellShowOptions(),
                        object : TapsellAdShowListener() {
                            override fun onOpened() {}
                            override fun onClosed() {}
                            override fun onError(message: String) {
                                Log.e(TAG, message)
                            }
                            override fun onRewarded(completed: Boolean) {}
                        })
                }

                override fun onError(message: String) {
                    Log.e(TAG, message)
                }
            })
    }

    fun showInterstitialBanner() {
        if (isNotClean)
        Tapsell.requestAd(context,
            INTERSTITIAL_BANNER_ID,
            TapsellAdRequestOptions(),
            object : TapsellAdRequestListener() {
                override fun onAdAvailable(adId: String) {
                    Tapsell.showAd(context,
                        INTERSTITIAL_BANNER_ID,
                        adId,
                        TapsellShowOptions(),
                        object : TapsellAdShowListener() {
                            override fun onOpened() {}
                            override fun onClosed() {}
                            override fun onError(message: String) {
                                Log.e(TAG, message)
                            }
                            override fun onRewarded(completed: Boolean) {}
                        })
                }

                override fun onError(message: String) {
                    Log.e(TAG, message)
                }
            })
    }

    fun loadStandardBanner(banner: TapsellBannerView) {
        banner.hide()
        if (isNotClean)
        banner.loadAd(context, STANDARD_BANNER_ID, TapsellBannerType.BANNER_320x50)
        banner.setEventListener(object : TapsellBannerViewEventListener {
            override fun onRequestFilled() {
                banner.show()
            }
            override fun onNoAdAvailable() {
                banner.hide()
            }
            override fun onNoNetwork() {
                banner.hide()
            }
            override fun onError(message: String) {
                banner.hide()
                Log.e(TAG, message)
            }
            override fun onHideBannerView() {
                banner.hide()
            }
        })
    }

    fun showSmartInterstitial() {
        if (MusicPlayerRemote.isPlaying) {
            showInterstitialBanner()
        } else {
            showInterstitialVideo()
        }
    }

}