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

/** Ads logic for handling the IMA SDK integration code and events. */
public class VideoPlayerController {

  /** Log interface, so we can output the log commands to the UI or similar. */
  public interface Logger {
    void log(String logMessage);
  }

  // Container with references to video player and ad UI ViewGroup.
  private AdDisplayContainer adDisplayContainer;

  // The AdsLoader instance exposes the requestAds method.
  private final AdsLoader adsLoader;

  // AdsManager exposes methods to control ad playback and listen to ad events.
  private AdsManager adsManager;

  // Factory class for creating SDK objects.
  private ImaSdkFactory sdkFactory;

  // Ad-enabled video player.
  private VideoPlayerWithAdPlayback videoPlayerWithAdPlayback;

  // Button the user taps to begin video playback and ad request.
  private View playButton;

  // VAST ad tag URL to use when requesting ads during video playback.
  private String currentAdTagUrl;

  // URL of content video.
  private String contentVideoUrl;

  // ViewGroup to render an associated companion ad into.
  private ViewGroup companionViewGroup;

  // Tracks if the SDK is playing an ad, since the SDK might not necessarily use the video
  // player provided to play the video ad.
  private boolean isAdPlaying;

  // View that handles taps to toggle ad pause/resume during video playback.
  private View playPauseToggle;

  // View that we can write log messages to, to display in the UI.
  private Logger log;

  private double playAdsAfterTime = -1;

  private boolean videoStarted;

  // Inner class implementation of AdsLoader.AdsLoaderListener.
  private class AdsLoadedListener implements AdsLoader.AdsLoadedListener {
    /** An event raised when ads are successfully loaded from the ad server via AdsLoader. */
    @Override
    public void onAdsManagerLoaded(AdsManagerLoadedEvent adsManagerLoadedEvent) {
      // Ads were successfully loaded, so get the AdsManager instance. AdsManager has
      // events for ad playback and errors.
      adsManager = adsManagerLoadedEvent.getAdsManager();

      // Attach event and error event listeners.
      adsManager.addAdErrorListener(
          new AdErrorEvent.AdErrorListener() {
            /** An event raised when there is an error loading or playing ads. */
            @Override
            public void onAdError(AdErrorEvent adErrorEvent) {
              log("Ad Error: " + adErrorEvent.getError().getMessage());
              resumeContent();
            }
          });
      adsManager.addAdEventListener(
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
                  adsManager.start();
                  break;
                case CONTENT_PAUSE_REQUESTED:
                  // AdEventType.CONTENT_PAUSE_REQUESTED is fired immediately before
                  // a video ad is played.
                  pauseContent();
                  break;
                case CONTENT_RESUME_REQUESTED:
                  // AdEventType.CONTENT_RESUME_REQUESTED is fired when the ad is
                  // completed and you should start playing your content.
                  resumeContent();
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
                  if (adsManager != null) {
                    adsManager.destroy();
                    adsManager = null;
                  }
                  adsLoader.release();
                  break;
                default:
                  break;
              }
            }
          });
      AdsRenderingSettings adsRenderingSettings =
          ImaSdkFactory.getInstance().createAdsRenderingSettings();
      adsRenderingSettings.setPlayAdsAfterTime(playAdsAfterTime);
      adsManager.init(adsRenderingSettings);
      seek(playAdsAfterTime);
      videoStarted = true;
    }
  }

  public VideoPlayerController(
      Context context,
      VideoPlayerWithAdPlayback videoPlayerWithAdPlayback,
      View playButton,
      View playPauseToggle,
      String language,
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
    ImaSdkSettings imaSdkSettings = sdkFactory.createImaSdkSettings();
    imaSdkSettings.setLanguage(language);

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
    playButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            requestAndPlayAds(-1);
          }
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

  /** Set the ad tag URL the player should use to request ads when playing a content video. */
  public void setAdTagUrl(String adTagUrl) {
    currentAdTagUrl = adTagUrl;
  }

  public String getAdTagUrl() {
    return currentAdTagUrl;
  }

  /** Request and subsequently play video ads from the ad server. */
  public void requestAndPlayAds(double playAdsAfterTime) {
    if (currentAdTagUrl == null || currentAdTagUrl == "") {
      log("No VAST ad tag URL specified");
      resumeContent();
      return;
    }

    // Since we're switching to a new video, tell the SDK the previous video is finished.
    if (adsManager != null) {
      adsManager.destroy();
    }

    playButton.setVisibility(View.GONE);

    // Create the ads request.
    AdsRequest request = sdkFactory.createAdsRequest();
    request.setAdTagUrl(currentAdTagUrl);
    request.setContentProgressProvider(videoPlayerWithAdPlayback.getContentProgressProvider());

    this.playAdsAfterTime = playAdsAfterTime;

    // Request the ad. After the ad is loaded, onAdsManagerLoaded() will be called.
    adsLoader.requestAds(request);
  }

  /** Touch to toggle play/pause during ad play instead of seeking. */
  private void setPlayPauseOnAdTouch() {
    // Use AdsManager pause/resume methods instead of the video player pause/resume methods
    // in case the SDK is using a different, SDK-created video player for ad playback.
    playPauseToggle.setOnTouchListener(
        new View.OnTouchListener() {
          public boolean onTouch(View view, MotionEvent event) {
            // If an ad is playing, touching it will toggle playback.
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
              if (isAdPlaying) {
                adsManager.pause();
              } else {
                adsManager.resume();
              }
              return true;
            } else {
              return false;
            }
          }
        });
  }

  /** Remove the play/pause on touch behavior. */
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
    if (adsManager != null && videoPlayerWithAdPlayback.getIsAdDisplayed()) {
      adsManager.pause();
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
    if (adsManager != null && videoPlayerWithAdPlayback.getIsAdDisplayed()) {
      adsManager.resume();
    } else {
      videoPlayerWithAdPlayback.play();
    }
  }

  public void destroy() {
    if (adsManager != null) {
      adsManager.destroy();
      adsManager = null;
    }
  }

  /** Seeks to time in content video in seconds. */
  public void seek(double time) {
    videoPlayerWithAdPlayback.seek((int) (time * 1000.0));
  }

  /** Returns the current time of the content video in seconds. */
  public double getCurrentContentTime() {
    return ((double) videoPlayerWithAdPlayback.getCurrentContentTime()) / 1000.0;
  }

  public boolean hasVideoStarted() {
    return videoStarted;
  }
}
