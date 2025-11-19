package com.example.snippets;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageButton;
import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.FriendlyObstruction;
import com.google.ads.interactivemedia.v3.api.FriendlyObstructionPurpose;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;

/** Snippets for the "Enable open measurement" guide in IMA Android client-side documentation. */
public class OpenMeasurementSnippets extends Activity {
  private AdDisplayContainer adDisplayContainer;
  private ImaSdkFactory imaSdkFactory;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.open_measurement_snippets);
  }

  private void openMeasurementSnippet() {
    // [START open_measurement_snippet]
    ViewGroup myTransparentTapOverlay = (ViewGroup) findViewById(R.id.transparentOverlay);
    ImageButton myPauseButton = (ImageButton) findViewById(R.id.pauseButton);
    // Substitute "myTransparentTapOverlay" and "myPauseButton" with the
    // elements you want to register as video controls overlays.
    // Make sure to register before ad playback starts.
    FriendlyObstruction overlayObstruction =
        imaSdkFactory.createFriendlyObstruction(
            myTransparentTapOverlay,
            FriendlyObstructionPurpose.NOT_VISIBLE,
            "This overlay is transparent");
    FriendlyObstruction pauseButtonObstruction =
        imaSdkFactory.createFriendlyObstruction(
            myPauseButton,
            FriendlyObstructionPurpose.VIDEO_CONTROLS,
            "This is the video player pause button");

    adDisplayContainer.registerFriendlyObstruction(overlayObstruction);
    adDisplayContainer.registerFriendlyObstruction(pauseButtonObstruction);
    // [END open_measurement_snippet]
  }

  private void unregisterFriendlyObstructionsSnippet() {
    // [START unregister_friendly_obstructions_snippet]
    adDisplayContainer.unregisterAllFriendlyObstructions();
    // [END unregister_friendly_obstructions_snippet]
  }
}
