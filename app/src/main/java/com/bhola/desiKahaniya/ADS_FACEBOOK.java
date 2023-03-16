package com.bhola.desiKahaniya;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;

public class ADS_FACEBOOK {


    private static InterstitialAd minterstitialAd;


    public static void interstitialAd(Context context, InterstitialAd interstitialAd, String Adunit_ID) {
        minterstitialAd = interstitialAd;
        String TAG = "TAGA";

        AudienceNetworkAds.initialize(context);


        minterstitialAd = new InterstitialAd(context, Adunit_ID);
        // Create listeners for the Interstitial Ad
        InterstitialAd finalInterstitialAd = interstitialAd;
        InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
                // Interstitial ad displayed callback
                Log.e(TAG, "Interstitial ad displayed.");
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                // Interstitial dismissed callback
                Log.e(TAG, "Interstitial ad dismissed.");
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Ad error callback
                Log.e(TAG, "Interstitial ad failed to load: " + adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Interstitial ad is loaded and ready to be displayed
                Log.d(TAG, "Interstitial ad is loaded and ready to be displayed!");
                // Show the ad
                try {
                    minterstitialAd.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onAdClicked(Ad ad) {
                // Ad clicked callback
                Log.d(TAG, "Interstitial ad clicked!");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Ad impression logged callback
                Log.d(TAG, "Interstitial ad impression logged!");
            }
        };

        // For auto play video ads, it's recommended to load the ad
        // at least 30 seconds before it is shown
        minterstitialAd.loadAd(
                minterstitialAd.buildLoadAdConfig()
                        .withAdListener(interstitialAdListener)
                        .build());
    }


    public static void bannerAds(Context context, AdView madView, LinearLayout facebook_bannerAd_layput, String Adunit_ID) {
        AdView adView = madView;
        LinearLayout adContainer = facebook_bannerAd_layput;
        adContainer.setVisibility(View.VISIBLE);


        AudienceNetworkAds.initialize(context);


        adView = new AdView(context, Adunit_ID, AdSize.BANNER_HEIGHT_50);
        adContainer.addView(adView);
        adView.loadAd();


        AdListener adListener = new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                Log.d("TAGA", "onErrror: " + adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {

            }

            @Override
            public void onAdClicked(Ad ad) {

            }

            @Override
            public void onLoggingImpression(Ad ad) {
                Log.d("TAGA", "Banner ad impression logged!");
            }
        };
        adView.loadAd(adView.buildLoadAdConfig().withAdListener(adListener).build());

    }


}
