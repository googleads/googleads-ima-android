package com.google.ads.interactivemedia.v3.samples.audioplayerexample;

import static com.google.ads.interactivemedia.v3.samples.audioplayerexample.Samples.SAMPLES;

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.ShuffleOrder;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.ui.PlayerNotificationManager.BitmapCallback;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

/** Wraps an audio player that is shared with IMA SDK for playing audio ads. */
class AudioPlayerAdapter {
  private static final String MEDIA_SESSION_TAG = "audio_demo_session";
  private static final boolean AUTO_PLAY = false;

  private final SimpleExoPlayer simpleExoPlayer;
  private final ProgressiveMediaSource.Factory mediaSourceFactory;

  private final PlayerNotificationManager.MediaDescriptionAdapter descriptionAdapter;
  private final MediaSessionCompat mediaSession;
  private final MediaSessionConnector mediaSessionConnector;

  private final ConcatenatingMediaSource songList;
  private int lastSongIndex;
  private long lastSongPosition;

  private String loadedMediaUrl;
  private String loadedMediaTitle;
  private String loadedMediaDescription;
  private Bitmap loadedMediaIcon;

  AudioPlayerAdapter(final Context context, String applicationName) {

    simpleExoPlayer = new SimpleExoPlayer.Builder(context).build();
    DefaultDataSourceFactory dataSourceFactory =
        new DefaultDataSourceFactory(context, Util.getUserAgent(context, applicationName));

    songList =
        new ConcatenatingMediaSource(
            /* isAtomic= */ false,
            /* useLazyPreparation= */ true,
            new ShuffleOrder.DefaultShuffleOrder(/* length= */ 0));
    mediaSourceFactory = new ProgressiveMediaSource.Factory(dataSourceFactory);
    for (Samples.Sample sample : SAMPLES) {
      MediaSource mediaSource = mediaSourceFactory.createMediaSource(sample.uri);
      songList.addMediaSource(mediaSource);
    }
    simpleExoPlayer.prepare(songList);
    simpleExoPlayer.setPlayWhenReady(AUTO_PLAY);

    descriptionAdapter =
        new PlayerNotificationManager.MediaDescriptionAdapter() {
          @Override
          public String getCurrentContentTitle(Player player) {
            if (loadedMediaTitle != null) {
              return loadedMediaTitle;
            }
            return SAMPLES[player.getCurrentWindowIndex()].title;
          }

          @Nullable
          @Override
          public PendingIntent createCurrentContentIntent(Player player) {
            return null;
          }

          @Nullable
          @Override
          public String getCurrentContentText(Player player) {
            if (loadedMediaDescription != null) {
              return loadedMediaDescription;
            }
            return SAMPLES[player.getCurrentWindowIndex()].description;
          }

          @Nullable
          @Override
          public Bitmap getCurrentLargeIcon(Player player, BitmapCallback callback) {
            if (loadedMediaIcon != null) {
              return loadedMediaIcon;
            }
            return Samples.getBitmap(
                context, SAMPLES[player.getCurrentWindowIndex()].bitmapResource);
          }
        };

    mediaSession = new MediaSessionCompat(context, MEDIA_SESSION_TAG);
    mediaSession.setActive(true);

    mediaSessionConnector = new MediaSessionConnector(mediaSession);
    mediaSessionConnector.setQueueNavigator(
        new TimelineQueueNavigator(mediaSession) {
          @Override
          public MediaDescriptionCompat getMediaDescription(Player player, int windowIndex) {
            if (loadedMediaDescription != null) {
              return new MediaDescriptionCompat.Builder()
                  .setDescription(loadedMediaDescription)
                  .build();
            }
            return Samples.createMediaDescription(context, SAMPLES[windowIndex]);
          }
        });
    mediaSessionConnector.setPlayer(simpleExoPlayer);
  }

  Player getSimpleExoPlayer() {
    return simpleExoPlayer;
  }

  PlayerNotificationManager.MediaDescriptionAdapter getDescriptionAdapter() {
    return descriptionAdapter;
  }

  void releasePlayer() {
    mediaSessionConnector.setPlayer(null);
    simpleExoPlayer.release();
  }

  MediaSessionCompat.Token getSessionToken() {
    return mediaSession.getSessionToken();
  }

  void seekTo(int songIndexInPlaylist) {
    simpleExoPlayer.seekTo(songIndexInPlaylist, C.TIME_UNSET);
    simpleExoPlayer.setPlayWhenReady(true);
  }

  void pause() {
    simpleExoPlayer.setPlayWhenReady(false);
    lastSongIndex = simpleExoPlayer.getCurrentWindowIndex();
    lastSongPosition = simpleExoPlayer.getCurrentPosition();
  }

  void resume() {
    simpleExoPlayer.setPlayWhenReady(false);
    loadedMediaUrl = null;
    loadedMediaTitle = null;
    loadedMediaDescription = null;
    simpleExoPlayer.prepare(songList);
    simpleExoPlayer.seekTo(lastSongIndex, lastSongPosition);
    simpleExoPlayer.setPlayWhenReady(true);
  }

  void load(
      @NonNull String mediaFileUrl,
      @Nullable String mediaTitle,
      @Nullable String mediaDescription,
      @Nullable Bitmap mediaIcon) {
    // Check the last URL for duplicate requests of the same URL.
    // The last URL is set back to null in `onPlayerStateChanged` event when ExoPlay finishes
    // playing the file.
    if (!mediaFileUrl.equals(loadedMediaUrl)) {

      loadedMediaUrl = mediaFileUrl;
      loadedMediaTitle = mediaTitle;
      loadedMediaDescription = mediaDescription;
      loadedMediaIcon = mediaIcon;
      pause();

      Uri uri = Uri.parse(mediaFileUrl);
      ProgressiveMediaSource media = mediaSourceFactory.createMediaSource(uri);
      simpleExoPlayer.prepare(media);
      simpleExoPlayer.setPlayWhenReady(true);
    }
  }

  public float getVolume() {
    return simpleExoPlayer.getVolume();
  }

  public long getCurrentPosition() {
    return simpleExoPlayer.getCurrentPosition();
  }

  public long getDuration() {
    return simpleExoPlayer.getDuration();
  }
}
