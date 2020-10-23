package com.google.ads.interactivemedia.v3.samples.videoplayerapp;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

/** Main Activity. */
public class MyActivity extends Activity {

  private PlayerView playerView;
  private SimpleExoPlayer player;
  private ImaAdsLoader adsLoader;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_my);

    playerView = findViewById(R.id.player_view);

    // Create an AdsLoader with the ad tag url.
    adsLoader = new ImaAdsLoader(this, Uri.parse(getString(R.string.ad_tag_url)));
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
    // Create a SimpleExoPlayer and set it as the player for content and ads.
    player = new SimpleExoPlayer.Builder(this).build();
    playerView.setPlayer(player);
    adsLoader.setPlayer(player);

    DataSource.Factory dataSourceFactory =
        new DefaultDataSourceFactory(this, Util.getUserAgent(this, getString(R.string.app_name)));

    ProgressiveMediaSource.Factory mediaSourceFactory =
        new ProgressiveMediaSource.Factory(dataSourceFactory);

    // Create the MediaSource for the content you wish to play.
    MediaSource mediaSource =
        mediaSourceFactory.createMediaSource(Uri.parse(getString(R.string.content_url)));

    // Create the AdsMediaSource using the AdsLoader and the MediaSource.
    AdsMediaSource adsMediaSource =
        new AdsMediaSource(mediaSource, dataSourceFactory, adsLoader, playerView);

    // Prepare the content and ad to be played with the SimpleExoPlayer.
    player.prepare(adsMediaSource);

    // Set PlayWhenReady. If true, content and ads will autoplay.
    player.setPlayWhenReady(false);
  }
}
