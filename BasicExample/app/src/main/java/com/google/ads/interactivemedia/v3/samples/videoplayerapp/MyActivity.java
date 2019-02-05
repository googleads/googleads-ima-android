package com.google.ads.interactivemedia.v3.samples.videoplayerapp;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent.AdErrorListener;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventListener;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsLoader.AdsLoadedListener;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.google.ads.interactivemedia.v3.api.AdsRequest;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;
import com.google.ads.interactivemedia.v3.api.player.ContentProgressProvider;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import com.google.ads.interactivemedia.v3.samples.samplevideoplayer.SampleVideoPlayer;
import com.google.ads.interactivemedia.v3.samples.samplevideoplayer.SampleVideoPlayer.OnVideoCompletedListener;

/**
 * Main Activity.
 */
public class MyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        orientVideoDescriptionFragment(getResources().getConfiguration().orientation);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        orientVideoDescriptionFragment(configuration.orientation);
    }

    private void orientVideoDescriptionFragment(int orientation) {
        // Hide the extra content when in landscape so the video is as large as possible.
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment extraContentFragment = fragmentManager.findFragmentById(R.id.videoDescription);

        if (extraContentFragment != null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                fragmentTransaction.hide(extraContentFragment);
            } else {
                fragmentTransaction.show(extraContentFragment);
            }
            fragmentTransaction.commit();
        }
    }

    /**
     * The main fragment for displaying video content.
     */
    public static class VideoFragment extends Fragment implements AdEventListener, AdErrorListener {

        private static String LOGTAG = "ImaExample";

        // The video player.
        private SampleVideoPlayer mVideoPlayer;

        // The container for the ad's UI.
        private ViewGroup mAdUiContainer;

        // Factory class for creating SDK objects.
        private ImaSdkFactory mSdkFactory;

        // The AdsLoader instance exposes the requestAds method.
        private AdsLoader mAdsLoader;

        // AdsManager exposes methods to control ad playback and listen to ad events.
        private AdsManager mAdsManager;

        // Whether an ad is displayed.
        private boolean mIsAdDisplayed;

        // The play button to trigger the ad request.
        private View mPlayButton;

        @Override
        public void onActivityCreated(Bundle bundle) {
            super.onActivityCreated(bundle);

            // Create an AdsLoader.
            mSdkFactory = ImaSdkFactory.getInstance();
            AdDisplayContainer adDisplayContainer = mSdkFactory.createAdDisplayContainer();
            adDisplayContainer.setAdContainer(mAdUiContainer);
            ImaSdkSettings settings = mSdkFactory.createImaSdkSettings();
            mAdsLoader = mSdkFactory.createAdsLoader(
                    this.getContext(), settings, adDisplayContainer);

            // Add listeners for when ads are loaded and for errors.
            mAdsLoader.addAdErrorListener(this);
            mAdsLoader.addAdsLoadedListener(new AdsLoadedListener() {
                @Override
                public void onAdsManagerLoaded(AdsManagerLoadedEvent adsManagerLoadedEvent) {
                    // Ads were successfully loaded, so get the AdsManager instance. AdsManager has
                    // events for ad playback and errors.
                    mAdsManager = adsManagerLoadedEvent.getAdsManager();

                    // Attach event and error event listeners.
                    mAdsManager.addAdErrorListener(VideoFragment.this);
                    mAdsManager.addAdEventListener(VideoFragment.this);
                    mAdsManager.init();
                }
            });

            // Add listener for when the content video finishes.
            mVideoPlayer.addVideoCompletedListener(new OnVideoCompletedListener() {
                @Override
                public void onVideoCompleted() {
                    // Handle completed event for playing post-rolls.
                    if (mAdsLoader != null) {
                        mAdsLoader.contentComplete();
                    }
                }
            });

            // When Play is clicked, request ads and hide the button.
            mPlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mVideoPlayer.setVideoPath(getString(R.string.content_url));
                    requestAds(getString(R.string.ad_tag_url));
                    view.setVisibility(View.GONE);
                }
            });
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_video, container, false);

            mVideoPlayer = (SampleVideoPlayer) rootView.findViewById(R.id.sampleVideoPlayer);
            mAdUiContainer = (ViewGroup) rootView.findViewById(R.id.videoPlayerWithAdPlayback);
            mPlayButton = rootView.findViewById(R.id.playButton);

            return rootView;
        }

        /**
         * Request video ads from the given VAST ad tag.
         * @param adTagUrl URL of the ad's VAST XML
         */
        private void requestAds(String adTagUrl) {
            // Create the ads request.
            AdsRequest request = mSdkFactory.createAdsRequest();
            request.setAdTagUrl(adTagUrl);
            request.setContentProgressProvider(new ContentProgressProvider() {
                @Override
                public VideoProgressUpdate getContentProgress() {
                    if (mIsAdDisplayed || mVideoPlayer == null || mVideoPlayer.getDuration() <= 0) {
                        return VideoProgressUpdate.VIDEO_TIME_NOT_READY;
                    }
                    return new VideoProgressUpdate(mVideoPlayer.getCurrentPosition(),
                            mVideoPlayer.getDuration());
                }
            });

            // Request the ad. After the ad is loaded, onAdsManagerLoaded() will be called.
            mAdsLoader.requestAds(request);
        }

        @Override
        public void onAdEvent(AdEvent adEvent) {
            Log.i(LOGTAG, "Event: " + adEvent.getType());

            // These are the suggested event types to handle. For full list of all ad event
            // types, see the documentation for AdEvent.AdEventType.
            switch (adEvent.getType()) {
                case LOADED:
                    // AdEventType.LOADED will be fired when ads are ready to be played.
                    // AdsManager.start() begins ad playback. This method is ignored for VMAP or
                    // ad rules playlists, as the SDK will automatically start executing the
                    // playlist.
                    mAdsManager.start();
                    break;
                case CONTENT_PAUSE_REQUESTED:
                    // AdEventType.CONTENT_PAUSE_REQUESTED is fired immediately before a video
                    // ad is played.
                    mIsAdDisplayed = true;
                    mVideoPlayer.pause();
                    break;
                case CONTENT_RESUME_REQUESTED:
                    // AdEventType.CONTENT_RESUME_REQUESTED is fired when the ad is completed
                    // and you should start playing your content.
                    mIsAdDisplayed = false;
                    mVideoPlayer.play();
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

        @Override
        public void onAdError(AdErrorEvent adErrorEvent) {
            Log.e(LOGTAG, "Ad Error: " + adErrorEvent.getError().getMessage());
            mVideoPlayer.play();
        }

        @Override
        public void onResume() {
            if (mAdsManager != null && mIsAdDisplayed) {
                mAdsManager.resume();
            } else {
                mVideoPlayer.play();
            }
            super.onResume();
        }
        @Override
        public void onPause() {
            if (mAdsManager != null && mIsAdDisplayed) {
                mAdsManager.pause();
            } else {
                mVideoPlayer.pause();
            }
            super.onPause();
        }
    }

    /**
     * The fragment for displaying any video title or other non-video content.
     */
    public static class VideoDescriptionFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_video_description, container, false);
        }
    }
}
