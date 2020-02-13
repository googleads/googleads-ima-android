package com.google.ads.interactivemedia.v3.samples.videoplayerapp;

/** Information about a video playlist item that the user will select in a playlist. */
public final class VideoItem {

  private final int mThumbnailResourceId;
  private final String mTitle;
  private final String mVideoUrl;
  private final String mAdTagUrl;
  private final boolean mIsVmap;

  public VideoItem(
      String videoUrl, String title, String adTagUrl, int thumbnailResourceId, boolean isVmap) {
    super();
    mThumbnailResourceId = thumbnailResourceId;
    mTitle = title;
    mAdTagUrl = adTagUrl;
    mVideoUrl = videoUrl;
    mIsVmap = isVmap;
  }

  /** Returns the video thumbnail image resource. */
  public int getImageResource() {
    return mThumbnailResourceId;
  }

  /** Returns the title of the video item. */
  public String getTitle() {
    return mTitle;
  }

  /** Returns the URL of the content video. */
  public String getVideoUrl() {
    return mVideoUrl;
  }

  /** Returns the ad tag for the video. */
  public String getAdTagUrl() {
    return mAdTagUrl;
  }

  /** Returns if the ad is VMAP. */
  public boolean getIsVmap() {
    return mIsVmap;
  }
}
