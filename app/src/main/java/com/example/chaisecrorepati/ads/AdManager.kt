package com.example.chaisecrorepati.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdManager(private val context: Context) {

    // TEST ID — apna real ID yahan lagao jab AdMob account ban jaye
    private val adUnitId = "ca-app-pub-1818705176239407/2738409205"  // TEST rewarded ID

    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    companion object {
        private const val TAG = "AdManager"
    }

    init {
        MobileAds.initialize(context) {
            Log.d(TAG, "AdMob initialized")
            loadAd()
        }
    }

    fun loadAd() {
        if (isLoading || rewardedAd != null) return
        isLoading = true

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, adUnitId, adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                    Log.d(TAG, "Ad loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isLoading = false
                    Log.e(TAG, "Ad failed to load: ${error.message}")
                }
            })
    }

    fun isAdReady(): Boolean = rewardedAd != null

    fun showAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdDismissed: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad == null) {
            Log.w(TAG, "Ad not ready yet")
            onAdDismissed()
            loadAd()  // reload karo
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadAd()  // agla ad preload karo
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                loadAd()
                Log.e(TAG, "Ad failed to show: ${error.message}")
            }
        }

        ad.show(activity) {
            // User ne pura ad dekha — reward do
            Log.d(TAG, "User earned reward")
            onRewardEarned()
        }
    }
}

