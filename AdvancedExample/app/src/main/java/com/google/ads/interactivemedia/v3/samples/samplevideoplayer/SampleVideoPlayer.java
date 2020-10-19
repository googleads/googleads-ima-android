// Copyright 2014 Google Inc. All Rights Reserved.

package com.google.ads.interactivemedia.v3.samples.samplevideoplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.util.AttributeSet;
import android.widget.MediaController;
import android.widget.VideoView;
import java.util.ArrayList;
import java.util.List;

/** A VideoView that intercepts various methods and reports them back via a PlayerCallback. */
public class SampleVideoPlayer extends VideoView implements VideoPlayer {

  private enum PlaybackState {
    STOPPED,
    PAUSED,
    PLAYING
  }

  private MediaController mediaController;
  private PlaybackState playbackState;
  private final List<PlayerCallback> videoPlayerCallbacks = new ArrayList<PlayerCallback>(1);

  public SampleVideoPlayer(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  public SampleVideoPlayer(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public SampleVideoPlayer(Context context) {
    super(context);
    init();
  }

  private void init() {
    playbackState = PlaybackState.STOPPED;
    mediaController = new MediaController(getContext());
    mediaController.setAnchorView(this);
    setMediaController(mediaController);

    // Set OnCompletionListener to notify our callbacks when the video is completed.
    super.setOnCompletionListener(
        new OnCompletionListener() {

          @Override
          public void onCompletion(MediaPlayer mediaPlayer) {
            // Reset the MediaPlayer.
            mediaPlayer.reset();
            mediaPlayer.setDisplay(getHolder());
            playbackState = PlaybackState.STOPPED;

            for (PlayerCallback callback : videoPlayerCallbacks) {
              callback.onComplete();
            }
          }
        });

    // Set OnErrorListener to notify our callbacks if the video errors.
    super.setOnErrorListener(
        new OnErrorListener() {

          @Override
          public boolean onError(MediaPlayer mp, int what, int extra) {
            playbackState = PlaybackState.STOPPED;
            for (PlayerCallback callback : videoPlayerCallbacks) {
              callback.onError();
            }

            // Returning true signals to MediaPlayer that we handled the error. This will
            // prevent the completion handler from being called.
            return true;
          }
        });
  }

  @Override
  public int getDuration() {
    return playbackState == PlaybackState.STOPPED ? 0 : super.getDuration();
  }

  @Override
  public int getVolume() {
    // Get the system's audio service and get media volume from it.
    AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
    if (audioManager != null) {
      double volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
      double max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
      if (max <= 0) {
        return 0;
      }
      // Return a range from 0-100.
      return (int) ((volume / max) * 100.0f);
    }
    return 0;
  }

  @Override
  public void setOnCompletionListener(OnCompletionListener listener) {
    // The OnCompletionListener can only be implemented by SampleVideoPlayer.
    throw new UnsupportedOperationException();
  }

  @Override
  public void setOnErrorListener(OnErrorListener listener) {
    // The OnErrorListener can only be implemented by SampleVideoPlayer.
    throw new UnsupportedOperationException();
  }

  // Methods implementing the VideoPlayer interface.
  @Override
  public void play() {
    super.start();
    for (PlayerCallback callback : videoPlayerCallbacks) {
      callback.onPlay();
    }
    playbackState = PlaybackState.PLAYING;
  }

  @Override
  public void resume() {
    super.start();
    for (PlayerCallback callback : videoPlayerCallbacks) {
      callback.onResume();
    }
    playbackState = PlaybackState.PLAYING;
  }

  @Override
  public void pause() {
    super.pause();
    playbackState = PlaybackState.PAUSED;
    for (PlayerCallback callback : videoPlayerCallbacks) {
      callback.onPause();
    }
  }

  @Override
  public void stopPlayback() {
    if (playbackState == PlaybackState.STOPPED) {
      return;
    }
    super.stopPlayback();
    playbackState = PlaybackState.STOPPED;
  }

  @Override
  public void disablePlaybackControls() {
    // The default behavior for mediaController.hide() hides the controls after 3 seconds.
    mediaController.hide();
  }

  @Override
  public void enablePlaybackControls(int timeout) {
    mediaController.show(timeout);
  }

  @Override
  public void addPlayerCallback(PlayerCallback callback) {
    videoPlayerCallbacks.add(callback);
  }

  @Override
  public void removePlayerCallback(PlayerCallback callback) {
    videoPlayerCallbacks.remove(callback);
  }
}
