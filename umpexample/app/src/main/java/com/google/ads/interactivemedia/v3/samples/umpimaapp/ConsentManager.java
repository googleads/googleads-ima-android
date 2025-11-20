package com.google.ads.interactivemedia.v3.samples.umpimaapp;

import android.app.Activity;
import com.google.android.gms.appset.AppSet;
import com.google.android.gms.appset.AppSetIdClient;
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
  public ConsentManager(Activity activity) {
    this.activity = activity;
    this.consentInformation = UserMessagingPlatform.getConsentInformation(activity);
  }

  /** Returns true if the app has the user consent for showing ads. */
  public boolean canRequestAds() {
    return consentInformation.canRequestAds();
  }

  // [START is_privacy_options_required]
  /** Helper function to determine if a privacy options entry point is required. */
  public boolean isPrivacyOptionsRequired() {
    return consentInformation.getPrivacyOptionsRequirementStatus()
        == PrivacyOptionsRequirementStatus.REQUIRED;
  }

  // [END is_privacy_options_required]

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

    // Example fetching App Set ID to identify the user across apps.
    // Set ConsentRequestParameters for this sample app to download a consent request form.
    // If your app has different consent requirements, change the parameters to make appropriate
    // consent requests.
    // [START build_consent_request_parameters]]
    AppSetIdClient client = AppSet.getClient(activity);
    client
        .getAppSetIdInfo()
        .addOnSuccessListener(
            info -> {
              String appSetId = info.getId();

              ConsentRequestParameters consentRequestParameters =
                  new ConsentRequestParameters.Builder()
                      .setTagForUnderAgeOfConsent(false)
                      .setConsentDebugSettings(debugSettings)
                      .setConsentSyncId(appSetId)
                      .build();
              // [END build_consent_request_parameters]

              // [START request_consent_info_update]
              // Requesting an update to consent information should be called on every app launch.
              consentInformation.requestConsentInfoUpdate(
                  activity,
                  consentRequestParameters,
                  () -> // Called when consent information is successfully updated.
                      // [START_EXCLUDE silent]
                      loadAndShowConsentFormIfRequired(
                          activity, onConsentGatheringCompleteListener),
                  // [END_EXCLUDE]
                  // Called when there's an error updating consent information.
                  requestConsentError ->
                      // [START_EXCLUDE silent]
                      onConsentGatheringCompleteListener.consentGatheringComplete(
                          requestConsentError));
              // [END_EXCLUDE]
              // [END request_consent_info_update]
            });
  }

  private void loadAndShowConsentFormIfRequired(
      Activity activity, OnConsentGatheringCompleteListener onConsentGatheringCompleteListener) {
    // [START load_and_show_consent_form]
    UserMessagingPlatform.loadAndShowConsentFormIfRequired(
        activity,
        formError -> {
          // Consent gathering process is complete.
          // [START_EXCLUDE silent]
          onConsentGatheringCompleteListener.consentGatheringComplete(formError);
          // [END_EXCLUDE]
        });
    // [END load_and_show_consent_form]
  }

  /** Shows a form to app users for collecting their consent. */
  public void showPrivacyOptionsForm(
      Activity activity, OnConsentFormDismissedListener onConsentFormDismissedListener) {
    // [START present_privacy_options_form]
    UserMessagingPlatform.showPrivacyOptionsForm(activity, onConsentFormDismissedListener);
    // [END present_privacy_options_form]
  }
}
