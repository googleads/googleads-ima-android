package com.google.ads.interactivemedia.v3.samples.audioplayerexample;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.ads.interactivemedia.v3.api.CompanionAdSlot;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.common.collect.ImmutableList;

import static com.google.ads.interactivemedia.v3.samples.audioplayerexample.Samples.SAMPLES;

/**
 * App featuring an audio playlist and buttons to trigger ad playback.
 */
public class MainActivity extends Activity {

  private static final String AD_TAG_URL = "https://vastsynthesizer.appspot.com/ima-sample-audio";
  private AudioPlayerService boundService;
  private ServiceConnection connection;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_activity);

    // Create an ImaSdkFactory for creating CompanionAdSlot and an AdDisplayContainer.
    ImaSdkFactory sdkFactory = ImaSdkFactory.getInstance();

    // Create a CompanionAdSlot and bind it with a ViewGroup on the UI for rendering companion
    // banners.
    ViewGroup companionView = findViewById(R.id.companionAdSlotFrame);
    final CompanionAdSlot companionAdSlot = sdkFactory.createCompanionAdSlot();
    companionAdSlot.setContainer(companionView);
    companionAdSlot.setSize(640, 640);

    connection =
        new ServiceConnection() {
          @Override
          public void onServiceConnected(ComponentName name, IBinder binder) {
            boundService = ((AudioPlayerService.ServiceBinder) binder).getBoundService();
            boundService.initializeAds(ImmutableList.of(companionAdSlot));
          }

          @Override
          public void onServiceDisconnected(ComponentName name) {
            boundService = null;
          }
        };

    Intent intent = new Intent(this, AudioPlayerService.class);
    Util.startForegroundService(this, intent);
    bindService(intent, connection, Context.BIND_AUTO_CREATE);

    ListView listView = findViewById(R.id.list_view);
    listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, SAMPLES));
    listView.setOnItemClickListener(
        new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (boundService != null) {
              boundService.getPlayer().seekTo(position);
            }
          }
        });

    Button requestAdButton = findViewById(R.id.requestAd);
    requestAdButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            if (boundService != null) {
              boundService.getAdPlayer().requestAd(AD_TAG_URL);
            }
          }
        });
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (connection != null) {
      unbindService(connection);
      connection = null;
    }
  }
}
