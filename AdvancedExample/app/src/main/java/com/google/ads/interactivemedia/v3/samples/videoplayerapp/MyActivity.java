package com.google.ads.interactivemedia.v3.samples.videoplayerapp;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.gms.cast.framework.CastButtonFactory;

/** Main Activity. */
public class MyActivity extends AppCompatActivity
    implements VideoListFragment.OnVideoSelectedListener,
        VideoListFragment.OnVideoListFragmentResumedListener,
        VideoFragment.OnVideoFragmentViewCreatedListener {

  private static final String VIDEO_PLAYLIST_FRAGMENT_TAG = "video_playlist_fragment_tag";
  private static final String VIDEO_EXAMPLE_FRAGMENT_TAG = "video_example_fragment_tag";

  private CastApplication mCastApplication;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_my);

    // The video list fragment won't exist for phone layouts, so add it dynamically so we can
    // .replace() it once the user selects a video.
    FragmentManager fragmentManager = getSupportFragmentManager();
    if (fragmentManager.findFragmentByTag(VIDEO_PLAYLIST_FRAGMENT_TAG) == null) {
      VideoListFragment videoListFragment = new VideoListFragment();
      getSupportFragmentManager()
          .beginTransaction()
          .add(R.id.video_example_container, videoListFragment, VIDEO_PLAYLIST_FRAGMENT_TAG)
          .commit();
    }
    mCastApplication = new CastApplication(this);
    orientAppUi();
  }

  @Override
  protected void onResume() {
    mCastApplication.onResume();
    super.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mCastApplication.onPause();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);

    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.my, menu);

    CastButtonFactory.setUpMediaRouteButton(
        getApplicationContext(), menu, R.id.media_route_menu_item);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
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
    VideoFragment videoFragment =
        (VideoFragment) fragmentManager.findFragmentByTag(VIDEO_EXAMPLE_FRAGMENT_TAG);

    Fragment videoListFragment = fragmentManager.findFragmentByTag(VIDEO_PLAYLIST_FRAGMENT_TAG);

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
    VideoFragment videoFragment =
        (VideoFragment) getSupportFragmentManager().findFragmentByTag(VIDEO_EXAMPLE_FRAGMENT_TAG);

    // Add the video fragment if it's missing (phone form factor), but only if the user
    // manually selected the video.
    if (videoFragment == null) {
      VideoListFragment videoListFragment =
          (VideoListFragment)
              getSupportFragmentManager().findFragmentByTag(VIDEO_PLAYLIST_FRAGMENT_TAG);
      int videoPlaylistFragmentId = videoListFragment.getId();

      videoFragment = new VideoFragment();
      getSupportFragmentManager()
          .beginTransaction()
          .replace(videoPlaylistFragmentId, videoFragment, VIDEO_EXAMPLE_FRAGMENT_TAG)
          .addToBackStack(null)
          .commit();
    }
    videoFragment.loadVideo(videoItem);
    mCastApplication.setVideoFragment(videoFragment);
    invalidateOptionsMenu();
    orientAppUi();
  }

  @Override
  public void onVideoListFragmentResumed() {
    invalidateOptionsMenu();
    orientAppUi();
  }

  @Override
  public void onVideoFragmentViewCreated() {
    orientAppUi();
  }

  private void hideStatusBar() {
    if (Build.VERSION.SDK_INT >= 16) {
      getWindow()
          .getDecorView()
          .setSystemUiVisibility(
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
