// Copyright 2014 Google Inc. All Rights Reserved.

package com.google.ads.interactivemedia.v3.samples.videoplayerapp;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.google.ads.interactivemedia.v3.api.AdsRequest;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;

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

    // Button the user taps to begin video playback and ad request.
    private View mPlayButton;

    // Default VAST ad tag; more complex apps might select ad tag based on content video criteria.
    private String mDefaultAdTagUrl;

    // Whether an ad is playing, independent of what ad video player the SDK is using.
    private boolean mIsAdPlaying;

    // View that handles taps to toggle ad pause/resume during video playback.
    private View mPlayPauseToggle;

    public VideoPlayerController(Context context,
            VideoPlayerWithAdPlayback videoPlayerWithAdPlayback, View playButton,
            View playPauseToggle) {
        mVideoPlayerWithAdPlayback = videoPlayerWithAdPlayback;
        mPlayButton = playButton;
        mPlayPauseToggle = playPauseToggle;
        mVideoPlayerWithAdPlayback.setOnContentCompleteListener(this);
        mDefaultAdTagUrl = context.getString(R.string.ad_tag_url);
        mIsAdPlaying = false;

        // Create an AdsLoader.
        mSdkFactory = ImaSdkFactory.getInstance();
        mAdsLoader = mSdkFactory.createAdsLoader(context);
        mAdsLoader.addAdErrorListener(this);
        mAdsLoader.addAdsLoadedListener(this);

        // When Play is clicked, request ads and hide the button.
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAds();
                mPlayButton.setVisibility(View.GONE);
            }
        });
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
                mIsAdPlaying = true;
                setPlayPauseOnAdTouch();
                break;
            case CONTENT_RESUME_REQUESTED:
                // AdEventType.CONTENT_RESUME_REQUESTED is fired when the ad is completed and you
                // should start playing your content.
                mVideoPlayerWithAdPlayback.resumeContentAfterAdPlayback();
                mIsAdPlaying = false;
                removePlayPauseOnAdTouch();
                break;
            case PAUSED:
                mIsAdPlaying = false;
                break;
            case RESUMED:
                mIsAdPlaying = true;
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
     * Touch to toggle play/pause during ad play instead of seeking.
     */
    private void setPlayPauseOnAdTouch() {
        // Use AdsManager pause/resume methods instead of the video player pause/resume methods
        // in case the SDK is using a different, SDK-created video player for ad playback.
        mPlayPauseToggle.setOnTouchListener(
                new View.OnTouchListener() {
                    public boolean onTouch(View view, MotionEvent event) {
                        // If an ad is playing, touching it will toggle playback.
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            if (mIsAdPlaying) {
                                mAdsManager.pause();
                            } else {
                                mAdsManager.resume();
                            }
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
        );
    }

    /**
     * Remove the play/pause on touch behavior.
     */
    private void removePlayPauseOnAdTouch() {
        mPlayPauseToggle.setOnTouchListener(null);
    }

    /**
     * An event raised when there is an error loading or playing ads.
     */
    @Override
    public void onAdError(AdErrorEvent adErrorEvent) {
        Log.e("ImaExample", "Ad Error: " + adErrorEvent.getError().getMessage());
        mIsAdPlaying = false;
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
     * Save position of the video, whether content or ad. Can be called when the app is
     * paused, for example.
     */
    public void savePosition() {
        mVideoPlayerWithAdPlayback.savePosition();
    }

    /**
     * Restore the previously saved progress location of the video. Can be called when
     * the app is resumed.
     */
    public void restorePosition() {
        mVideoPlayerWithAdPlayback.restorePosition();
    }
}
