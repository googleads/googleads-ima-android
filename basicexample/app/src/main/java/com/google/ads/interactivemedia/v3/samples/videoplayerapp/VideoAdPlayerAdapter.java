// Copyright 2022 Google LLC

package com.google.ads.interactivemedia.v3.samples.videoplayerapp;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.widget.VideoView;
import com.google.ads.interactivemedia.v3.api.AdPodInfo;
import com.google.ads.interactivemedia.v3.api.player.AdMediaInfo;
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/** Example implementation of IMA's VideoAdPlayer interface. */
public class VideoAdPlayerAdapter implements VideoAdPlayer {

  private static final String LOGTAG = "IMABasicSample";
  private static final long POLLING_TIME_MS = 250;
  private static final long INITIAL_DELAY_MS = 250;
  private final VideoView videoPlayer;
  private final AudioManager audioManager;
  private final List<VideoAdPlayerCallback> videoAdPlayerCallbacks = new ArrayList<>();
  private Timer timer;
  private int adDuration;

  // The saved ad position, used to resumed ad playback following an ad click-through.
  private int savedAdPosition;
  private AdMediaInfo loadedAdMediaInfo;

  public VideoAdPlayerAdapter(VideoView videoPlayer, AudioManager audioManager) {
    this.videoPlayer = videoPlayer;
    this.videoPlayer.setOnCompletionListener(
        (MediaPlayer mediaPlayer) -> notifyImaOnContentCompleted());
    this.audioManager = audioManager;
  }

  @Override
  public void addCallback(VideoAdPlayerCallback videoAdPlayerCallback) {
    videoAdPlayerCallbacks.add(videoAdPlayerCallback);
  }

  @Override
  public void loadAd(AdMediaInfo adMediaInfo, AdPodInfo adPodInfo) {
    // This simple ad loading logic works because preloading is disabled. To support
    // preloading ads your app must maintain state for the currently playing ad
    // while handling upcoming ad downloading and buffering at the same time.
    // See the IMA Android preloading guide for more info:
    // https://developers.google.com/interactive-media-ads/docs/sdks/android/client-side/preload
    loadedAdMediaInfo = adMediaInfo;
  }

  @Override
  public void pauseAd(AdMediaInfo adMediaInfo) {
    Log.i(LOGTAG, "pauseAd");
    savedAdPosition = videoPlayer.getCurrentPosition();
    stopAdTracking();
  }

  @Override
  public void playAd(AdMediaInfo adMediaInfo) {
    videoPlayer.setVideoURI(Uri.parse(adMediaInfo.getUrl()));

    videoPlayer.setOnPreparedListener(
        mediaPlayer -> {
          adDuration = mediaPlayer.getDuration();
          if (savedAdPosition > 0) {
            mediaPlayer.seekTo(savedAdPosition);
          }
          mediaPlayer.start();
          startAdTracking();
        });
    videoPlayer.setOnErrorListener(
        (mediaPlayer, errorType, extra) -> notifyImaSdkAboutAdError(errorType));
    videoPlayer.setOnCompletionListener(
        mediaPlayer -> {
          savedAdPosition = 0;
          notifyImaSdkAboutAdEnded();
        });
  }

  @Override
  public void release() {
    // any clean up that needs to be done.
  }

  @Override
  public void removeCallback(VideoAdPlayerCallback videoAdPlayerCallback) {
    videoAdPlayerCallbacks.remove(videoAdPlayerCallback);
  }

  @Override
  public void stopAd(AdMediaInfo adMediaInfo) {
    Log.i(LOGTAG, "stopAd");
    stopAdTracking();
  }

  /** Returns current volume as a percent of max volume. */
  @Override
  public int getVolume() {
    return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
  }

  private void startAdTracking() {
    Log.i(LOGTAG, "startAdTracking");
    if (timer != null) {
      return;
    }
    timer = new Timer();
    TimerTask updateTimerTask =
        new TimerTask() {
          @Override
          public void run() {
            VideoProgressUpdate progressUpdate = getAdProgress();
            notifyImaSdkAboutAdProgress(progressUpdate);
          }
        };
    timer.schedule(updateTimerTask, POLLING_TIME_MS, INITIAL_DELAY_MS);
  }

  private void notifyImaSdkAboutAdEnded() {
    Log.i(LOGTAG, "notifyImaSdkAboutAdEnded");
    savedAdPosition = 0;
    for (VideoAdPlayer.VideoAdPlayerCallback callback : videoAdPlayerCallbacks) {
      callback.onEnded(loadedAdMediaInfo);
    }
  }

  private void notifyImaSdkAboutAdProgress(VideoProgressUpdate adProgress) {
    for (VideoAdPlayer.VideoAdPlayerCallback callback : videoAdPlayerCallbacks) {
      callback.onAdProgress(loadedAdMediaInfo, adProgress);
    }
  }

  /**
   * @param errorType Media player's error type as defined at
   *     https://cs.android.com/android/platform/superproject/+/master:frameworks/base/media/java/android/media/MediaPlayer.java;l=4335
   * @return True to stop the current ad playback.
   */
  private boolean notifyImaSdkAboutAdError(int errorType) {
    Log.i(LOGTAG, "notifyImaSdkAboutAdError");

    switch (errorType) {
      case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
        Log.e(LOGTAG, "notifyImaSdkAboutAdError: MEDIA_ERROR_UNSUPPORTED");
        break;
      case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
        Log.e(LOGTAG, "notifyImaSdkAboutAdError: MEDIA_ERROR_TIMED_OUT");
        break;
      default:
        break;
    }
    for (VideoAdPlayer.VideoAdPlayerCallback callback : videoAdPlayerCallbacks) {
      callback.onError(loadedAdMediaInfo);
    }
    return true;
  }

  public void notifyImaOnContentCompleted() {
    Log.i(LOGTAG, "notifyImaOnContentCompleted");
    for (VideoAdPlayer.VideoAdPlayerCallback callback : videoAdPlayerCallbacks) {
      callback.onContentComplete();
    }
  }

  private void stopAdTracking() {
    Log.i(LOGTAG, "stopAdTracking");
    if (timer != null) {
      timer.cancel();
      timer = null;
    }
  }

  @Override
  public VideoProgressUpdate getAdProgress() {
    long adPosition = videoPlayer.getCurrentPosition();
    return new VideoProgressUpdate(adPosition, adDuration);
  }
}
