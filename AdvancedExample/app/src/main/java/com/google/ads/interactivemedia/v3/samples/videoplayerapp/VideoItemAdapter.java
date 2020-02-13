package com.google.ads.interactivemedia.v3.samples.videoplayerapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

/** Renders VideoItems into a GridView for displaying videos in a playlist format. */
public class VideoItemAdapter extends ArrayAdapter<VideoItem> {

  private int mLayoutResourceId;

  public VideoItemAdapter(Context context, int layoutResourceId, List<VideoItem> data) {
    super(context, layoutResourceId, data);
    this.mLayoutResourceId = layoutResourceId;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    VideoItemHolder videoItemHolder;
    View row = convertView;

    // Check if it's recycled.
    if (row == null) {
      LayoutInflater inflater = LayoutInflater.from(getContext());
      row = inflater.inflate(mLayoutResourceId, parent, false);
      videoItemHolder = new VideoItemHolder();
      videoItemHolder.title = (TextView) row.findViewById(R.id.videoItemText);
      videoItemHolder.image = (ImageView) row.findViewById(R.id.videoItemImage);
      row.setTag(videoItemHolder);
    } else {
      videoItemHolder = (VideoItemHolder) row.getTag();
    }

    VideoItem item = getItem(position);

    videoItemHolder.title.setText(item.getTitle());
    videoItemHolder.image.setImageResource(item.getImageResource());

    return row;
  }

  /** Holds the UI element equivalents of a VideoItem. */
  private class VideoItemHolder {

    TextView title;
    ImageView image;
  }
}
