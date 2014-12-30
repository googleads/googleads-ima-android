package com.google.ads.interactivemedia.v3.samples.videoplayerapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying a playlist of video thumbnails that the user can select from to play.
 */
public class VideoListFragment extends Fragment {

    private OnVideoSelectedListener mCallback;

    /**
     * Listener called when the user selects a video from the list.
     * Container activity must implement this interface.
     */
    public interface OnVideoSelectedListener {
        public void onVideoSelected(VideoItem videoItem);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnVideoSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnVideoSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_video_list, container, false);

        final ListView listView = (ListView) rootView.findViewById(R.id.videoListView);
        VideoItemAdapter videoItemAdapter = new VideoItemAdapter(rootView.getContext(),
                R.layout.video_item, getVideoItems());
        listView.setAdapter(videoItemAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if (mCallback != null) {
                    VideoItem selectedVideo = (VideoItem) listView.getItemAtPosition(position);

                    // If applicable, prompt the user to input a custom ad tag.
                    if (selectedVideo.getAdTagUrl().equals(getString(
                            R.string.custom_ad_tag_value))) {
                        getCustomAdTag(selectedVideo);
                    } else {
                        mCallback.onVideoSelected(selectedVideo);
                    }
                }
            }
        });

        return rootView;
    }

    private void getCustomAdTag(VideoItem originalVideoItem) {
        final EditText txtUrl = new EditText(this.getActivity());
        final VideoItem videoItem = originalVideoItem;
        txtUrl.setHint("VAST ad tag URL");

        new AlertDialog.Builder(this.getActivity())
                .setTitle("Custom VAST Ad Tag URL")
                .setView(txtUrl)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String customAdTagUrl = txtUrl.getText().toString();
                        VideoItem customAdTagVideoItem = new VideoItem(videoItem.getVideoUrl(),
                                videoItem.getTitle(), customAdTagUrl, videoItem.getImage());

                        if (mCallback != null) {
                            mCallback.onVideoSelected(customAdTagVideoItem);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
    }

    private List<VideoItem> getVideoItems() {
        final List<VideoItem> videoItems = new ArrayList<VideoItem>();
        // Build our Video item objects.
        Resources resources = getActivity().getResources();
        String[] videoTitles = resources.getStringArray(R.array.video_titles);
        String[] videoUrls = resources.getStringArray(R.array.video_urls);
        String[] adTags = resources.getStringArray(R.array.video_ad_tags);
        TypedArray videoThumbnails = getActivity().getResources().
                obtainTypedArray(R.array.video_thumbnails);

        for (int i = 0; i < videoTitles.length; i++) {
            Bitmap bitmap = BitmapFactory.decodeResource(getActivity().getResources(),
                    videoThumbnails.getResourceId(i, -1));
            videoItems.add(new VideoItem(videoUrls[i], videoTitles[i], adTags[i], bitmap));
        }

        return videoItems;
    }
}
