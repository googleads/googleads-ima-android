package com.example.snippets;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.media3.exoplayer.ima.ImaAdsLoader;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;

/** Snippets for the "Localize the ad UI" guide in IMA Android client-side documentation. */
@SuppressLint("UnsafeOptInUsageError")
public class LocalizeAdUiSnippets {

  // [START set_ad_ui_localization]
  private void setAdUiLocalization(String languageCode) {
    ImaSdkSettings imaSdkSettings = ImaSdkFactory.getInstance().createImaSdkSettings();
    imaSdkSettings.setLanguage(languageCode);
  }

  // [END set_ad_ui_localization]

  // [START set_ad_ui_localization_exoplayer]
  private void setAdUiLocalizationExoplayer(String languageCode, Context context) {
    ImaSdkSettings imaSdkSettings = ImaSdkFactory.getInstance().createImaSdkSettings();
    imaSdkSettings.setLanguage(languageCode);
    ImaAdsLoader.Builder builder =
        new ImaAdsLoader.Builder(context).setImaSdkSettings(imaSdkSettings);
  }
  // [END set_ad_ui_localization_exoplayer]
}
