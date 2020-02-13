// Copyright 2014 Google Inc. All Rights Reserved.

package com.google.ads.interactivemedia.v3.samples.samplevideoplayer;

/** Interface definition for controlling video playback. */
public interface VideoPlayer {

  /** Interface for alerting caller of major video events. */
  public interface PlayerCallback {

    /** Called when the current video starts playing from the beginning. */
    void onPlay();

    /** Called when the current video pauses playback. */
    void onPause();

    /** Called when the current video resumes playing from a paused state. */
    void onResume();

    /** Called when the current video has completed playback to the end of the video. */
    void onCompleted();

    /** Called when an error occurs during video playback. */
    void onError();
  }

  /** Play the currently loaded video from its current position. */
  void play();

  /** Pause the currently loaded video. */
  void pause();

  /** Resume the currently loaded video. */
  void resume();

  /** Get the playback progress state (milliseconds) of the current video. */
  int getCurrentPosition();

  /** Progress the currently loaded video to the given position (milliseconds). */
  void seekTo(int videoPosition);

  /** Get the total length of the currently loaded video in milliseconds. */
  int getDuration();

  /** Gets the current volume. Range is [0-100]. */
  int getVolume();

  /** Stop playing the currently loaded video. */
  void stopPlayback();

  /** Prevent the media controller (playback controls) from appearing. */
  void disablePlaybackControls();

  /** Allow the media controller (playback controls) to appear when appropriate. */
  void enablePlaybackControls();

  /** Set the URL or path of the video to play. */
  void setVideoPath(String videoUrl);

  /** Provide the player with a callback for major video events (pause, complete, resume, etc). */
  void addPlayerCallback(PlayerCallback callback);

  /** Remove a player callback from getting notified on video events. */
  void removePlayerCallback(PlayerCallback callback);
}
