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
import com.google.ads.interactivemedia.v3.api.CompanionAdSlot;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;
import java.util.ArrayList;

/** Ads logic for handling the IMA SDK integration code and events. */
public class VideoPlayerController {

  /** Log interface, so we can output the log commands to the UI or similar. */
  public interface Logger {
    void log(String logMessage);
  }

  // Container with references to video player and ad UI ViewGroup.
  private AdDisplayContainer mAdDisplayContainer;

  // The AdsLoader instance exposes the requestAds method.
  private AdsLoader mAdsLoader;

  // AdsManager exposes methods to control ad playback and listen to ad events.
  private AdsManager mAdsManager;

  // Factory class for creating SDK objects.
  private ImaSdkFactory mSdkFactory;

  // Ad-enabled video player.
  private VideoPlayerWithAdPlayback mVideoPlayerWithAdPlayback;

  // Button the user taps to begin video playback and ad request.
  private View mPlayButton;

  // VAST ad tag URL to use when requesting ads during video playback.
  private String mCurrentAdTagUrl;

  // URL of content video.
  private String mContentVideoUrl;

  // ViewGroup to render an associated companion ad into.
  private ViewGroup mCompanionViewGroup;

  // Tracks if the SDK is playing an ad, since the SDK might not necessarily use the video
  // player provided to play the video ad.
  private boolean mIsAdPlaying;

  // View that handles taps to toggle ad pause/resume during video playback.
  private View mPlayPauseToggle;

  // View that we can write log messages to, to display in the UI.
  private Logger mLog;

  private double mPlayAdsAfterTime = -1;

  private boolean mVideoStarted;

