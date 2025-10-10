// Copyright 2025 Google LLC

package com.google.ads.interactivemedia.v3.samples.snippets;

import android.media.AudioManager;
import android.widget.VideoView;
import com.google.ads.interactivemedia.v3.api.AdPodInfo;
import com.google.ads.interactivemedia.v3.api.player.AdMediaInfo;
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;

/** Example implementation of IMA's VideoAdPlayer interface. */
public class VideoAdPlayerAdapter implements VideoAdPlayer {

  public VideoAdPlayerAdapter(VideoView videoPlayer, AudioManager audioManager) {
    this.videoPlayer = videoPlayer;
    this.audioManager = audioManager;
  }

  @Override
  public void addCallback(VideoAdPlayerCallback videoAdPlayerCallback) {
    videoAdPlayerCallbacks.add(videoAdPlayerCallback);
  }

  @Override
  public void loadAd(AdMediaInfo adMediaInfo, AdPodInfo adPodInfo) {}

  @Override
  public void pauseAd(AdMediaInfo adMediaInfo) {}

  @Override
  public void playAd(AdMediaInfo adMediaInfo) {}

  @Override
  public void release() {}

  @Override
  public void removeCallback(VideoAdPlayerCallback videoAdPlayerCallback) {}

  @Override
  public void stopAd(AdMediaInfo adMediaInfo) {}

  @Override
  public int getVolume() {
    return 0;
  }

  @Override
  public VideoProgressUpdate getAdProgress() {
    long adPosition = videoPlayer.getCurrentPosition();
    return new VideoProgressUpdate(adPosition, 0);
  }
}
