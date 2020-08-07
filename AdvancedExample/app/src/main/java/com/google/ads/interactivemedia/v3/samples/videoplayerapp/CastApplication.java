package com.google.ads.interactivemedia.v3.samples.videoplayerapp;

import android.app.Activity;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadOptions;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.cast.framework.media.RemoteMediaClient.MediaChannelResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import java.io.IOException;

/** Handles connection to a Cast device. */
public class CastApplication implements Cast.MessageReceivedCallback {

  static final String APP_ID = "93F3197F"; // Replace with your receiver app id.

  private static final String NAMESPACE = "urn:x-cast:com.google.ads.ima.cast";
  private static final String TAG = "ChromeCastDemoActivity";

  private VideoFragment videoFragment;
  private VideoPlayerController videoPlayerController;
  private String adTagUrl;
  private String contentUrl;
  private boolean castAdPlaying;
  private double castContentTime;

  private Activity activity;
  private CastContext castContext;
  private CastSession castSession;
  private SessionManager sessionManager;

  private SessionManagerListener<CastSession> sessionManagerListener =
      new SessionManagerListener<CastSession>() {
        @Override
        public void onSessionStarting(CastSession session) {}

        @Override
        public void onSessionStarted(CastSession session, String sessionId) {
          onApplicationConnected(session);
        }

        @Override
        public void onSessionStartFailed(CastSession session, int error) {
          onApplicationDisconnected();
        }

        @Override
        public void onSessionEnding(CastSession session) {
          if (castSession != null && !castAdPlaying) {
            RemoteMediaClient remoteMediaClient = castSession.getRemoteMediaClient();
            if (remoteMediaClient != null) {
              double videoPosition = remoteMediaClient.getApproximateStreamPosition();
              castContentTime = videoPosition / 1000.0;
            }
          }
        }

        @Override
        public void onSessionEnded(CastSession session, int error) {
          onApplicationDisconnected();
        }

        @Override
        public void onSessionResuming(CastSession session, String sessionId) {}

        @Override
        public void onSessionResumed(CastSession session, boolean wasSuspended) {
          onApplicationConnected(session);
        }

        @Override
        public void onSessionResumeFailed(CastSession session, int error) {
          onApplicationDisconnected();
        }

        @Override
        public void onSessionSuspended(CastSession session, int reason) {}
      };

  public CastApplication(Activity newActivity) {
    activity = newActivity;

    castContext = CastContext.getSharedInstance(newActivity);
    sessionManager = castContext.getSessionManager();
  }

  void onPause() {
    sessionManager.removeSessionManagerListener(sessionManagerListener, CastSession.class);
  }

  void onResume() {
    sessionManager.addSessionManagerListener(sessionManagerListener, CastSession.class);
  }

  private void onApplicationConnected(CastSession session) {
    castSession = session;
    try {
      castSession.setMessageReceivedCallbacks(NAMESPACE, CastApplication.this);
    } catch (IOException e) {
      Log.e(TAG, "Exception when creating channel", e);
    }
    videoPlayerController = videoFragment.getVideoPlayerController();
    adTagUrl = videoPlayerController.getAdTagUrl();
    contentUrl = videoPlayerController.getContentVideoUrl();
    videoPlayerController.pause();
    loadMedia();
    activity.invalidateOptionsMenu();
  }

  private void onApplicationDisconnected() {
    // User stops casting. Resume video on device and seek to current time of Cast.
    if (videoPlayerController == null) {
      return;
    } else if (!videoPlayerController.hasVideoStarted()) {
      // Only re-request ads if VMAP or the video hasn't started.
      if (videoFragment.isVmap() || castContentTime == 0) {
        videoPlayerController.requestAndPlayAds(castContentTime);
      }
    }
    videoPlayerController.seek(castContentTime);

    activity.invalidateOptionsMenu();
    castSession = null;

    videoPlayerController.resume();
  }

  private void loadMedia() {
    MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
    mediaMetadata.putString(MediaMetadata.KEY_TITLE, "My video");
    MediaInfo mediaInfo =
        new MediaInfo.Builder(contentUrl)
            .setContentType("video/mp4")
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setMetadata(mediaMetadata)
            .build();

    MediaLoadOptions mediaLoadOptions = new MediaLoadOptions.Builder().build();

    RemoteMediaClient remoteMediaClient = castSession.getRemoteMediaClient();
    try {
      Log.d(TAG, "loading media");
      remoteMediaClient
          .load(mediaInfo, mediaLoadOptions)
          .setResultCallback(
              new ResultCallback<MediaChannelResult>() {
                @Override
                public void onResult(@NonNull MediaChannelResult mediaChannelResult) {
                  if (mediaChannelResult.getStatus().isSuccess()) {
                    // Since the player starts playing automatically we do not want to
                    // request the ad again in Chromecast except for VMAP because there
                    // are multiple ad breaks. To request a single ad use same the same
                    // message with current time as 0.
                    if (videoFragment.isVmap()
                        || videoPlayerController.getCurrentContentTime() == 0) {
                      sendMessage(
                          "requestAd,"
                              + adTagUrl
                              + ","
                              + videoPlayerController.getCurrentContentTime());
                    } else {
                      sendMessage("seek," + videoPlayerController.getCurrentContentTime());
                    }
                  } else {
                    Log.e(
                        TAG,
                        "Error loading Media : " + mediaChannelResult.getStatus().getStatusCode());
                  }
                }
              });
    } catch (Exception e) {
      Log.e(TAG, "Problem opening media during loading", e);
    }
  }

  @Override
  public void onMessageReceived(CastDevice castDevice, String namespace, String message) {
    Log.d(TAG, "onMessageReceived: " + message);
    String[] splitMessage = message.split(",");
    String event = splitMessage[0];
    switch (event) {
      case "onContentPauseRequested":
        castAdPlaying = true;
        castContentTime = Double.parseDouble(splitMessage[1]);
        break;
      case "onContentResumeRequested":
        castAdPlaying = false;
        break;
    }
  }

  private void sendMessage(String message) {
    try {
      Log.d(TAG, "Sending message: " + message);
      castSession
          .sendMessage(NAMESPACE, message)
          .setResultCallback(
              new ResultCallback<Status>() {
                @Override
                public void onResult(Status result) {
                  if (!result.isSuccess()) {
                    Log.e(TAG, "Sending message failed");
                  }
                }
              });
    } catch (Exception e) {
      Log.e(TAG, "Exception while sending message", e);
    }
  }

  void setVideoFragment(VideoFragment newVideoFragment) {
    videoFragment = newVideoFragment;
  }
}
