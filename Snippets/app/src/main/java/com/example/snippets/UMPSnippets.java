package com.example.snippets;

import android.app.Activity;
import androidx.annotation.NonNull;
import com.google.android.gms.appset.AppSet;
import com.google.android.gms.appset.AppSetIdClient;
import com.google.android.ump.ConsentRequestParameters;

/** Snippets for UMP. */
public class UMPSnippets {
  private void syncConsentIdentifier(@NonNull Activity activity) {
    // [START sync_consent_identifier]
    // Example fetching App Set ID to identify the user across apps.
    AppSetIdClient client = AppSet.getClient(activity);
    client
        .getAppSetIdInfo()
        .addOnSuccessListener(
            info -> {
              String appSetId = info.getId();
              ConsentRequestParameters params =
                  new ConsentRequestParameters.Builder().setConsentSyncId(appSetId).build();
            });
    // [END sync_consent_identifier]
  }
}
