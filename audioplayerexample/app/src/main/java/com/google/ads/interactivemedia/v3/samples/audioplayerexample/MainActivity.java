package com.google.ads.interactivemedia.v3.samples.audioplayerexample;

import static com.google.ads.interactivemedia.v3.samples.audioplayerexample.Samples.SAMPLES;

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
import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.CompanionAdSlot;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.common.collect.ImmutableList;
import java.util.Timer;
import java.util.TimerTask;

/** App featuring an audio playlist and buttons to trigger ad playback. */
public class MainActivity extends Activity {
  private static final String AD_TAG_URL = "https://vastsynthesizer.appspot.com/ima-sample-audio";

  private AudioPlayerService.AudioPlayerServiceBinder binder;
  private AdDisplayContainer adContainer;

  // Boolean for if the Android service connection is bound.
  private boolean serviceBound = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_activity);
    Context context = this;
    ViewGroup companionView = (ViewGroup) findViewById(R.id.companionAdSlotFrame);
    adContainer = createAdDisplayContainer(context, companionView);

    // Kick off the Audio Player Service in the foreground, so it can outlive this activity.
    Intent intent = new Intent(this, AudioPlayerService.class);
    Util.startForegroundService(this, intent);
    bindService(intent, connection, Context.BIND_AUTO_CREATE);

    ListView listView = findViewById(R.id.list_view);
    listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, SAMPLES));
    listView.setOnItemClickListener(
        new OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (serviceBound) {
              binder.updateSong(position);
            }
          }
        });

    Button requestAdButton = (Button) findViewById(R.id.requestAd);
    requestAdButton.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (serviceBound) {
              binder.requestAd(AD_TAG_URL);
            }
          }
        });

    Button requestAdLaterButton = (Button) findViewById(R.id.requestAdLater);
    requestAdLaterButton.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            TimerTask updateTimerTask =
                new TimerTask() {
                  @Override
                  public void run() {
                    if (serviceBound) {
                      binder.requestAd(AD_TAG_URL);
                    }
                  }
                };
            Timer timer = new Timer();
            timer.schedule(updateTimerTask, 3000);
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
          binder.initializeAds(adContainer);
          serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
          serviceBound = false;
        }
      };

  private static AdDisplayContainer createAdDisplayContainer(
      Context context, ViewGroup companionView) {
    ImaSdkFactory sdkFactory = ImaSdkFactory.getInstance();
    AdDisplayContainer container = sdkFactory.createAudioAdDisplayContainer(context);
    CompanionAdSlot companionAdSlot = sdkFactory.createCompanionAdSlot();
    companionAdSlot.setContainer(companionView);
    companionAdSlot.setSize(640, 640);
    container.setCompanionSlots(ImmutableList.of(companionAdSlot));
    return container;
  }
}
