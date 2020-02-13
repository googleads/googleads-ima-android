package com.google.ads.interactivemedia.v3.samples.videoplayerapp;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;

/** The main fragment for displaying video content. */
public class VideoFragment extends Fragment {

  private VideoPlayerController mVideoPlayerController;
  private VideoItem mVideoItem;
  private TextView mVideoTitle;
  private ScrollView mVideoExampleLayout;
  private OnVideoFragmentViewCreatedListener mViewCreatedCallback;

  /** Listener called when the fragment's onCreateView is fired. */
  public interface OnVideoFragmentViewCreatedListener {
    public void onVideoFragmentViewCreated();
  }

  @Override
  public void onActivityCreated(Bundle bundle) {
    super.onActivityCreated(bundle);
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_video, container, false);
    initUi(rootView);
    if (mViewCreatedCallback != null) {
      mViewCreatedCallback.onVideoFragmentViewCreated();
    }
    return rootView;
  }

  public void loadVideo(VideoItem videoItem) {
    if (mVideoPlayerController == null) {
      mVideoItem = videoItem;
      return;
    }
    mVideoItem = videoItem;
    mVideoPlayerController.setContentVideo(mVideoItem.getVideoUrl());
    mVideoPlayerController.setAdTagUrl(videoItem.getAdTagUrl());
    mVideoTitle.setText(videoItem.getTitle());
  }

  private void initUi(View rootView) {
    VideoPlayerWithAdPlayback mVideoPlayerWithAdPlayback =
        rootView.findViewById(R.id.videoPlayerWithAdPlayback);
    View playButton = rootView.findViewById(R.id.playButton);
    View playPauseToggle = rootView.findViewById(R.id.videoContainer);
    ViewGroup companionAdSlot = rootView.findViewById(R.id.companionAdSlot);
    mVideoTitle = rootView.findViewById(R.id.video_title);
    mVideoExampleLayout = rootView.findViewById(R.id.videoExampleLayout);
    mVideoExampleLayout.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
    mVideoExampleLayout.setSmoothScrollingEnabled(true);

    // Make the dummyScrollContent height the size of the screen height.
    DisplayMetrics displayMetrics = new DisplayMetrics();
    getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    ConstraintLayout constraintLayout = rootView.findViewById(R.id.constraintLayout);
    ConstraintSet forceHeight = new ConstraintSet();
    forceHeight.clone(constraintLayout);
    forceHeight.constrainHeight(R.id.dummyScrollContent, displayMetrics.heightPixels);
    forceHeight.applyTo(constraintLayout);

    final TextView logText = rootView.findViewById(R.id.logText);

    // Provide an implementation of a logger so we can output SDK events to the UI.
    VideoPlayerController.Logger logger =
        new VideoPlayerController.Logger() {
          @Override
          public void log(String message) {
            Log.i("ImaExample", message);
            if (logText != null) {
              logText.append(message);
            }
          }
        };

    mVideoPlayerController =
        new VideoPlayerController(
            this.getActivity(),
            mVideoPlayerWithAdPlayback,
            playButton,
            playPauseToggle,
            getString(R.string.ad_ui_lang),
            companionAdSlot,
            logger);

    // If we've already selected a video, load it now.
    if (mVideoItem != null) {
      loadVideo(mVideoItem);
    }
  }

  /** Shows or hides all non-video UI elements to make the video as large as possible. */
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

  public VideoPlayerController getVideoPlayerController() {
    return mVideoPlayerController;
  }

  @Override
  public void onPause() {
    if (mVideoPlayerController != null) {
      mVideoPlayerController.pause();
    }
    super.onPause();
  }

  @Override
  public void onResume() {
    if (mVideoPlayerController != null) {
      mVideoPlayerController.resume();
    }
    super.onResume();
  }

  @Override
  public void onDestroy() {
    if (mVideoPlayerController != null) {
      mVideoPlayerController.destroy();
    }
    super.onDestroy();
  }

  public boolean isVmap() {
    return mVideoItem.getIsVmap();
  }
}
