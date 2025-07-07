// Copyright 2022 Google LLC

package com.google.ads.interactivemedia.v3.samples.videoplayerapp;

import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.AdsRenderingSettings;
import com.google.ads.interactivemedia.v3.api.AdsRequest;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import java.util.Arrays;

/** Main activity. */
public class MyActivity extends AppCompatActivity {

  private static final String LOGTAG = "IMABasicSample";
  private static final String SAMPLE_VIDEO_URL =
      "https://storage.googleapis.com/gvabox/media/samples/stock.mp4";

  /**
   * IMA sample tag for a single skippable inline video ad. See more IMA sample tags at
   * https://developers.google.com/interactive-media-ads/docs/sdks/html5/client-side/tags
   */
  private static final String SAMPLE_VAST_TAG_URL =
      "https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/"
          + "single_preroll_skippable&sz=640x480&ciu_szs=300x250%2C728x90&gdfp_req=1&output=vast"
          + "&unviewed_position_start=1&env=vp&correlator=";

  // Factory class for creating SDK objects.
  private ImaSdkFactory sdkFactory;

  // The AdsLoader instance exposes the requestAds method.
  private AdsLoader adsLoader;

  // AdsManager exposes methods to control ad playback and listen to ad events.
  private AdsManager adsManager;

  // The saved content position, used to resumed content following an ad break.
  private int savedPosition = 0;

