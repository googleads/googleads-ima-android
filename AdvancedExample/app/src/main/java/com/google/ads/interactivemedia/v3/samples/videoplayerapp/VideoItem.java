package com.google.ads.interactivemedia.v3.samples.videoplayerapp;

import android.graphics.Bitmap;

/**
 * Information about a video playlist item that the user will select in a playlist.
 */
public final class VideoItem {

    private final Bitmap mThumbnail;
    private final String mTitle;
    private final String mVideoUrl;
    private final String mAdTagUrl;

    public VideoItem(String videoUrl, String title,  String adTagUrl, Bitmap thumbnail) {
        super();
        mThumbnail = thumbnail;
        mTitle = title;
        mAdTagUrl = adTagUrl;
        mVideoUrl = videoUrl;
    }

    /**
     * Returns the video thumbnail image.
     */
    public Bitmap getImage() {
        return mThumbnail;
    }

    /**
     * Returns the title of the video item.
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Returns the URL of the content video.
     */
    public String getVideoUrl() {
        return mVideoUrl;
    }

    /**
     * Returns the ad tag for the video.
     */
    public String getAdTagUrl() {
        return mAdTagUrl;
    }
}
