package com.google.ads.interactivemedia.v3.samples.videoplayerapp;

import java.util.Arrays;
import java.util.List;

/**
 * An enumeration of video metadata.
 */
public enum VideoMetadata {

    PRE_ROLL_NO_SKIP(
            "http://rmcdn.2mdn.net/MotifFiles/html/1248596/android_1330378998288.mp4",
            "Pre-roll, linear not skippable",
            "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/"
                    + "single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast"
                    + "&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct"
                    + "%3Dlinear&correlator=",
            R.drawable.thumbnail1,
            false),
    PRE_ROLL_SKIP(
            "http://rmcdn.2mdn.net/MotifFiles/html/1248596/android_1330378998288.mp4",
            "Pre-roll, linear, skippable",
            "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/"
                    + "single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast"
                    + "&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct"
                    + "%3Dskippablelinear&correlator=",
            R.drawable.thumbnail1,
            false),
    POST_ROLL(
            "http://rmcdn.2mdn.net/MotifFiles/html/1248596/android_1330378998288.mp4",
            "Post-roll",
            "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/"
                    + "ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp"
                    + "&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite"
                    + "%26sample_ar%3Dpostonly&cmsid=496&vid=short_onecue&correlator=",
            R.drawable.thumbnail1,
            true),
    VMAP("http://rmcdn.2mdn.net/MotifFiles/html/1248596/android_1330378998288.mp4",
            "VMAP",
            "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/"
                    + "ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp"
                    + "&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite"
                    + "%26sample_ar%3Dpremidpost&cmsid=496&vid=short_onecue&correlator=",
            R.drawable.thumbnail1,
            true),
    VMAP_PODS("http://rmcdn.2mdn.net/MotifFiles/html/1248596/android_1330378998288.mp4",
            "VMAP Pods",
            "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/"
                    + "ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp"
                    + "&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite"
                    + "%26sample_ar%3Dpremidpostpod&cmsid=496&vid=short_onecue&correlator=",
            R.drawable.thumbnail1,
            true),
    WRAPPER("http://rmcdn.2mdn.net/MotifFiles/html/1248596/android_1330378998288.mp4",
            "Wrapper",
            "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/"
                    + "single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast"
                    + "&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct"
                    + "%3Dredirectlinear&correlator=",
            R.drawable.thumbnail1,
            false),
    ADSENSE("http://rmcdn.2mdn.net/MotifFiles/html/1248596/android_1330378998288.mp4",
            "AdSense",
            "http://googleads.g.doubleclick.net/pagead/ads?client=ca-video-afvtest&ad_type=video",
            R.drawable.thumbnail1,
            false),
    CUSTOM("http://rmcdn.2mdn.net/MotifFiles/html/1248596/android_1330378998288.mp4",
            "Custom",
            "custom",
            R.drawable.thumbnail1,
            false);

    public static final List<VideoMetadata> APP_VIDEOS =
        Arrays.asList(PRE_ROLL_NO_SKIP, PRE_ROLL_SKIP, POST_ROLL, VMAP, VMAP_PODS, WRAPPER,
            ADSENSE, CUSTOM);

    /** The thumbnail image for the video. **/
    public final int thumbnail;

    /** The title of the video. **/
    public final String title;

    /** The URL for the video. **/
    public final String videoUrl;

    /** The ad tag for the video **/
    public final String adTagUrl;

    /** If the ad is VMAP. **/
    public final boolean isVmap;

    private VideoMetadata(String videoUrl, String title, String adTagUrl, int thumbnail,
                          boolean isVmap) {
        this.videoUrl = videoUrl;
        this.title = title;
        this.adTagUrl = adTagUrl;
        this.thumbnail = thumbnail;
        this.isVmap = isVmap;
    }
}
