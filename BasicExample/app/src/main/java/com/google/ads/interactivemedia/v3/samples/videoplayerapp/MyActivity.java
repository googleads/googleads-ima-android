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
import com.google.ads.interactivemedia.v3.samples.samplevideoplayer.VideoPlayer;

/**
 * Main Activity.
 */
public class MyActivity extends ActionBarActivity {

    // The video player.
    private static VideoPlayer mVideoPlayer;
    // The container for the ad's UI.
    private static ViewGroup mAdUIContainer;
    // The play button to trigger the ad request.
    private static View mPlayButton;

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
            mVideoPlayerController = new VideoPlayerController(this.getActivity(), mVideoPlayer,
                    mAdUIContainer);
            mVideoPlayerController.setContentVideo(getString(R.string.content_url));

            // When Play is clicked, request ads and hide the button.
            mPlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mVideoPlayerController.play();
                    view.setVisibility(View.GONE);
                }
            });
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_video, container, false);

            mVideoPlayer = (VideoPlayer) rootView.findViewById(R.id.sampleVideoPlayer);
            mAdUIContainer = (ViewGroup) rootView.findViewById(R.id.videoPlayerWithAdPlayback);
            mPlayButton = rootView.findViewById(R.id.playButton);

            return rootView;
        }

        @Override
        public void onResume() {
            if (mVideoPlayerController != null) {
                mVideoPlayerController.resume();
            }
            super.onResume();
        }
        @Override
        public void onPause() {
            if (mVideoPlayerController != null) {
                mVideoPlayerController.pause();
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
