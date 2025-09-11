// Copyright 2014 Google Inc. All Rights Reserved.

package com.google.ads.interactivemedia.v3.samples.videoplayerapp;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.google.ads.interactivemedia.v3.api.AdsRenderingSettings;
import com.google.ads.interactivemedia.v3.api.AdsRequest;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;

import java.util.ArrayList;
import java.util.List;

/** Ads logic for handling the IMA SDK integration code and events. */
public class VideoPlayerController {

  /** Log interface, so we can output the log commands to the UI or similar. */
  public interface Logger {
    void log(String logMessage);
  }

  // Container with references to video player and ad UI ViewGroup.
  private final AdDisplayContainer adDisplayContainer;

  // The AdsLoader instance exposes the requestAds method.
  private final AdsLoader adsLoader;

  private final List<AdItem> adsToPlay = new ArrayList<>();

  // Factory class for creating SDK objects.
  private final ImaSdkFactory sdkFactory;

  // Ad-enabled video player.
  private final VideoPlayerWithAdPlayback videoPlayerWithAdPlayback;

  // Button the user taps to begin video playback and ad request.
  private final View playButton;

  // URL of content video.
  private String contentVideoUrl;

  // ViewGroup to render an associated companion ad into.
  private final ViewGroup companionViewGroup;

  // Tracks if the SDK is playing an ad, since the SDK might not necessarily use the video
  // player provided to play the video ad.
  private boolean isAdPlaying;

  // View that handles taps to toggle ad pause/resume during video playback.
  private final View playPauseToggle;

  // View that we can write log messages to, to display in the UI.
  private final Logger log;

  private boolean videoStarted;

  // Inner class implementation of AdsLoader.AdsLoaderListener.
  private class AdsLoadedListener implements AdsLoader.AdsLoadedListener {
    /** An event raised when ads are successfully loaded from the ad server through AdsLoader. */
    @Override
    public void onAdsManagerLoaded(AdsManagerLoadedEvent adsManagerLoadedEvent) {
      // Ads were successfully loaded, so get the AdsManager instance. AdsManager has
      // events for ad playback and errors.
      AdItem adItem = (AdItem) adsManagerLoadedEvent.getUserRequestContext();
      final AdsManager[] adsManager = {adsManagerLoadedEvent.getAdsManager()};
      adItem.adsManager = adsManager[0];

      // Attach event and error event listeners.
      adsManager[0].addAdErrorListener(
          new AdErrorEvent.AdErrorListener() {
            /** An event raised when there is an error loading or playing ads. */
            @Override
            public void onAdError(AdErrorEvent adErrorEvent) {
              log("Ad Error: " + adErrorEvent.getError().getMessage());
              resumeContent();
            }
          });
      adsManager[0].addAdEventListener(
          new AdEvent.AdEventListener() {
            /** Responds to AdEvents. */
            @Override
            public void onAdEvent(AdEvent adEvent) {
              if (adEvent.getType() != AdEvent.AdEventType.AD_PROGRESS) {
                log("Event: " + adEvent.getType());
              }

              // These are the suggested event types to handle. For full list of all ad
              // event types, see the documentation for AdEvent.AdEventType.
              switch (adEvent.getType()) {
                case LOADED:
                  // AdEventType.LOADED will be fired when ads are ready to be
                  // played. AdsManager.start() begins ad playback. This method is
                  // ignored for VMAP or ad rules playlists, as the SDK will
                  // automatically start executing the playlist.
                  // only start if not already playing ad
                  if (adsToPlay.get(0).adsManager == adsManager[0]) {
                    log("Starting ad");
                    adsManager[0].start();
                  } else {
                    log("Not starting ad");
                  }
                  break;
                case CONTENT_PAUSE_REQUESTED:
                  // AdEventType.CONTENT_PAUSE_REQUESTED is fired immediately before
                  // a video ad is played.
                  pauseContent();
                  break;
                case CONTENT_RESUME_REQUESTED:
                  // AdEventType.CONTENT_RESUME_REQUESTED is fired when the ad is
                  // completed and you should start playing your content.
                  adsToPlay.remove(0);
                  AdItem currentAdItem = currentAdItem();
                  if (currentAdItem != null) {
                    log("Playing next ad");
                    currentAdItem.adsManager.start();
                  } else {
                    resumeContent();
                  }
                  break;
                case PAUSED:
                  isAdPlaying = false;
                  videoPlayerWithAdPlayback.enableControls();
                  break;
                case RESUMED:
                  isAdPlaying = true;
                  videoPlayerWithAdPlayback.disableControls();
                  break;
                case ALL_ADS_COMPLETED:
                  if (adsManager[0] != null) {
                    adsManager[0].destroy();
                    adsManager[0] = null;
                  }
                  break;
                case AD_BREAK_FETCH_ERROR:
                  log("Ad Fetch Error. Resuming content.");
                  // A CONTENT_RESUME_REQUESTED event should follow to trigger content playback.
                  break;
                default:
                  break;
              }
            }
          });
      AdsRenderingSettings adsRenderingSettings =
          ImaSdkFactory.getInstance().createAdsRenderingSettings();
      adsManager[0].init(adsRenderingSettings);
      videoStarted = true;
    }
  }