  // Inner class implementation of AdsLoader.AdsLoaderListener.
  private class AdsLoadedListener implements AdsLoader.AdsLoadedListener {
    /** An event raised when ads are successfully loaded from the ad server via AdsLoader. */
    @Override
    public void onAdsManagerLoaded(AdsManagerLoadedEvent adsManagerLoadedEvent) {
      // Ads were successfully loaded, so get the AdsManager instance. AdsManager has
      // events for ad playback and errors.
      mAdsManager = adsManagerLoadedEvent.getAdsManager();

      // Attach event and error event listeners.
      mAdsManager.addAdErrorListener(
          new AdErrorEvent.AdErrorListener() {
            /** An event raised when there is an error loading or playing ads. */
            @Override
            public void onAdError(AdErrorEvent adErrorEvent) {
              log("Ad Error: " + adErrorEvent.getError().getMessage());
              resumeContent();
            }
          });
      mAdsManager.addAdEventListener(
          new AdEvent.AdEventListener() {
            /** Responds to AdEvents. */
            @Override
            public void onAdEvent(AdEvent adEvent) {
              log("Event: " + adEvent.getType());

              // These are the suggested event types to handle. For full list of all ad
              // event types, see the documentation for AdEvent.AdEventType.
              switch (adEvent.getType()) {
                case LOADED:
                  // AdEventType.LOADED will be fired when ads are ready to be
                  // played. AdsManager.start() begins ad playback. This method is
                  // ignored for VMAP or ad rules playlists, as the SDK will
                  // automatically start executing the playlist.
                  mAdsManager.start();
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
                  mIsAdPlaying = false;
                  break;
                case RESUMED:
                  mIsAdPlaying = true;
                  break;
                case ALL_ADS_COMPLETED:
                  if (mAdsManager != null) {
                    mAdsManager.destroy();
                    mAdsManager = null;
                  }
                  break;
                default:
                  break;
              }
            }
          });
      AdsRenderingSettings adsRenderingSettings =
          ImaSdkFactory.getInstance().createAdsRenderingSettings();
      adsRenderingSettings.setPlayAdsAfterTime(mPlayAdsAfterTime);
      mAdsManager.init(adsRenderingSettings);
      seek(mPlayAdsAfterTime);
      mVideoStarted = true;
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
    mVideoPlayerWithAdPlayback = videoPlayerWithAdPlayback;
    mPlayButton = playButton;
    mPlayPauseToggle = playPauseToggle;
    mIsAdPlaying = false;
    mCompanionViewGroup = companionViewGroup;
    mLog = log;

    // Create an AdsLoader and optionally set the language.
    mSdkFactory = ImaSdkFactory.getInstance();
    ImaSdkSettings imaSdkSettings = mSdkFactory.createImaSdkSettings();
    imaSdkSettings.setLanguage(language);

    mAdDisplayContainer =
        ImaSdkFactory.createAdDisplayContainer(
            mVideoPlayerWithAdPlayback.getAdUiContainer(),
            mVideoPlayerWithAdPlayback.getVideoAdPlayer());
    mAdsLoader = mSdkFactory.createAdsLoader(context, imaSdkSettings, mAdDisplayContainer);

    mAdsLoader.addAdErrorListener(
        new AdErrorEvent.AdErrorListener() {
          /** An event raised when there is an error loading or playing ads. */
          @Override
          public void onAdError(AdErrorEvent adErrorEvent) {
            log("Ad Error: " + adErrorEvent.getError().getMessage());
            resumeContent();
          }
        });

    mAdsLoader.addAdsLoadedListener(new VideoPlayerController.AdsLoadedListener());

    mVideoPlayerWithAdPlayback.setOnContentCompleteListener(
        new VideoPlayerWithAdPlayback.OnContentCompleteListener() {
          /** Event raised by VideoPlayerWithAdPlayback when content video is complete. */
          @Override
          public void onContentComplete() {
            mAdsLoader.contentComplete();
          }
        });

    // When Play is clicked, request ads and hide the button.
    mPlayButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            requestAndPlayAds(-1);
          }
        });
  }

  private void log(String message) {
    if (mLog != null) {
      mLog.log(message + "\n");
    }
  }

  private void pauseContent() {
    mVideoPlayerWithAdPlayback.pauseContentForAdPlayback();
    mIsAdPlaying = true;
    setPlayPauseOnAdTouch();
  }

  private void resumeContent() {
    mVideoPlayerWithAdPlayback.resumeContentAfterAdPlayback();
    mIsAdPlaying = false;
    removePlayPauseOnAdTouch();
  }

  /** Set the ad tag URL the player should use to request ads when playing a content video. */
  public void setAdTagUrl(String adTagUrl) {
    mCurrentAdTagUrl = adTagUrl;
  }

  public String getAdTagUrl() {
    return mCurrentAdTagUrl;
  }

  /** Request and subsequently play video ads from the ad server. */
  public void requestAndPlayAds(double playAdsAfterTime) {
    if (mCurrentAdTagUrl == null || mCurrentAdTagUrl == "") {
      log("No VAST ad tag URL specified");
      resumeContent();
      return;
    }

    // Since we're switching to a new video, tell the SDK the previous video is finished.
    if (mAdsManager != null) {
      mAdsManager.destroy();
    }
    mAdsLoader.contentComplete();

    mPlayButton.setVisibility(View.GONE);

    // Create the ads request.
    AdsRequest request = mSdkFactory.createAdsRequest();
    request.setAdTagUrl(mCurrentAdTagUrl);
    request.setContentProgressProvider(mVideoPlayerWithAdPlayback.getContentProgressProvider());

    mPlayAdsAfterTime = playAdsAfterTime;

    // Request the ad. After the ad is loaded, onAdsManagerLoaded() will be called.
    mAdsLoader.requestAds(request);
  }

  /** Touch to toggle play/pause during ad play instead of seeking. */
  private void setPlayPauseOnAdTouch() {
    // Use AdsManager pause/resume methods instead of the video player pause/resume methods
    // in case the SDK is using a different, SDK-created video player for ad playback.
    mPlayPauseToggle.setOnTouchListener(
        new View.OnTouchListener() {
          public boolean onTouch(View view, MotionEvent event) {
            // If an ad is playing, touching it will toggle playback.
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
              if (mIsAdPlaying) {
                mAdsManager.pause();
              } else {
                mAdsManager.resume();
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
    mPlayPauseToggle.setOnTouchListener(null);
  }

  /**
   * Set metadata about the content video. In more complex implementations, this might more than
   * just a URL and could trigger additional decisions regarding ad tag selection.
   */
  public void setContentVideo(String videoPath) {
    mVideoPlayerWithAdPlayback.setContentVideoPath(videoPath);
    mContentVideoUrl = videoPath;
  }

  public String getContentVideoUrl() {
    return mContentVideoUrl;
  }

  /**
   * Save position of the video, whether content or ad. Can be called when the app is paused, for
   * example.
   */
  public void pause() {
    mVideoPlayerWithAdPlayback.savePosition();
    if (mAdsManager != null && mVideoPlayerWithAdPlayback.getIsAdDisplayed()) {
      mAdsManager.pause();
    } else {
      mVideoPlayerWithAdPlayback.pause();
    }
  }

  /**
   * Restore the previously saved progress location of the video. Can be called when the app is
   * resumed.
   */
  public void resume() {
    mVideoPlayerWithAdPlayback.restorePosition();
    if (mAdsManager != null && mVideoPlayerWithAdPlayback.getIsAdDisplayed()) {
      mAdsManager.resume();
    } else {
      mVideoPlayerWithAdPlayback.play();
    }
  }

  public void destroy() {
    if (mAdsManager != null) {
      mAdsManager.destroy();
      mAdsManager = null;
    }

    if (mAdDisplayContainer != null) {
      mAdDisplayContainer.destroy();
      mAdDisplayContainer = null;
    }
  }

  /** Seeks to time in content video in seconds. */
  public void seek(double time) {
    mVideoPlayerWithAdPlayback.seek((int) (time * 1000.0));
  }

  /** Returns the current time of the content video in seconds. */
  public double getCurrentContentTime() {
    return ((double) mVideoPlayerWithAdPlayback.getCurrentContentTime()) / 1000.0;
  }

  public boolean hasVideoStarted() {
    return mVideoStarted;
  }
}
