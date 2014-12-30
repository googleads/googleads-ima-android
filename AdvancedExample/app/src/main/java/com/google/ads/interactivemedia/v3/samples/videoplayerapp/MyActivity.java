package com.google.ads.interactivemedia.v3.samples.videoplayerapp;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Main Activity.
 */
public class MyActivity extends ActionBarActivity
    implements VideoListFragment.OnVideoSelectedListener {

    private static final String VIDEO_PLAYLIST_FRAGMENT_TAG = "video_playlist_fragment_tag";
    private static final String VIDEO_EXAMPLE_FRAGMENT_TAG = "video_example_fragment_tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        // The video list fragment won't exist for phone layouts, so add it dynamically so we can
        // .replace() it once the user selects a video.
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(VIDEO_PLAYLIST_FRAGMENT_TAG) == null) {
            VideoListFragment videoListFragment = new VideoListFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.video_example_container, videoListFragment,
                            VIDEO_PLAYLIST_FRAGMENT_TAG)
                    .commit();
        }

        orientAppUi();
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
        orientAppUi();
    }

    private void orientAppUi() {
        int orientation = getResources().getConfiguration().orientation;
        boolean isLandscape = (orientation == Configuration.ORIENTATION_LANDSCAPE);
        // Hide the non-video content when in landscape so the video is as large as possible.
        FragmentManager fragmentManager = getSupportFragmentManager();
        VideoFragment videoFragment = (VideoFragment) fragmentManager
                .findFragmentByTag(VIDEO_EXAMPLE_FRAGMENT_TAG);

        if (videoFragment != null) {
            Fragment videoListFragment = fragmentManager.findFragmentByTag(
                    VIDEO_PLAYLIST_FRAGMENT_TAG);
            // If the video playlist is onscreen (tablets) then hide that fragment.
            if (videoListFragment != null) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                if (isLandscape) {
                    fragmentTransaction.hide(videoListFragment);
                } else {
                    fragmentTransaction.show(videoListFragment);
                }
                fragmentTransaction.commit();
            }
            videoFragment.makeFullscreen(isLandscape);
            if (isLandscape) {
                hideStatusBar();
            } else {
                showStatusBar();
            }
        }
    }

    @Override
    public void onVideoSelected(VideoItem videoItem) {
        VideoFragment videoFragment = (VideoFragment)
                getSupportFragmentManager().findFragmentByTag(VIDEO_EXAMPLE_FRAGMENT_TAG);

        // Add the video fragment if it's missing (phone form factor), but only if the user
        // manually selected the video.
        if (videoFragment == null) {
            VideoListFragment videoListFragment = (VideoListFragment) getSupportFragmentManager()
                    .findFragmentByTag(VIDEO_PLAYLIST_FRAGMENT_TAG);
            int videoPlaylistFragmentId = videoListFragment.getId();

            videoFragment = new VideoFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(videoPlaylistFragmentId, videoFragment, VIDEO_EXAMPLE_FRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
        }
        videoFragment.loadVideo(videoItem);
        orientAppUi();
    }

    private void hideStatusBar() {
        if (Build.VERSION.SDK_INT >= 16) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
            getSupportActionBar().hide();
        }
    }

    private void showStatusBar() {
        if (Build.VERSION.SDK_INT >= 16) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            getSupportActionBar().show();
        }
    }

    /**
     * The main fragment for displaying video content.
     */
    public static class VideoFragment extends Fragment {

        private VideoPlayerController mVideoPlayerController;
        private VideoItem mVideoItem;
        private TextView mVideoTitle;
        private LinearLayout mVideoExampleLayout;

        @Override
        public void onActivityCreated(Bundle bundle) {
            super.onActivityCreated(bundle);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_video, container, false);
            initUi(rootView);

            return rootView;
        }

        private void loadVideo(VideoItem videoItem) {
            if (mVideoPlayerController == null) {
                mVideoItem = videoItem;
                return;
            }
            mVideoItem = videoItem;
            mVideoPlayerController.setContentVideo(mVideoItem.getVideoUrl());
            mVideoPlayerController.setAdTagUrl(videoItem.getAdTagUrl());
            mVideoTitle.setText(videoItem.getTitle());

            mVideoPlayerController.requestAndPlayAds();
        }

        private void initUi(View rootView) {
            VideoPlayerWithAdPlayback mVideoPlayerWithAdPlayback = (VideoPlayerWithAdPlayback)
                    rootView.findViewById(R.id.videoPlayerWithAdPlayback);
            View playButton = rootView.findViewById(R.id.playButton);
            View playPauseToggle = rootView.findViewById(R.id.videoContainer);
            ViewGroup companionAdSlot = (ViewGroup) rootView.findViewById(R.id.companionAdSlot);
            mVideoTitle = (TextView) rootView.findViewById(R.id.video_title);
            mVideoExampleLayout = (LinearLayout) rootView.findViewById(R.id.videoExampleLayout);

            final TextView logText = (TextView) rootView.findViewById(R.id.logText);
            final ScrollView logScroll = (ScrollView) rootView.findViewById(R.id.logScroll);

            // Provide an implementation of a logger so we can output SDK events to the UI.
            VideoPlayerController.Logger logger = new VideoPlayerController.Logger() {
                @Override
                public void log(String message) {
                    Log.i("ImaExample", message);
                    if (logText != null) {
                        logText.append(message);
                    }
                    if (logScroll != null) {
                        logScroll.post(new Runnable() {
                            @Override
                            public void run() {
                                logScroll.fullScroll(View.FOCUS_DOWN);
                            }
                        });
                    }
                }
            };

            mVideoPlayerController = new VideoPlayerController(this.getActivity(),
                    mVideoPlayerWithAdPlayback, playButton, playPauseToggle,
                    getString(R.string.ad_ui_lang), companionAdSlot, logger);

            // If we've already selected a video, load it now.
            if (mVideoItem != null) {
                loadVideo(mVideoItem);
            }
        }

        /**
         * Shows or hides all non-video UI elements to make the video as large as possible.
         */
        public void makeFullscreen(boolean isFullscreen) {
            for (int i = 0; i < mVideoExampleLayout.getChildCount(); i++) {
                View view = mVideoExampleLayout.getChildAt(i);
                // If it's not the video element, hide or show it, depending on fullscreen status.
                if (view.getId() != R.id.videoContainer) {
                    if (isFullscreen) {
                        view.setVisibility(View.GONE);
                    } else {
                        view.setVisibility(View.VISIBLE);
                    }
                }
            }
        }

        @Override
        public void onPause() {
            if (mVideoPlayerController != null) {
                mVideoPlayerController.savePosition();
            }
            super.onPause();
        }

        @Override
        public void onResume() {
            if (mVideoPlayerController != null) {
                mVideoPlayerController.restorePosition();
            }
            super.onResume();
        }
    }
}