  public VideoPlayerController(
      Context context,
      VideoPlayerWithAdPlayback videoPlayerWithAdPlayback,
      View playButton,
      View playPauseToggle,
      ImaSdkSettings imaSdkSettings,
      ViewGroup companionViewGroup,
      Logger log) {
    this.videoPlayerWithAdPlayback = videoPlayerWithAdPlayback;
    this.playButton = playButton;
    this.playPauseToggle = playPauseToggle;
    isAdPlaying = false;
    this.companionViewGroup = companionViewGroup;
    this.log = log;

    // Create an AdsLoader and optionally set the language.
    sdkFactory = ImaSdkFactory.getInstance();

    adDisplayContainer =
        ImaSdkFactory.createAdDisplayContainer(
            videoPlayerWithAdPlayback.getAdUiContainer(),
            videoPlayerWithAdPlayback.getVideoAdPlayer());
    adsLoader = sdkFactory.createAdsLoader(context, imaSdkSettings, adDisplayContainer);

    adsLoader.addAdErrorListener(
        new AdErrorEvent.AdErrorListener() {
          /** An event raised when there is an error loading or playing ads. */
          @Override
          public void onAdError(AdErrorEvent adErrorEvent) {
            log("Ad Error: " + adErrorEvent.getError().getMessage());
            resumeContent();
          }
        });

    adsLoader.addAdsLoadedListener(new VideoPlayerController.AdsLoadedListener());

    // When Play is clicked, request ads and hide the button.
    playButton.setOnClickListener(view -> {
      // add two ads to the queue that both will be played in sequence
      requestAndPlayAds(VideoMetadata.PRE_ROLL_NO_SKIP.adTagUrl);
      requestAndPlayAds(VideoMetadata.PRE_ROLL_SKIP.adTagUrl);
    });
  }

  private void log(String message) {
    if (log != null) {
      log.log(message + "\n");
    }
  }

  private void pauseContent() {
    videoPlayerWithAdPlayback.pauseContentForAdPlayback();
    isAdPlaying = true;
    setPlayPauseOnAdTouch();
  }

  private void resumeContent() {
    videoPlayerWithAdPlayback.resumeContentAfterAdPlayback();
    isAdPlaying = false;
    removePlayPauseOnAdTouch();
  }

  /**
   * Request and subsequently play video ads from the ad server.
   */
  public void requestAndPlayAds(String adTagUrl) {
    if (adTagUrl == null || adTagUrl.isEmpty()) {
      log("No VAST ad tag URL specified");
      resumeContent();
      return;
    }


    playButton.setVisibility(View.GONE);

    AdItem adItem = new AdItem(adTagUrl);
    adsToPlay.add(adItem);

    // Create the ads request.
    AdsRequest request = sdkFactory.createAdsRequest();
    request.setAdTagUrl(adTagUrl);
    request.setUserRequestContext(adItem);
    request.setContentProgressProvider(videoPlayerWithAdPlayback.getContentProgressProvider());

    // Request the ad. After the ad is loaded, onAdsManagerLoaded() will be called.
    adsLoader.requestAds(request);
  }

  /**
   * Touch to toggle play/pause during ad play instead of seeking.
   */
  private void setPlayPauseOnAdTouch() {
    // Use AdsManager pause/resume methods instead of the video player pause/resume methods
    // in case the SDK is using a different, SDK-created video player for ad playback.
    playPauseToggle.setOnTouchListener(
        (view, event) -> {
          view.performClick();
          // If an ad is playing, touching it will toggle playback.
          if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (isAdPlaying) {
              currentAdItem().adsManager.pause();
            } else {
              currentAdItem().adsManager.resume();
            }
            return true;
          } else {
            return false;
          }
        });
  }

  private AdItem currentAdItem() {
    if (adsToPlay.isEmpty()) return null;
    return adsToPlay.get(0);
  }

  /**
   * Remove the play/pause on touch behavior.
   */
  private void removePlayPauseOnAdTouch() {
    playPauseToggle.setOnTouchListener(null);
  }

  /**
   * Set metadata about the content video. In more complex implementations, this might more than
   * just a URL and could trigger additional decisions regarding ad tag selection.
   */
  public void setContentVideo(String videoPath) {
    videoPlayerWithAdPlayback.setContentVideoPath(videoPath);
    contentVideoUrl = videoPath;
  }

  public String getContentVideoUrl() {
    return contentVideoUrl;
  }

  /**
   * Save position of the video, whether content or ad. Can be called when the app is paused, for
   * example.
   */
  public void pause() {
    videoPlayerWithAdPlayback.savePosition();
    AdItem adItem = currentAdItem();
    if (adItem != null && adItem.adsManager != null && videoPlayerWithAdPlayback.getIsAdDisplayed()) {
      adItem.adsManager.pause();
    } else {
      videoPlayerWithAdPlayback.pause();
    }
  }

  /**
   * Restore the previously saved progress location of the video. Can be called when the app is
   * resumed.
   */
  public void resume() {
    videoPlayerWithAdPlayback.restorePosition();
    if (currentAdItem() != null && videoPlayerWithAdPlayback.getIsAdDisplayed()) {
      currentAdItem().adsManager.resume();
    } else {
      videoPlayerWithAdPlayback.play();
    }
  }

  public void destroy() {
    for (AdItem adItem : adsToPlay) {
      if (adItem.adsManager != null) {
        adItem.adsManager.destroy();
      }
    }
    adsToPlay.clear();
  }

  /**
   * Seeks to time in content video in seconds.
   */
  public void seek(double time) {
    videoPlayerWithAdPlayback.seek((int) (time * 1000.0));
  }

  /**
   * Returns the current time of the content video in seconds.
   */
  public double getCurrentContentTime() {
    return ((double) videoPlayerWithAdPlayback.getCurrentContentTime()) / 1000.0;
  }

  public boolean hasVideoStarted() {
    return videoStarted;
  }

  private static class AdItem {
    String tag;
    AdsManager adsManager;


    public AdItem(String tag) {
      this.tag = tag;
    }
  }
}
