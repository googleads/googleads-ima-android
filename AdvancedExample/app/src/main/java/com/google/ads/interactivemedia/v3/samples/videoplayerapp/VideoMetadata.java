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
            "http://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/"
                    + "single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast"
                    + "&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct"
                    + "%3Dlinear&correlator=",
            R.drawable.thumbnail1),
    PRE_ROLL_SKIP(
            "http://rmcdn.2mdn.net/MotifFiles/html/1248596/android_1330378998288.mp4",
            "Pre-roll, linear, skippable",
            "http://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/"
                    + "single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast"
                    + "&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct"
                    + "%3Dskippablelinear&correlator=",
            R.drawable.thumbnail1),
    POST_ROLL(
            "http://rmcdn.2mdn.net/MotifFiles/html/1248596/android_1330378998288.mp4",
            "Post-roll",
            "http://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/"
                    + "ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp"
                    + "&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite"
                    + "%26sample_ar%3Dpostonly&cmsid=496&vid=short_onecue&correlator=",
            R.drawable.thumbnail1),
    VMAP("http://rmcdn.2mdn.net/MotifFiles/html/1248596/android_1330378998288.mp4",
            "VMAP",
            "http://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/"
                    + "ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp"
                    + "&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite"
                    + "%26sample_ar%3Dpremidpost&cmsid=496&vid=short_onecue&correlator=",
            R.drawable.thumbnail1),
    VMAP_PODS("http://rmcdn.2mdn.net/MotifFiles/html/1248596/android_1330378998288.mp4",
            "VMAP Pods",
            "http://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/"
                    + "ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp"
                    + "&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite"
                    + "%26sample_ar%3Dpremidpostpod&cmsid=496&vid=short_onecue&correlator=",
            R.drawable.thumbnail1),
    WRAPPER("http://rmcdn.2mdn.net/MotifFiles/html/1248596/android_1330378998288.mp4",
            "Wrapper",
            "http://pubads.g.doubleclick.net/gampad/ads?sz=400x300&iu=%2F6062%2Fhanna_MA_group"
                    + "%2Fwrapper_with_comp&ciu_szs=728x90&impl=s&gdfp_req=1&env=vp&output"
                    + "=xml_vast2&unviewed_position_start=1&m_ast=vast&url=[referrer_url]"
                    + "&correlator=[timestamp]",
            R.drawable.thumbnail1),
    ADSENSE("http://rmcdn.2mdn.net/MotifFiles/html/1248596/android_1330378998288.mp4",
            "AdSense",
            "http://googleads.g.doubleclick.net/pagead/ads?client=ca-video-afvtest&ad_type=video",
            R.drawable.thumbnail1),
    CUSTOM("http://rmcdn.2mdn.net/MotifFiles/html/1248596/android_1330378998288.mp4",
            "Custom",
            "custom",
            R.drawable.thumbnail1);

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

    private VideoMetadata(String videoUrl, String title, String adTagUrl, int thumbnail) {
        this.videoUrl = videoUrl;
        this.title = title;
        this.adTagUrl = adTagUrl;
        this.thumbnail = thumbnail;
    }
}
