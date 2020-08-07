package com.google.ads.interactivemedia.v3.samples.videoplayerapp;

/** Information about a video playlist item that the user will select in a playlist. */
public final class VideoItem {

  private final int thumbnailResourceId;
  private final String title;
  private final String videoUrl;
  private final String adTagUrl;
  private final boolean isVmap;

  public VideoItem(
      String video, String videoTitle, String adTag, int resourceId, boolean isVmapAd) {
    super();
    thumbnailResourceId = resourceId;
    title = videoTitle;
    adTagUrl = adTag;
    videoUrl = video;
    isVmap = isVmapAd;
  }

  /** Returns the video thumbnail image resource. */
  public int getImageResource() {
    return thumbnailResourceId;
  }

  /** Returns the title of the video item. */
  public String getTitle() {
    return title;
  }

  /** Returns the URL of the content video. */
  public String getVideoUrl() {
    return videoUrl;
  }

  /** Returns the ad tag for the video. */
  public String getAdTagUrl() {
    return adTagUrl;
  }

  /** Returns if the ad is VMAP. */
  public boolean getIsVmap() {
    return isVmap;
  }
}
