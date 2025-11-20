package com.example.snippets;

import android.view.View;
import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.FriendlyObstruction;
import com.google.ads.interactivemedia.v3.api.FriendlyObstructionPurpose;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;

/** Snippets for the "Enable open measurement" guide in IMA Android client-side documentation. */
public class OpenMeasurementSnippets {

  // [START register_friendly_obstructions]
  private void registerFriendlyObstructions(
      AdDisplayContainer adDisplayContainer, View transparentTapOverlay, View pauseButton) {
    ImaSdkFactory imaSdkFactory = ImaSdkFactory.getInstance();
    // Make sure to register all friendly obstructions before ad playback starts.
    FriendlyObstruction overlayObstruction =
        imaSdkFactory.createFriendlyObstruction(
            transparentTapOverlay,
            FriendlyObstructionPurpose.NOT_VISIBLE,
            "This overlay is transparent");
    FriendlyObstruction pauseButtonObstruction =
        imaSdkFactory.createFriendlyObstruction(
            pauseButton,
            FriendlyObstructionPurpose.VIDEO_CONTROLS,
            "This is the video player pause button");

    adDisplayContainer.registerFriendlyObstruction(overlayObstruction);
    adDisplayContainer.registerFriendlyObstruction(pauseButtonObstruction);
  }

  // [END register_friendly_obstructions]

  // [START unregister_friendly_obstructions]
  private void unregisterFriendlyObstructions(AdDisplayContainer adDisplayContainer) {
    adDisplayContainer.unregisterAllFriendlyObstructions();
  }
  // [END unregister_friendly_obstructions]
}
