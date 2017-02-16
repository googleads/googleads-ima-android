package com.google.ads.interactivemedia.v3.samples.videoplayerapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * The main fragment for displaying video content.
 */
public class VideoFragment extends Fragment {

    private VideoPlayerController mVideoPlayerController;
    private VideoItem mVideoItem;
    private TextView mVideoTitle;
    private LinearLayout mVideoExampleLayout;

    private OnVideoFragmentViewCreatedListener mViewCreatedCallback;

    /**
     * Listener called when the fragment's onCreateView is fired.
     */
    public interface OnVideoFragmentViewCreatedListener {
        public void onVideoFragmentViewCreated();
    }

    @Override
    public void onAttach(Activity activity) {
        try {
            mViewCreatedCallback = (OnVideoFragmentViewCreatedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement " + OnVideoFragmentViewCreatedListener.class.getName());
        }
        super.onAttach(activity);
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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

    public boolean isVmap() {
        return mVideoItem.getIsVmap();
    }
}
