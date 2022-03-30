package com.google.ads.interactivemedia.v3.samples.videoplayerapp;

import java.util.Arrays;
import java.util.List;

/** An enumeration of video metadata. */
public enum VideoMetadata {
  PRE_ROLL_NO_SKIP(
      "https://storage.googleapis.com/gvabox/media/samples/stock.mp4",
      "Pre-roll, linear not skippable",
      "https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/"
          + "single_ad_samples&sz=640x480&cust_params=sample_ct%3Dlinear&"
          + "ciu_szs=300x250%2C728x90&gdfp_req=1&output=vast&unviewed_position_start=1&"
          + "env=vp&impl=s&correlator=",
      R.drawable.thumbnail1,
      false),
  PRE_ROLL_SKIP(
      "https://storage.googleapis.com/gvabox/media/samples/stock.mp4",
      "Pre-roll, linear, skippable",
      "https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/"
          + "single_preroll_skippable&sz=640x480&ciu_szs=300x250%2C728x90&gdfp_req=1&"
          + "output=vast&unviewed_position_start=1&env=vp&impl=s&correlator=",
      R.drawable.thumbnail1,
      false),
  POST_ROLL(
      "https://storage.googleapis.com/gvabox/media/samples/stock.mp4",
      "Post-roll",
      "https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/"
          + "vmap_ad_samples&sz=640x480&cust_params=sample_ar%3Dpostonly&ciu_szs=300x250&"
          + "gdfp_req=1&ad_rule=1&output=vmap&unviewed_position_start=1&env=vp&impl=s&correlator=",
      R.drawable.thumbnail1,
      true),
  VMAP(
      "https://storage.googleapis.com/gvabox/media/samples/stock.mp4",
      "VMAP",
      "https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/"
          + "vmap_ad_samples&sz=640x480&cust_params=sample_ar%3Dpremidpost&ciu_szs=300x250&"
          + "gdfp_req=1&ad_rule=1&output=vmap&unviewed_position_start=1&env=vp&impl=s&"
          + "cmsid=496&vid=short_onecue&correlator=",
      R.drawable.thumbnail1,
      true),
  VMAP_PODS(
      "https://storage.googleapis.com/gvabox/media/samples/stock.mp4",
      "VMAP Pods",
      "https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/"
          + "vmap_ad_samples&sz=640x480&cust_params=sample_ar%3Dpremidpostpod&ciu_szs=300x250&"
          + "gdfp_req=1&ad_rule=1&output=vmap&unviewed_position_start=1&env=vp&impl=s&cmsid=496&"
          + "vid=short_onecue&correlator=",
      R.drawable.thumbnail1,
      true),
  WRAPPER(
      "https://storage.googleapis.com/gvabox/media/samples/stock.mp4",
      "Wrapper",
      "https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/"
          + "single_ad_samples&sz=640x480&cust_params=sample_ct%3Dredirectlinear&"
          + "ciu_szs=300x250%2C728x90&gdfp_req=1&output=vast&unviewed_position_start=1&"
          + "env=vp&impl=s&correlator=",
      R.drawable.thumbnail1,
      false),
  CUSTOM(
      "https://storage.googleapis.com/gvabox/media/samples/stock.mp4",
      "Custom",
      "custom",
      R.drawable.thumbnail1,
      false);

  public static final List<VideoMetadata> APP_VIDEOS =
      Arrays.asList(PRE_ROLL_NO_SKIP, PRE_ROLL_SKIP, POST_ROLL, VMAP, VMAP_PODS, WRAPPER, CUSTOM);

  /** The thumbnail image for the video. */
  public final int thumbnail;

  /** The title of the video. */
  public final String title;

  /** The URL for the video. */
  public final String videoUrl;

  /** The ad tag for the video */
  public final String adTagUrl;

  /** If the ad is VMAP. */
  public final boolean isVmap;

  private VideoMetadata(
      String videoUrl, String title, String adTagUrl, int thumbnail, boolean isVmap) {
    this.videoUrl = videoUrl;
    this.title = title;
    this.adTagUrl = adTagUrl;
    this.thumbnail = thumbnail;
    this.isVmap = isVmap;
  }
}
