package com.google.ads.interactivemedia.v3.samples.videoplayerapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import java.io.IOException;

/**
 * Handles connection to chromecast device.
 */
public class CastApplication {
    private static final String TAG = "ChromeCastDemoActivity";
    public static final String APP_ID = "8EE292C4";
    public static final String NAMESPACE = "urn:x-cast:com.google.ads.ima.cast";

    private static CastDevice sSelectedDevice;
    private static GoogleApiClient sApiClient;

    private boolean mCastApplicationStarted;
    private RemoteMediaPlayer mRemoteMediaPlayer;
    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private VideoFragment mVideoFragment;
    private VideoPlayerController mVideoPlayerController;
    private Context mAppContext;
    private String mAdTagUrl;
    private String mContentUrl;
    private boolean mCastAdPlaying;
    private double mCastContentTime;

    public CastApplication(Context context) {
        mAppContext = context;
        mMediaRouter = MediaRouter.getInstance(context);
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(CastMediaControlIntent.categoryForCast(APP_ID))
                .build();
    }

    private final Cast.Listener castClientListener = new Cast.Listener() {
        @Override
        public void onApplicationStatusChanged() {
            if (sApiClient != null) {
                Log.d(TAG, "onApplicationStatusChanged: "
                        + Cast.CastApi.getApplicationStatus(sApiClient));
            }
        }

        @Override
        public void onApplicationDisconnected(int statusCode) {
            setSelectedDevice(null);
        }

        @Override
        public void onVolumeChanged() {
            if (sApiClient != null) {
                Log.d(TAG, "onVolumeChanged: " + Cast.CastApi.getVolume(sApiClient));
            }
        }
    };

    public void activityOnStart() {
        mMediaRouter.addCallback(mMediaRouteSelector, mediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
    }

    private void createMediaPlayer() {
        Log.d(TAG, "making media player");
        // Create a Remote Media Player
        mRemoteMediaPlayer = new RemoteMediaPlayer();

        mRemoteMediaPlayer.setOnStatusUpdatedListener(
                new RemoteMediaPlayer.OnStatusUpdatedListener() {
                    @Override
                    public void onStatusUpdated() {
                        MediaStatus mediaStatus = mRemoteMediaPlayer.getMediaStatus();
                        Log.d(TAG, "Media status: " + mediaStatus);
                    }
                });

        mRemoteMediaPlayer.setOnMetadataUpdatedListener(
                new RemoteMediaPlayer.OnMetadataUpdatedListener() {
                    @Override
                    public void onMetadataUpdated() {
                        MediaInfo mediaInfo = mRemoteMediaPlayer.getMediaInfo();
                        Log.d(TAG, "Media info: " + mediaInfo);
                    }
                });

        try {
            Cast.CastApi.setMessageReceivedCallbacks(sApiClient,
                    mRemoteMediaPlayer.getNamespace(), mRemoteMediaPlayer);
        } catch (IOException e) {
            Log.e(TAG, "Exception while creating media channel", e);
        }

        mRemoteMediaPlayer
                .requestStatus(sApiClient)
                .setResultCallback(
                        new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                            @Override
                            public void onResult(RemoteMediaPlayer.MediaChannelResult result) {
                                if (!result.getStatus().isSuccess()) {
                                    Log.e(TAG, "Failed to request status.");
                                } else {
                                    MediaMetadata mediaMetadata =
                                            new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
                                    mediaMetadata.putString(MediaMetadata.KEY_TITLE, "My video");
                                    MediaInfo mediaInfo = new MediaInfo.Builder(mContentUrl)
                                            .setContentType("video/mp4")
                                            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                                            .setMetadata(mediaMetadata)
                                            .build();
                                    loadMedia(mediaInfo, false);
                                }
                            }
                        });
    }


