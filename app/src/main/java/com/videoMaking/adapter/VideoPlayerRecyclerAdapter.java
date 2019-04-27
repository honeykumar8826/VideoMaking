package com.videoMaking.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.bumptech.glide.RequestManager;
import com.videoMaking.R;
import com.videoMaking.holder.VideoPlayerViewHolder;
import com.videoMaking.modal.VideoInfo;

import java.util.List;

public class VideoPlayerRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<VideoInfo> videoInfoList;
    private RequestManager requestManager;

    public VideoPlayerRecyclerAdapter(List<VideoInfo> videoInfoList, RequestManager requestManager) {
        this.videoInfoList = videoInfoList;
        this.requestManager = requestManager;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        return new VideoPlayerViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_video_layout,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ((VideoPlayerViewHolder)viewHolder).onBind(videoInfoList.get(i),requestManager);
    }

    @Override
    public int getItemCount() {
        return videoInfoList.size();
    }
}
