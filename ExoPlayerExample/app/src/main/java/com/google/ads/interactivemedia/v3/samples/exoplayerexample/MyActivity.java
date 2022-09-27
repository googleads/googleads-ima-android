package com.google.ads.interactivemedia.v3.samples.exoplayerexample;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import androidx.multidex.MultiDex;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.util.Util;

/** Main Activity. */
public class MyActivity extends Activity {

  private static final String SAMPLE_VIDEO_URL =
      "https://storage.googleapis.com/gvabox/media/samples/stock.mp4";
  private static final String SAMPLE_VAST_TAG_URL =
      "https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/"
          + "single_ad_samples&sz=640x480&cust_params=sample_ct%3Dlinear&ciu_szs=300x250%2C728x90"
          + "&gdfp_req=1&output=vast&unviewed_position_start=1&env=vp&impl=s&correlator=";

  private StyledPlayerView playerView;
  private ExoPlayer player;
  private ImaAdsLoader adsLoader;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_my);
    MultiDex.install(this);

    playerView = findViewById(R.id.player_view);

    // Create an AdsLoader.
    adsLoader = new ImaAdsLoader.Builder(/* context= */ this).build();
  }

  @Override
  public void onStart() {
    super.onStart();
    if (Util.SDK_INT > 23) {
      initializePlayer();
      if (playerView != null) {
        playerView.onResume();
      }
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    if (Util.SDK_INT <= 23 || player == null) {
      initializePlayer();
      if (playerView != null) {
        playerView.onResume();
      }
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (Util.SDK_INT <= 23) {
      if (playerView != null) {
        playerView.onPause();
      }
      releasePlayer();
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    if (Util.SDK_INT > 23) {
      if (playerView != null) {
        playerView.onPause();
      }
      releasePlayer();
    }
  }

  @Override
  protected void onDestroy() {
    adsLoader.release();

    super.onDestroy();
  }

  private void releasePlayer() {
    adsLoader.setPlayer(null);
    playerView.setPlayer(null);
    player.release();
    player = null;
  }

  private void initializePlayer() {
    // Set up the factory for media sources, passing the ads loader and ad view providers.
    DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(this);

    MediaSource.Factory mediaSourceFactory =
        new DefaultMediaSourceFactory(dataSourceFactory)
            .setAdsLoaderProvider(unusedAdTagUri -> adsLoader)
            .setAdViewProvider(playerView);

    // Create an ExoPlayer and set it as the player for content and ads.
    player = new ExoPlayer.Builder(this).setMediaSourceFactory(mediaSourceFactory).build();
    playerView.setPlayer(player);
    adsLoader.setPlayer(player);

    // Create the MediaItem to play, specifying the content URI and ad tag URI.
    Uri contentUri = Uri.parse(SAMPLE_VIDEO_URL);
    Uri adTagUri = Uri.parse(SAMPLE_VAST_TAG_URL);
    MediaItem mediaItem =
        new MediaItem.Builder()
            .setUri(contentUri)
            .setAdsConfiguration(new MediaItem.AdsConfiguration.Builder(adTagUri).build())
            .build();

    // Prepare the content and ad to be played with the SimpleExoPlayer.
    player.setMediaItem(mediaItem);
    player.prepare();

    // Set PlayWhenReady. If true, content and ads will autoplay.
    player.setPlayWhenReady(false);
  }
}
