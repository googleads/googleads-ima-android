package com.example.snippets;

import android.content.Context;
import android.view.ViewGroup;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ima.ImaAdsLoader;
import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.CompanionAdSlot;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import java.util.ArrayList;

/** Snippets for the "Add companion ads support" guide in IMA Android client-side documentation. */
@OptIn(markerClass = UnstableApi.class)
public class CompanionAdSnippets {

  // [START create_companion_ad_slot]
  public void createCompanionAdSlot(ViewGroup companionViewGroup) {
    ImaSdkFactory sdkFactory = ImaSdkFactory.getInstance();
    CompanionAdSlot companionAdSlot = sdkFactory.createCompanionAdSlot(companionViewGroup);
    companionAdSlot.setSize(300, 250);
    ArrayList<CompanionAdSlot> companionAdSlots = new ArrayList<CompanionAdSlot>();
    companionAdSlots.add(companionAdSlot);
  }

  // [END create_companion_ad_slot]

  // [START set_companion_ad_slots_exoplayer]
  public void setCompanionAdSlotExoPlayer(
      Context context, ArrayList<CompanionAdSlot> companionAdSlots) {
    ImaAdsLoader adsLoader =
        new ImaAdsLoader.Builder(context).setCompanionAdSlots(companionAdSlots).build();
  }

  // [END set_companion_ad_slots_exoplayer]

  // [START set_companion_ad_slots]
  public void setCompanionAdSlot(
      AdDisplayContainer adDisplayContainer, ArrayList<CompanionAdSlot> companionAdSlots) {
    adDisplayContainer.setCompanionSlots(companionAdSlots);
  }

  // [END set_companion_ad_slots]

  public void createFluidCompanionAdSlot(ViewGroup companionViewGroup) {
    ImaSdkFactory sdkFactory = ImaSdkFactory.getInstance();
    CompanionAdSlot companionAdSlot = sdkFactory.createCompanionAdSlot(companionViewGroup);
    // [START set_fluid_size_companion_ad]
    companionAdSlot.setSize(CompanionAdSlot.FLUID_SIZE, CompanionAdSlot.FLUID_SIZE);
    // [END set_fluid_size_companion_ad]
    ArrayList<CompanionAdSlot> companionAdSlots = new ArrayList<CompanionAdSlot>();
    companionAdSlots.add(companionAdSlot);
  }
}
