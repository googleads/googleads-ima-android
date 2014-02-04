// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.ads.interactivemedia.v3.samples.demoapp;

import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.text.InputType.TYPE_TEXT_VARIATION_URI;

import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent.AdErrorListener;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventListener;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsLoader.AdsLoadedListener;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.google.ads.interactivemedia.v3.api.AdsRequest;
import com.google.ads.interactivemedia.v3.api.CompanionAdSlot;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;
import com.google.ads.interactivemedia.v3.samples.demoapp.player.DemoPlayer;
import com.google.ads.interactivemedia.v3.samples.demoapp.player.TrackingVideoView.CompleteCallback;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * A demo application for the Android IMA SDK.
 */
public class DroidDemoActivity extends Activity
    implements
    AdErrorListener,
    AdsLoadedListener,
    AdEventListener,
    CompleteCallback {

  private static final String CONTENT_URL =
      "http://rmcdn.2mdn.net/MotifFiles/html/1248596/" + "android_1330378998288.mp4";
  protected String[] defaultTagUrls;
  protected FrameLayout videoHolder;
  protected DemoPlayer videoPlayer;
  protected ScrollView logScroll;
  protected TextView log;
  protected ViewGroup companionView;
  protected ViewGroup leaderboardCompanionView;
  protected AdsLoader adsLoader;
  protected AdsManager adsManager;
  protected AdDisplayContainer container;
  protected ImaSdkFactory sdkFactory;
  protected ImaSdkSettings sdkSettings;

  protected Button languageButton;
  protected Button requestAdButton;
  protected Spinner defaultTagSpinner;
  protected boolean isAdStarted;
  protected boolean isAdPlaying;

  protected boolean contentStarted = false;
  protected String tagUrl;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    initUi();

    sdkFactory = ImaSdkFactory.getInstance();
    createAdsLoader();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    // This is a crude way to save the video state on orientation change.
    if (videoPlayer != null) {
      videoPlayer.savePosition();
      videoHolder.removeView(videoPlayer);
    }
    super.onConfigurationChanged(newConfig);
    setContentView(R.layout.main);
    initUi();
    videoPlayer.restorePosition();
    videoPlayer.play();
  }

  protected void initUi() {
    videoHolder = (FrameLayout) findViewById(R.id.videoHolder);

    if (videoPlayer == null) {
      videoPlayer = new DemoPlayer(this);
      videoPlayer.setCompletionCallback(this);
    }
    videoHolder.addView(videoPlayer);

    companionView = (ViewGroup) findViewById(R.id.companionFrame);
    leaderboardCompanionView = (ViewGroup) findViewById(R.id.leaderboardCompanionFrame);
    logScroll = (ScrollView) findViewById(R.id.scroll);
    log = (TextView) findViewById(R.id.log);

    languageButton = (Button) findViewById(R.id.language);
    requestAdButton = (Button) findViewById(R.id.requestAd);
    defaultTagSpinner = (Spinner) findViewById(R.id.tagSpinner);

    defaultTagUrls = getResources().getStringArray(R.array.default_ad_tags);
    setButtonListeners();
  }

  protected void createAdsLoader() {
    adsLoader = sdkFactory.createAdsLoader(this, getImaSdkSettings());
    adsLoader.addAdErrorListener(this);
    adsLoader.addAdsLoadedListener(this);
  }

  protected ImaSdkSettings getImaSdkSettings() {
    if (sdkSettings == null) {
      sdkSettings = sdkFactory.createImaSdkSettings();
    }
    return sdkSettings;
  }

  protected void playVideo() {
    log("Playing video");
    videoPlayer.playContent(CONTENT_URL);
    contentStarted = true;
  }

  protected void pauseResumeAd() {
    if (isAdStarted) {
      if (isAdPlaying) {
        log("Pausing video");
        videoPlayer.pauseAd();
      } else {
        log("Resuming video");
        videoPlayer.resumeAd();
      }
    }
  }

  protected AdsRequest buildAdsRequest() {
    container = sdkFactory.createAdDisplayContainer();
    container.setPlayer(videoPlayer);
    container.setAdContainer(videoPlayer.getUiContainer());
    log("Requesting ads");
    AdsRequest request = sdkFactory.createAdsRequest();
    request.setAdTagUrl(tagUrl);

    ArrayList<CompanionAdSlot> companionAdSlots = new ArrayList<CompanionAdSlot>();

    CompanionAdSlot companionAdSlot = sdkFactory.createCompanionAdSlot();
    companionAdSlot.setContainer(companionView);
    companionAdSlot.setSize(300, 50);
    companionAdSlots.add(companionAdSlot);

    if (leaderboardCompanionView != null) {
      CompanionAdSlot leaderboardCompanionAdSlot = sdkFactory.createCompanionAdSlot();
      leaderboardCompanionAdSlot.setContainer(leaderboardCompanionView);
      leaderboardCompanionAdSlot.setSize(728, 90);
      companionAdSlots.add(leaderboardCompanionAdSlot);
    }

    container.setCompanionSlots(companionAdSlots);
    request.setAdDisplayContainer(container);
    return request;
  }

  protected void requestAd() {
    adsLoader.requestAds(buildAdsRequest());
  }

  @Override
  public void onAdError(AdErrorEvent event) {
    log(event.getError().getMessage() + "\n");
  }

  @Override
  public void onAdsManagerLoaded(AdsManagerLoadedEvent event) {
    log("Ads loaded!");
    adsManager = event.getAdsManager();
    adsManager.addAdErrorListener(this);
    adsManager.addAdEventListener(this);
    log("Calling init.");
    adsManager.init();
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (videoPlayer != null) {
      videoPlayer.savePosition();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (videoPlayer != null) {
      videoPlayer.restorePosition();
    }
  }

  @Override
  public void onComplete() {
    if (videoPlayer.isContentPlaying()) {
      adsLoader.contentComplete();
    }
  }

  @Override
  public void onAdEvent(AdEvent event) {
    log("Event:" + event.getType());

    switch (event.getType()) {
      case LOADED:
        log("Calling start.");
        adsManager.start();
        break;
      case CONTENT_PAUSE_REQUESTED:
        if (contentStarted) {
          videoPlayer.pauseContent();
        }
        break;
      case CONTENT_RESUME_REQUESTED:
        if (contentStarted) {
          videoPlayer.resumeContent();
        } else {
          playVideo();
        }
        break;
      case STARTED:
        isAdStarted = true;
        isAdPlaying = true;
        break;
      case COMPLETED:
        isAdStarted = false;
        isAdPlaying = false;
        break;
      case ALL_ADS_COMPLETED:
        isAdStarted = false;
        isAdPlaying = false;
        adsManager.destroy();
        break;
      case PAUSED:
        isAdPlaying = false;
        break;
      case RESUMED:
        isAdPlaying = true;
        break;
      default:
        break;
    }
  }

  protected void log(String message) {
    log.append(message + "\n");
    logScroll.post(new Runnable() {
      @Override
      public void run() {
        logScroll.fullScroll(View.FOCUS_DOWN);
      }
    });
  }

  protected void promptTagUrl() {
    AlertDialog.Builder prompt = new AlertDialog.Builder(this);
    prompt.setTitle("Set ad tag URL");
    prompt.setMessage("Enter the ad tag URL to use:");

    final EditText tagInput = new EditText(this);
    tagInput.setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_URI);
    tagInput.setSingleLine(true);
    prompt.setView(tagInput);

    prompt.setPositiveButton("OK", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        tagUrl = tagInput.getText().toString();
      }
    });
    prompt.setNegativeButton("Cancel", null);

    prompt.show();
  }

  protected void promptLanguage() {
    AlertDialog.Builder prompt = new AlertDialog.Builder(this);
    prompt.setTitle("Set ad UI language code");
    prompt.setMessage("Enter 2-letter language code ('en','ru',etc.):");

    final EditText tagInput = new EditText(this);
    tagInput.setInputType(TYPE_CLASS_TEXT);
    tagInput.setSingleLine(true);
    prompt.setView(tagInput);

    prompt.setPositiveButton("OK", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        sdkSettings.setLanguage(tagInput.getText().toString());
        if (adsManager != null) {
          adsManager.destroy();
        }
        createAdsLoader();
      }
    });
    prompt.setNegativeButton("Cancel", null);
    prompt.show();
  }

  protected void setButtonListeners() {
    languageButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        promptLanguage();
      }
    });

    requestAdButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        requestAd();
      }
    });

    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
        this, R.array.default_ad_tag_labels, android.R.layout.simple_spinner_item);
    // Specify the layout to use when the list of choices appears
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // Apply the adapter to the spinner
    defaultTagSpinner.setAdapter(adapter);
    defaultTagSpinner.setOnItemSelectedListener(createTagSelectionListener());
  }

  protected OnItemSelectedListener createTagSelectionListener() {
    return new OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (pos == defaultTagUrls.length) {
          promptTagUrl();
        } else if (pos < defaultTagUrls.length) {
          tagUrl = defaultTagUrls[pos];
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> arg0) {}
    };
  }
}
