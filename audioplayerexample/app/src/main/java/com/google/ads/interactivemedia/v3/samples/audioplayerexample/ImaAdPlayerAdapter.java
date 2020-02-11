package com.google.ads.interactivemedia.v3.samples.audioplayerexample;

import android.content.Context;
import android.util.Log;
import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdPodInfo;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.google.ads.interactivemedia.v3.api.AdsRequest;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;
import com.google.ads.interactivemedia.v3.api.player.AdMediaInfo;
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;

class ImaAdPlayerAdapter implements VideoAdPlayer {

  private static final String TAG = "audio_demo_log";
  private static final String AD_DESC = "Ads";
  private static final String AD_TITLE = "Music will resume shortly.";

  private final Context context;
  private AudioPlayerAdapter audioPlayerAdapter;

  private final ImaSdkFactory sdkFactory;
  private final ImaSdkSettings imaSdkSettings;

  private AdsManager adsManager;
  private AdsLoader adsLoader;

  public ImaAdPlayerAdapter(Context context, AudioPlayerAdapter audioPlayerAdapter) {
    this.context = context;
    this.audioPlayerAdapter = audioPlayerAdapter;
    sdkFactory = ImaSdkFactory.getInstance();
    imaSdkSettings = ImaSdkFactory.getInstance().createImaSdkSettings();
  }

  @Override
  public void loadAd(AdMediaInfo adMediaInfo, AdPodInfo adPodInfo) {
    // Publishers can implement pre-loading the ads media file in this event and then pass the
    // downloaded media file in `playAd` event.
    String url = adMediaInfo.getUrl();
    Log.i(TAG, "loadAd: " + url);
  }

  @Override
  public void playAd(AdMediaInfo adMediaInfo) {
    String adMediaFileUrl = adMediaInfo.getUrl();

    // Use null for mediaIcon unless your ad has an icon to show in the notification menu.
    this.audioPlayerAdapter.load(adMediaFileUrl, AD_TITLE, AD_DESC, null);
    ;
  }

  @Override
  public void pauseAd(AdMediaInfo adMediaInfo) {
    String url = adMediaInfo.getUrl();
    Log.i(TAG, "pauseAd: " + url);
  }

  @Override
  public void stopAd(AdMediaInfo adMediaInfo) {
    String url = adMediaInfo.getUrl();
    Log.i(TAG, "stopAd: " + url);
    this.audioPlayerAdapter.resume();
  }

  @Override
  public void release() {
    this.audioPlayerAdapter = null;
  }

  @Override
  public void addCallback(VideoAdPlayerCallback videoAdPlayerCallback) {}

  @Override
  public void removeCallback(VideoAdPlayerCallback videoAdPlayerCallback) {}

  @Override
  public VideoProgressUpdate getAdProgress() {
    return new VideoProgressUpdate(
        this.audioPlayerAdapter.getCurrentPosition(), this.audioPlayerAdapter.getDuration());
  }

  @Override
  public int getVolume() {
    // Returns the volume of the player as a percentage from 0 to 100.
    return (int) (this.audioPlayerAdapter.getVolume() * 100);
  }

  public void initializeAds(AdDisplayContainer adDisplayContainer) {

    adDisplayContainer.setPlayer(this);
    adsLoader = sdkFactory.createAdsLoader(context, imaSdkSettings, adDisplayContainer);
    adsLoader.addAdErrorListener(
        new AdErrorEvent.AdErrorListener() {
          @Override
          public void onAdError(AdErrorEvent adErrorEvent) {
            Log.e(TAG, "Ad Error: " + adErrorEvent.getError().getMessage());
          }
        });

    adsLoader.addAdsLoadedListener(
        new AdsLoader.AdsLoadedListener() {
          @Override
          public void onAdsManagerLoaded(AdsManagerLoadedEvent adsManagerLoadedEvent) {
            // Ads were successfully loaded, so get the AdsManager instance. AdsManager has
            // events for ad playback and errors.
            adsManager = adsManagerLoadedEvent.getAdsManager();

            // Attach event and error event listeners.
            adsManager.addAdErrorListener(
                new AdErrorEvent.AdErrorListener() {
                  @Override
                  public void onAdError(AdErrorEvent adErrorEvent) {
                    Log.e(TAG, "Ad Error: " + adErrorEvent.getError().getMessage());
                  }
                });
            adsManager.addAdEventListener(
                new AdEvent.AdEventListener() {
                  @Override
                  public void onAdEvent(AdEvent adEvent) {
                    Log.i(TAG, "Ad Event: " + adEvent.getType());

                    // These are the suggested event types to handle. For full list of all ad event
                    // types, see the documentation for AdEvent.AdEventType.
                    switch (adEvent.getType()) {
                      case LOADED:
                        adsManager.start();
                        break;
                      case CONTENT_PAUSE_REQUESTED:
                        audioPlayerAdapter.pause();
                        break;
                      case CONTENT_RESUME_REQUESTED:
                        audioPlayerAdapter.resume();
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
                });
            Log.d(TAG, "onAdsManagerLoaded: calling adsManager.init()");
            adsManager.init();
          }
        });
  }

  public void requestAd(String adTagUrl) {
    AdsRequest request = sdkFactory.createAdsRequest();
    request.setAdTagUrl(adTagUrl);
    Log.i(TAG, "requestAd: " + adTagUrl);
    adsLoader.requestAds(request);
  }
}
