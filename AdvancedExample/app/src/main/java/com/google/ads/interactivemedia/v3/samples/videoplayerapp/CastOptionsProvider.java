package com.google.ads.interactivemedia.v3.samples.videoplayerapp;

import android.content.Context;
import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;
import java.util.List;

/** Provides options to the CastApplication. */
public final class CastOptionsProvider implements OptionsProvider {

  @Override
  public CastOptions getCastOptions(Context context) {
    return new CastOptions.Builder().setReceiverApplicationId(CastApplication.APP_ID).build();
  }

  @Override
  public List<SessionProvider> getAdditionalSessionProviders(Context context) {
    return null;
  }
}
