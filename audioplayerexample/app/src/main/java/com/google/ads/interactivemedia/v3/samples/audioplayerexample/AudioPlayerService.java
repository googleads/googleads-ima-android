package com.google.ads.interactivemedia.v3.samples.audioplayerexample;

import static com.google.ads.interactivemedia.v3.samples.audioplayerexample.Constants.PLAYBACK_CHANNEL_ID;
import static com.google.ads.interactivemedia.v3.samples.audioplayerexample.Constants.PLAYBACK_NOTIFICATION_ID;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.analytics.AnalyticsListener;
import androidx.media3.exoplayer.source.ConcatenatingMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.source.ShuffleOrder;
import androidx.media3.session.MediaSession;
import androidx.media3.ui.PlayerNotificationManager;
import androidx.media3.ui.PlayerNotificationManager.BitmapCallback;
import androidx.media3.ui.PlayerNotificationManager.MediaDescriptionAdapter;
import androidx.media3.ui.PlayerNotificationManager.NotificationListener;
import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.CompanionAdSlot;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.common.collect.ImmutableList;

/**
 * Allows audio playback with hooks for advertisements. This is meant to run as a Foreground Service
 * to enable playback to continue even if the app is minimized or cleaned up.
 */
@SuppressLint("UnsafeOptInUsageError")
/* @SuppressLint is needed for new media3 APIs. */
public class AudioPlayerService extends Service {

  private boolean isAdPlaying;
  private ExoPlayer player;
  private MediaSession mediaSession;
  private PlayerNotificationManager playerNotificationManager;
  private ConcatenatingMediaSource contentMediaSource;
  private ImaService imaService;

  private final Samples.Sample[] sampleList = Samples.getSamples();

  @Override
  public void onCreate() {
    super.onCreate();
    final Context context = this;
    isAdPlaying = false;

    player = new ExoPlayer.Builder(context).build();

    DefaultDataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(context);

    contentMediaSource =
        new ConcatenatingMediaSource(
            /* isAtomic= */ false,
            /* useLazyPreparation= */ true,
            new ShuffleOrder.DefaultShuffleOrder(/* length= */ 0));
    for (Samples.Sample sample : sampleList) {
      MediaItem mediaItem = new MediaItem.Builder().setUri(sample.uri).build();
      MediaSource mediaSource =
          new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
      contentMediaSource.addMediaSource(mediaSource);
    }

    player.setMediaSource(contentMediaSource);
    player.prepare();
    player.setPlayWhenReady(true);

    MediaDescriptionAdapter descriptionAdapter =
        new MediaDescriptionAdapter() {
          @Override
          public String getCurrentContentTitle(Player player) {
            if (isAdPlaying) {
              return getString(R.string.ad_content_title);
            }
            return sampleList[player.getCurrentMediaItemIndex()].title;
          }

          @Nullable
          @Override
          public PendingIntent createCurrentContentIntent(Player player) {
            return null;
          }

          @Nullable
          @Override
          public String getCurrentContentText(Player player) {
            if (isAdPlaying) {
              // Null will remove the extra line for description.
              return null;
            }
            return sampleList[player.getCurrentMediaItemIndex()].description;
          }

          @Nullable
          @Override
          public Bitmap getCurrentLargeIcon(Player player, BitmapCallback callback) {
            // Use null for ad playback unless your ad has an icon to show in the notification
            // menu.
            if (isAdPlaying) {
              return null;
            }
            return Samples.getBitmap(
                context, sampleList[player.getCurrentMediaItemIndex()].bitmapResource);
          }
        };

    playerNotificationManager =
        new PlayerNotificationManager.Builder(
                context,
                /* notificationId= */ PLAYBACK_NOTIFICATION_ID,
                /* channelId= */ PLAYBACK_CHANNEL_ID)
            .setMediaDescriptionAdapter(descriptionAdapter)
            .setChannelNameResourceId(R.string.playback_channel_name)
            .setChannelDescriptionResourceId(R.string.playback_channel_description)
            .setNotificationListener(
                new NotificationListener() {
                  public void onNotificationStarted(
                      int notificationId, Notification notification, boolean ongoing) {
                    // This must be called within 5 seconds of the notification being displayed and
                    // before the main app has been killed.
                    startForeground(
                        notificationId,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
                  }

                  @Override
                  public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                    stopSelf();
                  }
                })
            .build();
    playerNotificationManager.setPlayer(player);

    mediaSession = new MediaSession.Builder(context, player).build();

    playerNotificationManager.setMediaSessionToken(mediaSession.getSessionCompatToken());

    imaService = new ImaService(context, dataSourceFactory, new SharedAudioPlayer());
  }

  @Override
  public void onDestroy() {
    mediaSession.release();
    mediaSession = null;
    player.release();
    player = null;

    super.onDestroy();
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return new AudioPlayerServiceBinder();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return START_STICKY;
  }

  /**
   * A limited API for the ImaService which provides a minimal surface of control over playback on
   * the shared ExoPlayer instance.
   */
  class SharedAudioPlayer {
    public void claim() {
      isAdPlaying = true;
      player.setPlayWhenReady(false);
    }

    public void release() {
      if (isAdPlaying) {
        isAdPlaying = false;
        player.setMediaSource(contentMediaSource);
        player.prepare();
        player.setPlayWhenReady(true);
        // TODO: Seek to where you left off the stream, if needed.
      }
    }

    public void prepare(MediaSource mediaSource) {
      player.setMediaSource(mediaSource);
      player.prepare();
    }

    public void addAnalyticsListener(AnalyticsListener listener) {
      player.addAnalyticsListener(listener);
    }

    public ExoPlayer getPlayer() {
      return player;
    }
  }

  /** Provide a Binder to the Application allowing control of the Audio Service */
  public class AudioPlayerServiceBinder extends Binder {
    public void updateSong(int index) {
      if (isAdPlaying) {
        // Return here to prevent changing the song while an ad is playing. A publisher could
        // instead choose queue up the change for after the ad is completed, or cancel the ad.
        return;
      }
      if (player.getCurrentTimeline().getWindowCount() > index) {
        player.seekTo(index, C.TIME_UNSET);
      }
    }

    public void initializeAds(Context context, ViewGroup companionView) {
      ImaSdkFactory sdkFactory = ImaSdkFactory.getInstance();
      AdDisplayContainer container =
          ImaSdkFactory.createAudioAdDisplayContainer(context, imaService.imaVideoAdPlayer);
      CompanionAdSlot companionAdSlot = sdkFactory.createCompanionAdSlot();
      companionAdSlot.setContainer(companionView);
      companionAdSlot.setSize(300, 250);
      container.setCompanionSlots(ImmutableList.of(companionAdSlot));
      imaService.init(container);
    }

    public void requestAd(String adTagUrl) {
      imaService.requestAds(adTagUrl);
    }
  }
}
