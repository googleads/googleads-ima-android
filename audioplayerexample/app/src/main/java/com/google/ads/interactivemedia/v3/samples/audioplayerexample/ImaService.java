package com.google.ads.interactivemedia.v3.samples.audioplayerexample;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.analytics.AnalyticsListener;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent.AdErrorListener;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventListener;
import com.google.ads.interactivemedia.v3.api.AdPodInfo;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsLoader.AdsLoadedListener;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.google.ads.interactivemedia.v3.api.AdsRequest;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;
import com.google.ads.interactivemedia.v3.api.player.AdMediaInfo;
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer;
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer.VideoAdPlayerCallback;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import com.google.ads.interactivemedia.v3.samples.audioplayerexample.AudioPlayerService.SharedAudioPlayer;
import java.util.ArrayList;
import java.util.List;

/**
 * Takes control of audio playback from the AudioPlayerService to play some ads and then returns
 * control afterwards.
 */
public final class ImaService implements AdErrorListener, AdEventListener, AdsLoadedListener {

  private static final String LOGGING_TAG = "ImaService";

  private AdsLoader adsLoader;
  private AdsManager adsManager;
  private AdMediaInfo currentAd;
  private ImaProgressTracker progressTracker;
  private final Context context;
  private final SharedAudioPlayer sharedAudioPlayer;
  private final ExoPlayer exoPlayer;
  private final List<VideoAdPlayerCallback> callbacks;
  private final ImaSdkFactory sdkFactory;
  private final DefaultDataSource.Factory dataSourceFactory;
  public ImaVideoAdPlayer imaVideoAdPlayer = new ImaVideoAdPlayer();

  @UnstableApi
  ImaService(
      Context context,
      DefaultDataSource.Factory dataSourceFactory,
      SharedAudioPlayer sharedAudioPlayer) {
    this.context = context;
    this.sharedAudioPlayer = sharedAudioPlayer;
    this.exoPlayer = sharedAudioPlayer.getPlayer();
    this.callbacks = new ArrayList<>();
    this.sdkFactory = ImaSdkFactory.getInstance();
    this.dataSourceFactory = dataSourceFactory;
    sharedAudioPlayer.addAnalyticsListener(new ImaListener());
  }

  /**
   * Initializes the ImaService. Note: Ad playback with CompanionAds requires an AdDisplayContainer
   * from the MainActivity.
   */
  public void init(AdDisplayContainer adDisplayContainer) {
    adsLoader = sdkFactory.createAdsLoader(context, getImaSdkSettings(), adDisplayContainer);
    adsLoader.addAdErrorListener(this);
    adsLoader.addAdsLoadedListener(this);

    progressTracker = new ImaProgressTracker(imaVideoAdPlayer);
  }

  public void requestAds(String adTagUrl) {
    AdsRequest request = sdkFactory.createAdsRequest();
    request.setAdTagUrl(adTagUrl);
    // The ContentProgressProvider is only needed for scheduling ads with VMAP ad requests
    request.setContentProgressProvider(
        () -> new VideoProgressUpdate(exoPlayer.getCurrentPosition(), exoPlayer.getDuration()));
    adsLoader.requestAds(request);
  }

  @Override
  public void onAdsManagerLoaded(AdsManagerLoadedEvent adsManagerLoadedEvent) {
    adsManager = adsManagerLoadedEvent.getAdsManager();
    adsManager.addAdErrorListener(this);
    adsManager.addAdEventListener(this);
    adsManager.init();
  }

  @Override
  public void onAdError(AdErrorEvent adErrorEvent) {
    Log.e(LOGGING_TAG, "Ad Error: ".concat(adErrorEvent.getError().getMessage()));
  }

  @Override
  @UnstableApi
  public void onAdEvent(AdEvent adEvent) {
    Log.i(LOGGING_TAG, "Event: ".concat(adEvent.getType().toString()));
    switch (adEvent.getType()) {
      case LOADED:
        // If preloading we may to call start() at a particular time offset, instead of immediately.
        adsManager.start();
        break;
      case CONTENT_PAUSE_REQUESTED:
        sharedAudioPlayer.claim();
        break;
      case CONTENT_RESUME_REQUESTED:
        sharedAudioPlayer.release();
        break;
      case ALL_ADS_COMPLETED:
        if (adsManager != null) {
          adsManager.destroy();
          adsManager = null;
        }
        break;
      default:
        break;
    }
  }

  /** Allows IMA to tell the custom player what to do. */
  class ImaVideoAdPlayer implements VideoAdPlayer {
    @Override
    public void loadAd(AdMediaInfo adMediaInfo, AdPodInfo adPodInfo) {
      // If implementing preloading, we would pass the url back to the audio player here.
    }

