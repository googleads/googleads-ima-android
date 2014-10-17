package com.google.ads.interactivemedia.v3.samples.videoplayerapp;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

/**
 * Main Activity.
 */
public class MyActivity extends ActionBarActivity {
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
    public static class VideoFragment extends Fragment {

        protected VideoPlayerController mVideoPlayerController;

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

        protected void initUi(View rootView) {
            VideoPlayerWithAdPlayback mVideoPlayerWithAdPlayback = (VideoPlayerWithAdPlayback)
                    rootView.findViewById(R.id.videoPlayerWithAdPlayback);
            View playButton = rootView.findViewById(R.id.playButton);
            View playPauseToggle = rootView.findViewById(R.id.videoOverlay);
            mVideoPlayerController = new VideoPlayerController(this.getActivity(),
                    mVideoPlayerWithAdPlayback, playButton, playPauseToggle);
            mVideoPlayerController.setContentVideo(getString(R.string.content_url));
        }

        @Override
        public void onPause() {
            super.onPause();
            if (mVideoPlayerController != null) {
                mVideoPlayerController.savePosition();
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            if (mVideoPlayerController != null) {
                mVideoPlayerController.restorePosition();
            }
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
