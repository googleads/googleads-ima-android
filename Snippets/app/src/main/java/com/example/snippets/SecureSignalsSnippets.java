package com.example.snippets;

import com.google.ads.interactivemedia.v3.api.AdsRequest;
import com.google.ads.interactivemedia.v3.api.signals.SecureSignals;

/**
 * Snippets for the "Get started with secure signals" guide in IMA Android client-side
 * documentation.
 */
public class SecureSignalsSnippets {

  // [START set_secure_signals]
  private void setSecureSignals(AdsRequest adsRequest, String secureSignalsString) {
    SecureSignals signal = SecureSignals.create(secureSignalsString);
    adsRequest.setSecureSignals(signal);
  }

  // [END set_secure_signals]
}
