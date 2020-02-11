package com.google.ads.interactivemedia.v3.samples.audioplayerexample;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import androidx.annotation.Nullable;
import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

/** Plays music and ads using a shared instance of `ExoPlayer`. */
public class AudioPlayerService extends Service {

  public static final String PLAYBACK_CHANNEL_ID = "audio_demo_service_channel";
  public static final int PLAYBACK_NOTIFICATION_ID = 1;

  private PlayerNotificationManager playerNotificationManager;
  private AudioPlayerAdapter audioPlayerAdapter;
  private ImaAdPlayerAdapter imaAdPlayerAdapter;

  @Override
  public void onCreate() {
    super.onCreate();

    // Create an `AudioPlayerAdapter` to wrap an instance of ExoPlayer.
    audioPlayerAdapter = new AudioPlayerAdapter(this, getString(R.string.application_name));

    // Share the instance of ExoPlayer with the IMA SDK.
    imaAdPlayerAdapter = new ImaAdPlayerAdapter(this, audioPlayerAdapter);

    // Create a manager for a status bar notification that is required for foreground services.
    // https://developer.android.com/guide/topics/ui/notifiers/notifications.html#foreground-service
    playerNotificationManager =
        PlayerNotificationManager.createWithNotificationChannel(
            this,
            PLAYBACK_CHANNEL_ID,
            R.string.playback_channel_name,
            R.string.playback_channel_description,
            PLAYBACK_NOTIFICATION_ID,
            audioPlayerAdapter.getDescriptionAdapter());

    playerNotificationManager.setMediaSessionToken(audioPlayerAdapter.getSessionToken());
    playerNotificationManager.setPlayer(audioPlayerAdapter.getSimpleExoPlayer());
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return START_STICKY;
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return new ServiceBinder(this);
  }

  @Override
  public void onDestroy() {
    playerNotificationManager.setPlayer(null);
    audioPlayerAdapter.releasePlayer();

    super.onDestroy();
  }

  public void initializeAds(AdDisplayContainer adContainer) {
    imaAdPlayerAdapter.initializeAds(adContainer);
  }

  public AudioPlayerAdapter getPlayer() {
    return audioPlayerAdapter;
  }

  public ImaAdPlayerAdapter getAdPlayer() {
    return imaAdPlayerAdapter;
  }

  /** Provides access to the `AudioPlayerService` instance after binding. */
  public static class ServiceBinder extends Binder {
    private final AudioPlayerService boundService;

    /** @param service The bound instance of the service */
    public ServiceBinder(AudioPlayerService service) {
      boundService = service;
    }

    public AudioPlayerService getBoundService() {
      return boundService;
    }
  }
}