    private void loadMedia(MediaInfo mediaInfo, Boolean autoplay) {
        try {
            Log.d(TAG, "loading media");
            mRemoteMediaPlayer.load(sApiClient, mediaInfo, autoplay)
                    .setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                        @Override
                        public void onResult(RemoteMediaPlayer.MediaChannelResult result) {
                            if (result.getStatus().isSuccess()) {
                                // Since the player starts playing automatically we do not want to
                                // request the ad again in Chromecast except for VMAP because there
                                // are multiple ad breaks. To request a single ad use same the same
                                // message with current time as 0.
                                if (mVideoFragment.isVmap()
                                        || mVideoPlayerController.getCurrentContentTime() == 0) {
                                    sendMessage("requestAd," + mAdTagUrl + ","
                                            + mVideoPlayerController.getCurrentContentTime());
                                } else {
                                    sendMessage("seek,"
                                        + mVideoPlayerController.getCurrentContentTime());
                                }
                            } else {
                                Log.e(TAG, "Error loading Media : "
                                        + result.getStatus().getStatusCode());
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Problem opening media during loading", e);
        }
    }

    private final GoogleApiClient.ConnectionCallbacks connectionCallback =
            new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    try {
                        Log.d(TAG, "api client connected");
                        Cast.CastApi.launchApplication(sApiClient, APP_ID, false).setResultCallback(
                                new ResultCallback<Cast.ApplicationConnectionResult>() {
                                    @Override
                                    public void onResult(Cast.ApplicationConnectionResult result) {
                                        Status status = result.getStatus();
                                        if (status.isSuccess()) {
                                            mCastApplicationStarted = true;
                                            try {
                                                Cast.CastApi.setMessageReceivedCallbacks(sApiClient,
                                                        NAMESPACE, incomingMsgHandler);
                                                createMediaPlayer();
                                            } catch (Exception e) {
                                                Log.e(TAG, "Exception while creating channel", e);
                                            }
                                        } else {
                                            Log.e(TAG, "Connection result failed: "
                                                    + status.toString());
                                        }
                                    }
                                }
                        );
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to launch application", e);
                    }
                }

                @Override
                public void onConnectionSuspended(int i) {
                    Log.e(TAG, "Connection suspended");
                }
            };

    private final GoogleApiClient.OnConnectionFailedListener connectionFailedListener =
            new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(ConnectionResult connectionResult) {
                    setSelectedDevice(null);
                }
            };

    private final MediaRouter.Callback mediaRouterCallback = new MediaRouter.Callback() {
        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo info) {
            // User starts casting. Pause video on device and set Chromecast device.
            mVideoPlayerController = mVideoFragment.getVideoPlayerController();
            mVideoPlayerController.pause();
            mAdTagUrl = mVideoPlayerController.getAdTagUrl();
            mContentUrl = mVideoPlayerController.getContentVideoUrl();
            CastDevice device = CastDevice.getFromBundle(info.getExtras());
            setSelectedDevice(device);
        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo info) {
            // User stops casting. Resume video on device and seek to current time of Chromecast.
            mVideoPlayerController.resume();
            if (mCastAdPlaying) {
                mVideoPlayerController.seek(mCastContentTime);
            } else {
                double videoPosition = mRemoteMediaPlayer.getApproximateStreamPosition();
                mVideoPlayerController.seek(videoPosition / 1000.0);
            }

            stopCastApplication();
            setSelectedDevice(null);
        }
    };

    private void setSelectedDevice(CastDevice device) {
        if (mVideoFragment == null) {
            return;
        }
        sSelectedDevice = device;

        if (sSelectedDevice != null) {
            try {
                stopCastApplication();
                disconnectApiClient();
                connectApiClient();
            } catch (IllegalStateException e) {
                Log.d(TAG, "Exception while connecting API client", e);
                disconnectApiClient();
            }
        } else {
            disconnectApiClient();
            mMediaRouter.selectRoute(mMediaRouter.getDefaultRoute());
        }
    }

    private void connectApiClient() {
        Cast.CastOptions apiOptions =
                Cast.CastOptions.builder(sSelectedDevice, castClientListener).build();
        sApiClient = new GoogleApiClient.Builder(mAppContext)
                .addApi(Cast.API, apiOptions)
                .addConnectionCallbacks(connectionCallback)
                .addOnConnectionFailedListener(connectionFailedListener)
                .build();
        sApiClient.connect();
    }

    private void disconnectApiClient() {
        if (sApiClient == null) {
            return;
        }

        sApiClient.disconnect();
        sApiClient = null;
    }

    private void stopCastApplication() {
        if (sApiClient == null || !mCastApplicationStarted) {
            return;
        }

        Cast.CastApi.stopApplication(sApiClient);
        mCastApplicationStarted = false;
    }

    public final Cast.MessageReceivedCallback incomingMsgHandler =
            new Cast.MessageReceivedCallback() {
                @Override
                public void onMessageReceived(CastDevice castDevice, String namespace,
                                              String message) {
                    Log.d(TAG, "Receiving message: " + message);
                    String[] splitMessage = message.split(",");
                    String event = splitMessage[0];
                    switch (event) {
                        case "onContentPauseRequested":
                            mCastAdPlaying = true;
                            mCastContentTime = Double.parseDouble(splitMessage[1]);
                            return;
                        case "onContentResumeRequested":
                            mCastAdPlaying = false;
                            return;
                    }
                }
            };

    private void sendMessage(String message) {
        if (sApiClient != null && incomingMsgHandler != null) {
            try {
                Log.d(TAG, "Sending message: " + message);
                Cast.CastApi.sendMessage(sApiClient, NAMESPACE, message)
                        .setResultCallback(new ResultCallback<Status>() {
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
    }

    public void setVideoFragment(VideoFragment videoFragment) {
        mVideoFragment = videoFragment;
    }

    public MediaRouteSelector getMediaRouteSelector() {
        return mMediaRouteSelector;
    }
}
