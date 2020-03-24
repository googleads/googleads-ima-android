package com.google.ads.interactivemedia.v3.samples.audioplayerexample;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import com.google.android.exoplayer2.util.Util;

/** App featuring an audio playlist and buttons to trigger ad playback. */
public class MainActivity extends Activity {
  private static final String AD_TAG_URL = "https://vastsynthesizer.appspot.com/ima-sample-audio";
  private final Samples.Sample[] sampleList = Samples.getSamples();

  private AudioPlayerService.AudioPlayerServiceBinder binder;
  private Context context;
  private ViewGroup companionView;

  // Boolean for if the Android service connection is bound.
  private boolean serviceBound = false;

  @Override
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
        new OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (serviceBound) {
              binder.updateSong(position);
            }
          }
        });

    Button requestAdButton = findViewById(R.id.requestAd);
    requestAdButton.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (serviceBound) {
              binder.requestAd(AD_TAG_URL);
            }
          }
        });
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (connection != null) {
      unbindService(connection);
    }
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
