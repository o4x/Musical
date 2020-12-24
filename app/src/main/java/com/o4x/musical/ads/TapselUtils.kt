package com.o4x.musical.ads

import android.content.Context
import android.util.Log
import com.o4x.musical.extensions.hide
import com.o4x.musical.extensions.show
import ir.tapsell.sdk.*
import ir.tapsell.sdk.bannerads.TapsellBannerType
import ir.tapsell.sdk.bannerads.TapsellBannerView
import ir.tapsell.sdk.bannerads.TapsellBannerViewEventListener
import java.util.*


class TapselUtils(val context: Context) {

    companion object {
        private const val TAG = "TapselUtils"
        private const val REWARD_BASED_ID = "5fe4eef05f5f670001a7351e"
        private const val INTERSTITIAL_VIDEO_ID = "5fe442a85f5f670001a734c7"
        private const val INTERSTITIAL_BANNER_ID = "5fe4db57ebc784000124f473"
        private const val STANDARD_BANNER_ID = "5fe4dd79ebc784000124f474"
    }

    fun showRewardBased() {
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
        banner.loadAd(context, STANDARD_BANNER_ID, TapsellBannerType.BANNER_320x50)
        banner.setEventListener(object : TapsellBannerViewEventListener {
            override fun onRequestFilled() {
                banner.show()
            }

            override fun onNoAdAvailable() {}
            override fun onNoNetwork() {}
            override fun onError(message: String) {
                Log.e(TAG, message)
            }
            override fun onHideBannerView() {}
        })
    }

    fun showRandomsInterstitial() {
        val random = Random()
        val numberOfMethods = 2

        when (random.nextInt(numberOfMethods)) {
            0 -> showInterstitialVideo()
            1 -> showInterstitialBanner()
            else -> showInterstitialBanner()
        }

    }

}