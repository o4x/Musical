package com.o4x.musical.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.o4x.musical.App
import com.o4x.musical.helper.MusicPlayerRemote


class AdsUtils(val activity: Activity) {

    companion object {
        private const val TAG = "AdsUtils"
        private const val INTERSTITIAL_VIDEO_ID = "ca-app-pub-8217710892260661/8260304214"
        private const val INTERSTITIAL_BANNER_ID = "ca-app-pub-8217710892260661/9102806115"
//        private const val BANNER_ID = "ca-app-pub-8217710892260661/3993756143"

        fun initialize(context: Context) {
            MobileAds.initialize(context) { }
        }
    }

    private val isNotClean = !App.isCleanVersion()

    private fun showInterstitialVideo() {
        if (isNotClean) {
            val adRequest: AdRequest = AdRequest.Builder().build()

            InterstitialAd.load(
                activity,
                INTERSTITIAL_VIDEO_ID,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        Log.i(TAG, "onAdLoaded")
                        interstitialAd.fullScreenContentCallback = object: FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                Log.d(TAG, "Ad was dismissed.")
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                                Log.d(TAG, "Ad failed to show.")
                            }

                            override fun onAdShowedFullScreenContent() {
                                Log.d(TAG, "Ad showed fullscreen content.")
                            }
                        }

                        interstitialAd.show(activity)
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        // Handle the error
                        Log.i(TAG, loadAdError.message)
                    }
                })
        }
    }

    private fun showInterstitialBanner() {
        if (isNotClean) {
            val adRequest: AdRequest = AdRequest.Builder().build()

            InterstitialAd.load(
                activity,
                INTERSTITIAL_BANNER_ID,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        Log.i(TAG, "onAdLoaded")
                        interstitialAd.fullScreenContentCallback = object: FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                Log.d(TAG, "Ad was dismissed.")
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                                Log.d(TAG, "Ad failed to show.")
                            }

                            override fun onAdShowedFullScreenContent() {
                                Log.d(TAG, "Ad showed fullscreen content.")
                            }
                        }

                        interstitialAd.show(activity)
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        // Handle the error
                        Log.i(TAG, loadAdError.message)
                    }
                })
        }
    }

    fun loadStandardBanner(mAdView: AdView) {
        if (isNotClean) {
            val adRequest = AdRequest.Builder().build()
            mAdView.loadAd(adRequest)
            mAdView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, adError.message)
                }

                override fun onAdOpened() {
                    // Code to be executed when an ad opens an overlay that
                    // covers the screen.
                }

                override fun onAdClicked() {
                    // Code to be executed when the user clicks on an ad.
                }

                override fun onAdLeftApplication() {
                    // Code to be executed when the user has left the app.
                }

                override fun onAdClosed() {
                    // Code to be executed when the user is about to return
                    // to the app after tapping on an ad.
                }
            }
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