    @Override
    @UnstableApi
    public void playAd(AdMediaInfo adMediaInfo) {
      String url = adMediaInfo.getUrl();
      progressTracker.start();
      if (currentAd == adMediaInfo) {
        for (VideoAdPlayerCallback callback : callbacks) {
          callback.onResume(adMediaInfo);
        }
      } else {
        currentAd = adMediaInfo;
        for (VideoAdPlayerCallback callback : callbacks) {
          callback.onPlay(adMediaInfo);
        }
        MediaItem mediaItem = new MediaItem.Builder().setUri(Uri.parse(url)).build();
        MediaSource mediaSource =
            new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
        sharedAudioPlayer.prepare(mediaSource);
      }
      exoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void pauseAd(AdMediaInfo adMediaInfo) {
      exoPlayer.setPlayWhenReady(false);
      progressTracker.stop();
      for (VideoAdPlayerCallback callback : callbacks) {
        callback.onPause(adMediaInfo);
      }
    }

    @Override
    public void stopAd(AdMediaInfo adMediaInfo) {
      progressTracker.stop();
      exoPlayer.setPlayWhenReady(false);
      notifyEnded();
    }

    @Override
    public void release() {
      // Add any additional clean-up of resource used by the VideoAdPlayer.
    }

    @Override
    public void addCallback(VideoAdPlayerCallback callback) {
      callbacks.add(callback);
    }

    @Override
    public void removeCallback(VideoAdPlayerCallback callback) {
      callbacks.remove(callback);
    }

    @Override
    public VideoProgressUpdate getAdProgress() {
      if (currentAd == null) {
        return null;
      }

      return new VideoProgressUpdate(exoPlayer.getCurrentPosition(), exoPlayer.getDuration());
    }

    void sendProgressUpdate() {
      for (VideoAdPlayerCallback callback : callbacks) {
        callback.onAdProgress(currentAd, getAdProgress());
      }
    }

    @Override
    public int getVolume() {
      if (exoPlayer == null) {
        return -1;
      } else {
        return (int) (100 * exoPlayer.getVolume());
      }
    }
  }

  /** Encapsulates callbacks for ExoPlayer changes, and lets IMA know the state of playback */
  @UnstableApi
  class ImaListener implements AnalyticsListener {
    @Override
    public void onPlaybackStateChanged(@NonNull EventTime eventTime, int playbackState) {
      if (currentAd == null) {
        // This may be null if state changes after stopAd for a given mediaInfo
        return;
      }
      switch (playbackState) {
        case Player.STATE_BUFFERING:
          for (VideoAdPlayerCallback callback : callbacks) {
            callback.onBuffering(currentAd);
          }
          break;
        case Player.STATE_READY:
          for (VideoAdPlayerCallback callback : callbacks) {
            callback.onLoaded(currentAd);
          }
          break;
        case Player.STATE_ENDED:
          // Handles when the media item in the source is completed.
          notifyEnded();
          break;
        default:
          break;
      }
    }

    @Override
    public void onPlayWhenReadyChanged(
        @NonNull EventTime eventTime, boolean playWhenReady, int playbackState) {
      if (currentAd == null) {
        // This may be null if state changes after stopAd for a given mediaInfo
        return;
      }
      switch (playbackState) {
        case Player.STATE_BUFFERING:
          for (VideoAdPlayerCallback callback : callbacks) {
            callback.onBuffering(currentAd);
          }
          break;
        case Player.STATE_READY:
          for (VideoAdPlayerCallback callback : callbacks) {
            callback.onLoaded(currentAd);
          }
          break;
        case Player.STATE_ENDED:
          // Handles when the media item in the source is completed.
          notifyEnded();
          break;
        default:
          break;
      }
    }
  }

  private void notifyEnded() {
    for (VideoAdPlayerCallback callback : callbacks) {
      callback.onEnded(currentAd);
    }
  }

  static class ImaProgressTracker implements Handler.Callback {
    static final int START = 0;
    static final int UPDATE = 1;
    static final int QUIT = 2;
    static final int UPDATE_PERIOD_MS = 1000;
    private final Handler messageHandler;
    private final ImaVideoAdPlayer player;

    ImaProgressTracker(ImaVideoAdPlayer player) {
      this.messageHandler = new Handler(this);
      this.player = player;
    }

    @Override
    public boolean handleMessage(Message msg) {
      switch (msg.what) {
        case QUIT:
          // Don't remove START message since it is yet to send updates. The QUIT message comes from
          // the current ad being updated, hence we remove UPDATE messages.
          messageHandler.removeMessages(UPDATE);
          break;
        case UPDATE:
        // Intentional fallthrough. START message is introduced as a way to differentiate the
        // beginning (the START of progress event) and progress itself (UPDATE events). Handling
        // for both the messages are same.
        case START:
          player.sendProgressUpdate();
          messageHandler.removeMessages(UPDATE);
          messageHandler.sendEmptyMessageDelayed(UPDATE, UPDATE_PERIOD_MS);
          break;
        default:
          break;
      }
      return true;
    }

    void start() {
      messageHandler.sendEmptyMessage(START);
    }

    void stop() {
      messageHandler.sendMessageAtFrontOfQueue(Message.obtain(messageHandler, QUIT));
    }
  }

  public static void initializeIMASDK(Context context) {
    ImaSdkFactory.getInstance().initialize(context, getImaSdkSettings());
  }

  private static ImaSdkSettings getImaSdkSettings() {
    ImaSdkSettings settings = ImaSdkFactory.getInstance().createImaSdkSettings();
    // Set any IMA SDK settings here.
    return settings;
  }
}
