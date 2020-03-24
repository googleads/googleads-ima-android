package com.google.ads.interactivemedia.v3.samples.audioplayerexample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import androidx.annotation.DrawableRes;

/** Helper to provide a list of sample audio files for the app. */
public final class Samples {

  /** Helper to provide a sample audio file for the app. */
  public static final class Sample {
    public final Uri uri;
    public final String mediaId;
    public final String title;
    public final String description;
    public final int bitmapResource;

    public Sample(
        String uri, String mediaId, String title, String description, int bitmapResource) {
      this.uri = Uri.parse(uri);
      this.mediaId = mediaId;
      this.title = title;
      this.description = description;
      this.bitmapResource = bitmapResource;
    }

    @Override
    public String toString() {
      return title;
    }
  }

  private static final Sample[] SAMPLES =
      new Sample[] {
        new Sample(
            "https://storage.googleapis.com/automotive-media/Jazz_In_Paris.mp3",
            "audio_1",
            "Jazz in Paris",
            "Jazz for the masses",
            R.drawable.album_art_1),
        new Sample(
            "https://storage.googleapis.com/automotive-media/The_Messenger.mp3",
            "audio_2",
            "The messenger",
            "Hipster guide to London",
            R.drawable.album_art_2),
        new Sample(
            "https://storage.googleapis.com/automotive-media/Talkies.mp3",
            "audio_3",
            "Talkies",
            "If it talks like a duck and walks like a duck.",
            R.drawable.album_art_3),
      };

  public static Sample[] getSamples() {
    return SAMPLES;
  }

  public static MediaDescriptionCompat getMediaDescription(Context context, Sample sample) {
    Bundle extras = new Bundle();
    Bitmap bitmap = getBitmap(context, sample.bitmapResource);
    extras.putParcelable(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap);
    extras.putParcelable(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, bitmap);
    return new MediaDescriptionCompat.Builder()
        .setMediaId(sample.mediaId)
        .setIconBitmap(bitmap)
        .setTitle(sample.title)
        .setDescription(sample.description)
        .setExtras(extras)
        .build();
  }

  public static Bitmap getBitmap(Context context, @DrawableRes int bitmapResource) {
    return ((BitmapDrawable) context.getResources().getDrawable(bitmapResource)).getBitmap();
  }

  private Samples() {}
}
