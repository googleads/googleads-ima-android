package com.google.ads.interactivemedia.v3.samples.videoplayerapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;

/** Fragment for displaying a playlist of video thumbnails that the user can select from to play. */
public class VideoListFragment extends Fragment {

  private OnVideoSelectedListener selectedCallback;
  LayoutInflater inflater;
  ViewGroup container;

  /**
   * Listener called when the user selects a video from the list. Container activity must implement
   * this interface.
   */
  public interface OnVideoSelectedListener {
    void onVideoSelected(VideoItem videoItem);
  }

  private OnVideoListFragmentResumedListener resumeCallback;

  /** Listener called when the video list fragment resumes. */
  public interface OnVideoListFragmentResumedListener {
    public void onVideoListFragmentResumed();
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    try {
      selectedCallback = (OnVideoSelectedListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(
          context.toString() + " must implement " + OnVideoSelectedListener.class.getName());
    }

    try {
      resumeCallback = (OnVideoListFragmentResumedListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(
          context.toString()
              + " must implement "
              + OnVideoListFragmentResumedListener.class.getName());
    }
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    this.inflater = inflater;
    this.container = container;
    View rootView = inflater.inflate(R.layout.fragment_video_list, container, false);

    final ListView listView = (ListView) rootView.findViewById(R.id.videoListView);
    VideoItemAdapter videoItemAdapter =
        new VideoItemAdapter(rootView.getContext(), R.layout.video_item, getVideoItems());
    listView.setAdapter(videoItemAdapter);

    listView.setOnItemClickListener(
        new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            if (selectedCallback != null) {
              VideoItem selectedVideo = (VideoItem) listView.getItemAtPosition(position);

              // If applicable, prompt the user to input a custom ad tag.
              if (selectedVideo.getAdTagUrl().equals(getString(R.string.custom_ad_tag_value))) {
                getCustomAdTag(selectedVideo);
              } else {
                selectedCallback.onVideoSelected(selectedVideo);
              }
            }
          }
        });

    return rootView;
  }

  private void getCustomAdTag(VideoItem originalVideoItem) {
    View dialogueView = inflater.inflate(R.layout.custom_ad_tag, container, false);
    final EditText txtUrl = (EditText) dialogueView.findViewById(R.id.customTag);
    txtUrl.setHint("VAST ad tag URL");
    final CheckBox isVmap = (CheckBox) dialogueView.findViewById(R.id.isVmap);
    final VideoItem videoItem = originalVideoItem;

    new AlertDialog.Builder(this.getActivity())
        .setTitle("Custom VAST Ad Tag URL")
        .setView(dialogueView)
        .setPositiveButton(
            "OK",
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int whichButton) {
                String customAdTagUrl = txtUrl.getText().toString();
                VideoItem customAdTagVideoItem =
                    new VideoItem(
                        videoItem.getVideoUrl(),
                        videoItem.getTitle(),
                        customAdTagUrl,
                        videoItem.getImageResource(),
                        isVmap.isChecked());

                if (selectedCallback != null) {
                  selectedCallback.onVideoSelected(customAdTagVideoItem);
                }
              }
            })
        .setNegativeButton(
            "Cancel",
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int whichButton) {}
            })
        .show();
  }

  private List<VideoItem> getVideoItems() {
    final List<VideoItem> videoItems = new ArrayList<VideoItem>();

    // Iterate through the videos' metadata and add video items to list.
    for (int i = 0; i < VideoMetadata.APP_VIDEOS.size(); i++) {
      VideoMetadata videoMetadata = VideoMetadata.APP_VIDEOS.get(i);
      videoItems.add(
          new VideoItem(
              videoMetadata.videoUrl,
              videoMetadata.title,
              videoMetadata.adTagUrl,
              videoMetadata.thumbnail,
              videoMetadata.isVmap));
    }

    return videoItems;
  }

  @Override
  public void onResume() {
    super.onResume();
    if (resumeCallback != null) {
      resumeCallback.onVideoListFragmentResumed();
    }
  }
}
