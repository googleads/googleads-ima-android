package com.google.ads.interactivemedia.v3.samples.audioplayerexample;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.media3.common.util.Util;

/** App featuring an audio playlist and buttons to trigger ad playback. */
public class MainActivity extends Activity {
  private static final String AD_TAG_URL =
      "https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/"
          + "audio-preroll&ad_type=audio&sz=1x1&ciu_szs=300x250&gdfp_req=1&output=vast&"
          + "unviewed_position_start=1&env=vp&impl=s&correlator=";
  private final Samples.Sample[] sampleList = Samples.getSamples();

  private AudioPlayerService.AudioPlayerServiceBinder binder;
  private Context context;
  private ViewGroup companionView;

  // Boolean for if the Android service connection is bound.
  private boolean serviceBound = false;

  @Override
  @androidx.media3.common.util.UnstableApi
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_activity);
    context = this;
    companionView = findViewById(R.id.companionAdSlotFrame);

    // Kick off the Audio Player Service in the foreground, so it can outlive this activity.
    Intent intent = new Intent(this, AudioPlayerService.class);
    Util.startForegroundService(this, intent);
    bindService(intent, connection, Context.BIND_AUTO_CREATE);

    ListView listView = findViewById(R.id.list_view);
    listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sampleList));
    listView.setOnItemClickListener(
        (parent, view, position, id) -> {
          if (serviceBound) {
            binder.updateSong(position);
          }
        });

    Button requestAdButton = findViewById(R.id.requestAd);
    requestAdButton.setOnClickListener(
        view -> {
          if (serviceBound) {
            binder.requestAd(AD_TAG_URL);
          }
        });
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    unbindService(connection);
  }

  /** Defines callbacks for service binding, passed to bindService() */
  private final ServiceConnection connection =
      new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
          // We've bound to LocalService, cast the IBinder and get LocalService instance
          binder = (AudioPlayerService.AudioPlayerServiceBinder) service;
          binder.initializeAds(context, companionView);
          serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
          serviceBound = false;
        }
      };
}
