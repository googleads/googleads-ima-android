/*
 * Copyright (C) 2025 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ads.interactivemedia.v3.samples.snippets;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.FriendlyObstruction;
import com.google.ads.interactivemedia.v3.api.FriendlyObstructionPurpose;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;

/** Main Activity that contains code snippets for IMA Android client-side documentation. */
public class MyActivity extends AppCompatActivity {

  private AdDisplayContainer adDisplayContainer;
  private ImaSdkFactory imaSdkFactory;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_my);

    // Create the IMA class and interface objects used in the snippets.
    imaSdkFactory = new ImaSdkFactory.getInstance();
    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

    adDisplayContainer =
        ImaSdkFactory.createAdDisplayContainer(
            findViewById(R.id.videoPlayerContainer),
            new VideoAdPlayerAdapter(findViewById(R.id.videoView), audioManager));
  }

  private void openMeasurementSnippet() {
    // [START open_measurement_snippet]
    ViewGroup transparentTapOverlay = (ViewGroup) findViewById(R.id.transparentOverlay);
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
