// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.ads.interactivemedia.v3.samples.demoapp.player;

import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer.VideoAdPlayerCallback;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.List;

/**
 * A VideoView that intercepts various methods and reports them back to a set of
 * VideoAdPlayerCallbacks.
 */
public class TrackingVideoView extends VideoView implements OnCompletionListener, OnErrorListener {
  /** Interface for alerting caller of video completion. */
  public interface CompleteCallback {
    public void onComplete();
  }

  private enum PlaybackState {
    STOPPED, PAUSED, PLAYING
  }

  private final List<VideoAdPlayerCallback> adCallbacks = new ArrayList<VideoAdPlayerCallback>(1);
  private CompleteCallback completeCallback;
  private PlaybackState state = PlaybackState.STOPPED;

  public TrackingVideoView(Context context) {
    super(context);
    super.setOnCompletionListener(this);
    super.setOnErrorListener(this);
  }

  public void setCompleteCallback(CompleteCallback callback) {
    this.completeCallback = callback;
  }

  public void togglePlayback() {
    switch(state) {
      case STOPPED:
      case PAUSED:
        start();
        break;
      case PLAYING:
        pause();
        break;
    }
  }

  @Override
  public void start() {
    super.start();
    PlaybackState oldState = state;
    state = PlaybackState.PLAYING;

    switch (oldState) {
      case STOPPED:
        for (VideoAdPlayerCallback callback : adCallbacks) {
          callback.onPlay();
        }
        break;
      case PAUSED:
        for (VideoAdPlayerCallback callback : adCallbacks) {
          callback.onResume();
        }
        break;
      default:
        // Already playing; do nothing.
    }
  }

  @Override
  public void pause() {
    super.pause();
    state = PlaybackState.PAUSED;

    for (VideoAdPlayerCallback callback : adCallbacks) {
      callback.onPause();
    }
  }

  @Override
  public void stopPlayback() {
    super.stopPlayback();
    onStop();
  }

  private void onStop() {
    if (state == PlaybackState.STOPPED) {
      return; // Already stopped; do nothing.
    }

    state = PlaybackState.STOPPED;
  }

  @Override
  public void onCompletion(MediaPlayer mp) {
    // Reset the MediaPlayer.
    // This prevents a race condition which occasionally results in the media
    // player crashing between ads and content.
    mp.setDisplay(null);
    mp.reset();
    mp.setDisplay(getHolder());

    onStop();
    for (VideoAdPlayerCallback callback : adCallbacks) {
      callback.onEnded();
    }
    completeCallback.onComplete();
  }

  @Override
  public boolean onError(MediaPlayer mp, int what, int extra) {
    for (VideoAdPlayerCallback callback : adCallbacks) {
      callback.onError();
    }
    onStop();
    // Returning true signals to MediaPlayer that we handled the error. This will prevent the
    // completion handler from being called.
    return true;
  }

  public void addCallback(VideoAdPlayerCallback callback) {
    adCallbacks.add(callback);
  }

  public void removeCallback(VideoAdPlayerCallback callback) {
    adCallbacks.remove(callback);
  }

  @Override
  public void setOnCompletionListener(OnCompletionListener l) {
    throw new UnsupportedOperationException();
  }
}