  // This sample uses a VideoView for content and ad playback. For production
  // apps, Android's Exoplayer offers a more fully featured player compared to
  // the VideoView.
  private VideoView videoPlayer;
  private MediaController mediaController;
  private VideoAdPlayerAdapter videoAdPlayerAdapter;
  private ImaSdkSettings imaSdkSettings;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_my);

    // Initialize the IMA SDK as early as possible when the app starts. If your app already
    // overrides Application.onCreate(), call this method inside the onCreate() method.
    // https://developer.android.com/topic/performance/vitals/launch-time#app-creation
    sdkFactory = ImaSdkFactory.getInstance();
    sdkFactory.initialize(this, getImaSdkSettings());

    // Create the UI for controlling the video view.
    mediaController = new MediaController(this);
    videoPlayer = findViewById(R.id.videoView);
    mediaController.setAnchorView(videoPlayer);
    videoPlayer.setMediaController(mediaController);

    // Create an ad display container that uses a ViewGroup to listen to taps.
    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    videoAdPlayerAdapter = new VideoAdPlayerAdapter(videoPlayer, audioManager);

    AdDisplayContainer adDisplayContainer =
        ImaSdkFactory.createAdDisplayContainer(
            findViewById(R.id.videoPlayerContainer), videoAdPlayerAdapter);

    // Create an AdsLoader.
    adsLoader = sdkFactory.createAdsLoader(this, getImaSdkSettings(), adDisplayContainer);

    // Add listeners for when ads are loaded and for errors.
    adsLoader.addAdErrorListener(
        new AdErrorEvent.AdErrorListener() {
          /** An event raised when there is an error loading or playing ads. */
          @Override
          public void onAdError(AdErrorEvent adErrorEvent) {
            Log.i(LOGTAG, "Ad Error: " + adErrorEvent.getError().getMessage());
            resumeContent();
          }
        });
    adsLoader.addAdsLoadedListener(
        adsManagerLoadedEvent -> {
          // Ads were successfully loaded, so get the AdsManager instance. AdsManager has
          // events for ad playback and errors.
          adsManager = adsManagerLoadedEvent.getAdsManager();

          // Attach event and error event listeners.
          adsManager.addAdErrorListener(
              new AdErrorEvent.AdErrorListener() {
                /** An event raised when there is an error loading or playing ads. */
                @Override
                public void onAdError(AdErrorEvent adErrorEvent) {
                  Log.e(LOGTAG, "Ad Error: " + adErrorEvent.getError().getMessage());
                  String universalAdIds =
                      Arrays.toString(adsManager.getCurrentAd().getUniversalAdIds());
                  Log.i(
                      LOGTAG,
                      "Discarding the current ad break with universal "
                          + "ad Ids: "
                          + universalAdIds);
                  adsManager.discardAdBreak();
                }
              });
          adsManager.addAdEventListener(
              new AdEvent.AdEventListener() {
                /** Responds to AdEvents. */
                @Override
                public void onAdEvent(AdEvent adEvent) {
                  if (adEvent.getType() != AdEvent.AdEventType.AD_PROGRESS) {
                    Log.i(LOGTAG, "Event: " + adEvent.getType());
                  }
                  // These are the suggested event types to handle. For full list of
                  // all ad event types, see AdEvent.AdEventType documentation.
                  switch (adEvent.getType()) {
                    case LOADED:
                      // AdEventType.LOADED is fired when ads are ready to play.

                      // This sample app uses the sample tag
                      // single_preroll_skippable_ad_tag_url that requires calling
                      // AdsManager.start() to start ad playback.
                      // If you use a different ad tag URL that returns a VMAP or
                      // an ad rules playlist, the adsManager.init() function will
                      // trigger ad playback automatically and the IMA SDK will
                      // ignore the adsManager.start().
                      // It is safe to always call adsManager.start() in the
                      // LOADED event.
                      adsManager.start();
                      break;
                    case CONTENT_PAUSE_REQUESTED:
                      // AdEventType.CONTENT_PAUSE_REQUESTED is fired when you
                      // should pause your content and start playing an ad.
                      pauseContentForAds();
                      break;
                    case CONTENT_RESUME_REQUESTED:
                      // AdEventType.CONTENT_RESUME_REQUESTED is fired when the ad
                      // you should play your content.
                      resumeContent();
                      break;
                    case ALL_ADS_COMPLETED:
                      // Calling adsManager.destroy() triggers the function
                      // VideoAdPlayer.release().
                      adsManager.destroy();
                      adsManager = null;
                      break;
                    case CLICKED:
                      // When the user clicks on the Learn More button, the IMA SDK fires
                      // this event, pauses the ad, and opens the ad's click-through URL.
                      // When the user returns to the app, the IMA SDK calls the
                      // VideoAdPlayer.playAd() function automatically.
                      break;
                    default:
                      break;
                  }
                }
              });
          AdsRenderingSettings adsRenderingSettings =
              ImaSdkFactory.getInstance().createAdsRenderingSettings();
          // Add any ads rendering settings here.
          // This init() only loads the UI rendering settings locally.
          adsManager.init(adsRenderingSettings);
        });

    // When the play button is clicked, request ads and hide the button.
    View playButton = findViewById(R.id.playButton);
    playButton.setOnClickListener(
        view -> {
          videoPlayer.setVideoPath(SAMPLE_VIDEO_URL);
          requestAds(SAMPLE_VAST_TAG_URL);
          view.setVisibility(View.GONE);
        });
    updateVideoDescriptionVisibility();
  }

  @Override
  public void onConfigurationChanged(Configuration configuration) {
    super.onConfigurationChanged(configuration);
    // Hide the extra content when in landscape so the video is as large as possible.
    updateVideoDescriptionVisibility();
  }

  private void updateVideoDescriptionVisibility() {
    int orientation = getResources().getConfiguration().orientation;
    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
      findViewById(R.id.videoDescription).setVisibility(View.GONE);
    } else {
      findViewById(R.id.videoDescription).setVisibility(View.VISIBLE);
    }
  }

  private void pauseContentForAds() {
    Log.i(LOGTAG, "pauseContentForAds");
    savedPosition = videoPlayer.getCurrentPosition();
    videoPlayer.stopPlayback();
    // Hide the buttons and seek bar controlling the video view.
    videoPlayer.setMediaController(null);
  }

  private void resumeContent() {
    Log.i(LOGTAG, "resumeContent");

    // Show the buttons and seek bar controlling the video view.
    videoPlayer.setVideoPath(SAMPLE_VIDEO_URL);
    videoPlayer.setMediaController(mediaController);
    videoPlayer.setOnPreparedListener(
        mediaPlayer -> {
          if (savedPosition > 0) {
            mediaPlayer.seekTo(savedPosition);
          }
          mediaPlayer.start();
        });
    videoPlayer.setOnCompletionListener(
        mediaPlayer -> videoAdPlayerAdapter.notifyImaOnContentCompleted());
  }

  private void requestAds(String adTagUrl) {
    // Create the ads request.
    AdsRequest request = sdkFactory.createAdsRequest();
    request.setAdTagUrl(adTagUrl);
    request.setContentProgressProvider(
        () -> {
          if (videoPlayer.getDuration() <= 0) {
            return VideoProgressUpdate.VIDEO_TIME_NOT_READY;
          }
          return new VideoProgressUpdate(
              videoPlayer.getCurrentPosition(), videoPlayer.getDuration());
        });

    // Request the ad. After the ad is loaded, onAdsManagerLoaded() will be called.
    adsLoader.requestAds(request);
  }

  private ImaSdkSettings getImaSdkSettings() {
    if (imaSdkSettings == null) {
      imaSdkSettings = ImaSdkFactory.getInstance().createImaSdkSettings();
      // Set any IMA SDK settings here.
    }
    return imaSdkSettings;
  }
}
