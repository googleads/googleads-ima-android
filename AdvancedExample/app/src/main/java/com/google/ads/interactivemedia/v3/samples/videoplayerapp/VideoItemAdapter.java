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

  private final int layoutResourceId;

  public VideoItemAdapter(Context context, int layoutResourceId, List<VideoItem> data) {
    super(context, layoutResourceId, data);
    this.layoutResourceId = layoutResourceId;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    VideoItemHolder videoItemHolder;
    View row = convertView;

    // Check if it's recycled.
    if (row == null) {
      LayoutInflater inflater = LayoutInflater.from(getContext());
      row = inflater.inflate(layoutResourceId, parent, false);
      videoItemHolder = new VideoItemHolder();
      videoItemHolder.title = row.findViewById(R.id.videoItemText);
      videoItemHolder.image = row.findViewById(R.id.videoItemImage);
      row.setTag(videoItemHolder);
    } else {
      videoItemHolder = (VideoItemHolder) row.getTag();
    }

    VideoItem item = getItem(position);

    assert item != null;
    videoItemHolder.title.setText(item.getTitle());
    videoItemHolder.image.setImageResource(item.getImageResource());

    return row;
  }

  /** Holds the UI element equivalents of a VideoItem. */
  private static class VideoItemHolder {

    TextView title;
    ImageView image;
  }
}
