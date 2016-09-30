package com.google.ads.interactivemedia.v3.samples.videoplayerapp;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * Main Activity.
 */
public class MyActivity extends AppCompatActivity
    implements VideoListFragment.OnVideoSelectedListener,
            VideoListFragment.OnVideoListFragmentResumedListener,
            VideoFragment.OnVideoFragmentViewCreatedListener {

    private static final String VIDEO_PLAYLIST_FRAGMENT_TAG = "video_playlist_fragment_tag";
    private static final String VIDEO_EXAMPLE_FRAGMENT_TAG = "video_example_fragment_tag";

    private static CastApplication sCastApplication;
    private boolean mReadyToCast;

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

        sCastApplication = new CastApplication(getApplicationContext());
        orientAppUi();
    }

    @Override
    protected void onStart() {
        super.onStart();
        sCastApplication.activityOnStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);

        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
        MediaRouteActionProvider mediaRouteActionProvider =
                (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
        mediaRouteActionProvider.setRouteSelector(sCastApplication.getMediaRouteSelector());
        mediaRouteMenuItem.setVisible(mReadyToCast);
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

        Fragment videoListFragment = fragmentManager.findFragmentByTag(
                VIDEO_PLAYLIST_FRAGMENT_TAG);

        if (videoFragment != null) {
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
        } else {
            // If returning to the list from a fullscreen video, check if the video
            // list fragment exists and is hidden. If so, show it.
            if (videoListFragment != null && videoListFragment.isHidden()) {
                fragmentManager.beginTransaction().show(videoListFragment).commit();
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
        sCastApplication.setVideoFragment(videoFragment);
        mReadyToCast = true;
        invalidateOptionsMenu();
        orientAppUi();
    }

    @Override
    public void onVideoListFragmentResumed() {
        mReadyToCast = false;
        invalidateOptionsMenu();
        orientAppUi();
    }

    @Override
    public void onVideoFragmentViewCreated() {
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
}
