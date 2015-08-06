// Copyright 2014 Google Inc. All Rights Reserved.
package com.google.ads.interactivemedia.v3.samples.videoplayerapp;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.google.ads.interactivemedia.v3.api.AdsRequest;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.samples.samplevideoplayer.VideoPlayer;

/**
 * Ads logic for handling the IMA SDK integration code and events.
 */
public class VideoPlayerController implements AdErrorEvent.AdErrorListener,
        AdsLoader.AdsLoadedListener, AdEvent.AdEventListener,
        VideoPlayerWithAdPlayback.OnContentCompleteListener {

    // Container with references to video player and ad UI ViewGroup.
    private AdDisplayContainer mAdDisplayContainer;

    // The AdsLoader instance exposes the requestAds method.
    private AdsLoader mAdsLoader;

    // AdsManager exposes methods to control ad playback and listen to ad events.
    private AdsManager mAdsManager;

    // Factory class for creating SDK objects.
    private ImaSdkFactory mSdkFactory;

    // Ad-enabled video player.
    private VideoPlayerWithAdPlayback mVideoPlayerWithAdPlayback;

    // Default VAST ad tag; more complex apps might select ad tag based on content video criteria.
    private String mDefaultAdTagUrl;

    public VideoPlayerController(Context context, VideoPlayer videoPlayer,
            ViewGroup adUiContainer) {
        mVideoPlayerWithAdPlayback = new VideoPlayerWithAdPlayback(videoPlayer, adUiContainer);
        mVideoPlayerWithAdPlayback.init();
        mVideoPlayerWithAdPlayback.setOnContentCompleteListener(this);
        mDefaultAdTagUrl = context.getString(R.string.ad_tag_url_vmap);

        // Create an AdsLoader.
        mSdkFactory = ImaSdkFactory.getInstance();
        mAdsLoader = mSdkFactory.createAdsLoader(context);
        mAdsLoader.addAdErrorListener(this);
        mAdsLoader.addAdsLoadedListener(this);
    }

    /**
     * Request video ads using the default VAST ad tag. Typically, you would change your ad tag
     * URL based on the current content being played.
     */
    private void requestAds() {
        requestAds(mDefaultAdTagUrl);
    }

    /**
     * Request video ads from the given VAST ad tag.
     * @param adTagUrl URL of the ad's VAST XML
     */
    private void requestAds(String adTagUrl) {
        mAdDisplayContainer = mSdkFactory.createAdDisplayContainer();
        mAdDisplayContainer.setPlayer(mVideoPlayerWithAdPlayback.getVideoAdPlayer());
        mAdDisplayContainer.setAdContainer(mVideoPlayerWithAdPlayback.getAdUiContainer());

        // Create the ads request.
        AdsRequest request = mSdkFactory.createAdsRequest();
        request.setAdTagUrl(adTagUrl);
        request.setAdDisplayContainer(mAdDisplayContainer);
        request.setContentProgressProvider(mVideoPlayerWithAdPlayback.getContentProgressProvider());

        // Request the ad. After the ad is loaded, onAdsManagerLoaded() will be called.
        mAdsLoader.requestAds(request);
    }

    /**
     * An event raised when ads are successfully loaded from the ad server via an AdsLoader.
     */
    @Override
    public void onAdsManagerLoaded(AdsManagerLoadedEvent adsManagerLoadedEvent) {
        // Ads were successfully loaded, so get the AdsManager instance. AdsManager has
        // events for ad playback and errors.
        mAdsManager = adsManagerLoadedEvent.getAdsManager();

        // Attach event and error event listeners.
        mAdsManager.addAdErrorListener(this);
        mAdsManager.addAdEventListener(this);
        mAdsManager.init();
    }

    /**
     * Responds to AdEvents.
     */
    @Override
    public void onAdEvent(AdEvent adEvent) {
        Log.i("ImaExample", "Event: " + adEvent.getType());

        // These are the suggested event types to handle. For full list of all ad event types,
        // see the documentation for AdEvent.AdEventType.
        switch (adEvent.getType()) {
            case LOADED:
                // AdEventType.LOADED will be fired when ads are ready to be played.
                // AdsManager.start() begins ad playback. This method is ignored for VMAP or ad
                // rules playlists, as the SDK will automatically start executing the playlist.
                mAdsManager.start();
                break;
            case CONTENT_PAUSE_REQUESTED:
                // AdEventType.CONTENT_PAUSE_REQUESTED is fired immediately before a video ad is
                // played.
                mVideoPlayerWithAdPlayback.pauseContentForAdPlayback();
                break;
            case CONTENT_RESUME_REQUESTED:
                // AdEventType.CONTENT_RESUME_REQUESTED is fired when the ad is completed and you
                // should start playing your content.
                mVideoPlayerWithAdPlayback.resumeContentAfterAdPlayback();
                break;
            case ALL_ADS_COMPLETED:
                if (mAdsManager != null) {
                    mAdsManager.destroy();
                    mAdsManager = null;
                }
                break;
            default:
                break;
        }
    }

    /**
     * An event raised when there is an error loading or playing ads.
     */
    @Override
    public void onAdError(AdErrorEvent adErrorEvent) {
        Log.e("ImaExample", "Ad Error: " + adErrorEvent.getError().getMessage());
        mVideoPlayerWithAdPlayback.resumeContentAfterAdPlayback();
    }

    /**
     * Event raised by VideoPlayerWithAdPlayback when the content video is complete.
     */
    @Override
    public void onContentComplete() {
        mAdsLoader.contentComplete();
    }

    /**
     * Set metadata about the content video. In more complex implementations, this might
     * more than just a URL and could trigger additional decisions regarding ad tag selection.
     */
    public void setContentVideo(String videoPath) {
        mVideoPlayerWithAdPlayback.setContentVideoPath(videoPath);
    }

    /**
     * Starts ad playback.
     */
    public void play() {
        requestAds();
    }

    /**
     * Resumes video playback.
     */
    public void resume() {
        mVideoPlayerWithAdPlayback.restorePosition();
        if (mAdsManager != null && mVideoPlayerWithAdPlayback.getIsAdDisplayed()) {
            mAdsManager.resume();
        }
    }

    /**
     * Pauses video playback.
     */
    public void pause() {
        mVideoPlayerWithAdPlayback.savePosition();
        if (mAdsManager != null && mVideoPlayerWithAdPlayback.getIsAdDisplayed()) {
            mAdsManager.pause();
        }
    }
}
