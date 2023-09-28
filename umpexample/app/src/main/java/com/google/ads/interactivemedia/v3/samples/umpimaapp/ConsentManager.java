package com.google.ads.interactivemedia.v3.samples.umpimaapp;

import android.app.Activity;
import androidx.annotation.NonNull;
import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentForm.OnConsentFormDismissedListener;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentInformation.PrivacyOptionsRequirementStatus;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;

/**
 * Manages app user's consent using the Google User Messaging Platform SDK or a Consent Management
 * Platform (CMP) that has been certified by Google . See
 * https://support.google.com/admanager/answer/10113209 for more information about GDPR messages for
 * apps. See also https://support.google.com/adsense/answer/13554116 for more information about
 * Google consent management requirements for serving ads in the EEA and UK.
 */
public class ConsentManager {
  private final Activity activity;
  private final ConsentInformation consentInformation;

  /** Interface definition for a callback to be invoked when consent gathering is complete. */
  public interface OnConsentGatheringCompleteListener {
    void consentGatheringComplete(FormError error);
  }

  /** Constructor */
  public ConsentManager(@NonNull Activity activity) {
    this.activity = activity;
    this.consentInformation = UserMessagingPlatform.getConsentInformation(activity);
  }

  /** Returns true if the app has the user consent for showing ads. */
  public boolean canRequestAds() {
    return consentInformation.canRequestAds();
  }

  /** Helper function to determine if GDPR consent messages are required. */
  public boolean areGDPRConsentMessagesRequired() {
    return consentInformation.getPrivacyOptionsRequirementStatus()
        == PrivacyOptionsRequirementStatus.REQUIRED;
  }

  /** Load remote updates of consent messages and gather previously cached user consent. */
  public void gatherConsent(OnConsentGatheringCompleteListener onConsentGatheringCompleteListener) {
    // For testing purposes, you can force a DebugGeography of EEA or NOT_EEA.
    ConsentDebugSettings debugSettings =
        new ConsentDebugSettings.Builder(activity)
            // .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)

            // Check your logcat output for the hashed device ID, such as
            // "Use new ConsentDebugSettings.Builder().addTestDeviceHashedId("ABCDEF012345")" to use
            // the debug functionality.
            .addTestDeviceHashedId("TEST-DEVICE-HASHED-ID")
            .build();

    // Set up parameters for this sample app to download a consent request form. If your app has
    // different consent requirements, change this parameter to make appropriate consent requests.
    ConsentRequestParameters params =
        new ConsentRequestParameters.Builder()
            // Set the following tag to false to indicate that your app users are not under the
            // age of consent.
            .setTagForUnderAgeOfConsent(false)
            .setConsentDebugSettings(debugSettings)
            .build();

    // Requesting an update to consent information should be called on every app launch.
    consentInformation.requestConsentInfoUpdate(
        activity,
        params,
        () ->
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                activity,
                formError -> {
                  // Consent has been gathered.
                  onConsentGatheringCompleteListener.consentGatheringComplete(formError);
                }),
        requestConsentError ->
            onConsentGatheringCompleteListener.consentGatheringComplete(requestConsentError));
  }

  /** Shows a form to app users for collecting their consent. */
  public void showPrivacyOptionsForm(
      Activity activity, OnConsentFormDismissedListener onConsentFormDismissedListener) {
    UserMessagingPlatform.showPrivacyOptionsForm(activity, onConsentFormDismissedListener);
  }
}
