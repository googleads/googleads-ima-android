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

  private VideoFragment mVideoFragment;
  private VideoPlayerController mVideoPlayerController;
  private String mAdTagUrl;
  private String mContentUrl;
  private boolean mCastAdPlaying;
  private double mCastContentTime;

  private Activity mActivity;
  private CastContext mCastContext;
  private CastSession mCastSession;
  private SessionManager mSessionManager;

  private SessionManagerListener<CastSession> mSessionManagerListener =
      new SessionManagerListener<CastSession>() {
        @Override
        public void onSessionStarting(CastSession castSession) {}

        @Override
        public void onSessionStarted(CastSession castSession, String sessionId) {
          onApplicationConnected(castSession);
        }

        @Override
        public void onSessionStartFailed(CastSession castSession, int error) {
          onApplicationDisconnected();
        }

        @Override
        public void onSessionEnding(CastSession castSession) {
          if (mCastSession != null && !mCastAdPlaying) {
            RemoteMediaClient remoteMediaClient = mCastSession.getRemoteMediaClient();
            if (remoteMediaClient != null) {
              double videoPosition = remoteMediaClient.getApproximateStreamPosition();
              mCastContentTime = videoPosition / 1000.0;
            }
          }
        }

        @Override
        public void onSessionEnded(CastSession castSession, int error) {
          onApplicationDisconnected();
        }

        @Override
        public void onSessionResuming(CastSession castSession, String sessionId) {}

        @Override
        public void onSessionResumed(CastSession castSession, boolean wasSuspended) {
          onApplicationConnected(castSession);
        }

        @Override
        public void onSessionResumeFailed(CastSession castSession, int error) {
          onApplicationDisconnected();
        }

        @Override
        public void onSessionSuspended(CastSession castSession, int reason) {}
      };

  public CastApplication(Activity activity) {
    mActivity = activity;

    mCastContext = CastContext.getSharedInstance(activity);
    mSessionManager = mCastContext.getSessionManager();
  }

  void onPause() {
    mSessionManager.removeSessionManagerListener(mSessionManagerListener, CastSession.class);
  }

  void onResume() {
    mSessionManager.addSessionManagerListener(mSessionManagerListener, CastSession.class);
  }

  private void onApplicationConnected(CastSession castSession) {
    mCastSession = castSession;
    try {
      mCastSession.setMessageReceivedCallbacks(NAMESPACE, CastApplication.this);
    } catch (IOException e) {
      Log.e(TAG, "Exception when creating channel", e);
    }
    mVideoPlayerController = mVideoFragment.getVideoPlayerController();
    mAdTagUrl = mVideoPlayerController.getAdTagUrl();
    mContentUrl = mVideoPlayerController.getContentVideoUrl();
    mVideoPlayerController.pause();
    loadMedia();
    mActivity.invalidateOptionsMenu();
  }

  private void onApplicationDisconnected() {
    // User stops casting. Resume video on device and seek to current time of Cast.
    if (mVideoPlayerController == null) {
      return;
    } else if (!mVideoPlayerController.hasVideoStarted()) {
      // Only re-request ads if VMAP or the video hasn't started.
      if (mVideoFragment.isVmap() || mCastContentTime == 0) {
        mVideoPlayerController.requestAndPlayAds(mCastContentTime);
      }
    }
    mVideoPlayerController.seek(mCastContentTime);

    mActivity.invalidateOptionsMenu();
    mCastSession = null;

    mVideoPlayerController.resume();
  }

  private void loadMedia() {
    MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
    mediaMetadata.putString(MediaMetadata.KEY_TITLE, "My video");
    MediaInfo mediaInfo =
        new MediaInfo.Builder(mContentUrl)
            .setContentType("video/mp4")
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setMetadata(mediaMetadata)
            .build();

    MediaLoadOptions mediaLoadOptions = new MediaLoadOptions.Builder().build();

    RemoteMediaClient remoteMediaClient = mCastSession.getRemoteMediaClient();
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
                    if (mVideoFragment.isVmap()
                        || mVideoPlayerController.getCurrentContentTime() == 0) {
                      sendMessage(
                          "requestAd,"
                              + mAdTagUrl
                              + ","
                              + mVideoPlayerController.getCurrentContentTime());
                    } else {
                      sendMessage("seek," + mVideoPlayerController.getCurrentContentTime());
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
        mCastAdPlaying = true;
        mCastContentTime = Double.parseDouble(splitMessage[1]);
        break;
      case "onContentResumeRequested":
        mCastAdPlaying = false;
        break;
    }
  }

  private void sendMessage(String message) {
    try {
      Log.d(TAG, "Sending message: " + message);
      mCastSession
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

  void setVideoFragment(VideoFragment videoFragment) {
    mVideoFragment = videoFragment;
  }
}
