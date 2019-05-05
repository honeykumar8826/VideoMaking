package com.videoMaking.holder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.bumptech.glide.RequestManager;
import com.videoMaking.R;
import com.videoMaking.modal.VideoInfo;

public class VideoPlayerViewHolder extends RecyclerView.ViewHolder {
    /*public FrameLayout media_container;*/
    public ImageView thumbnail;
    public ProgressBar progressBar;
    private View parent;

    public VideoPlayerViewHolder(@NonNull View itemView) {
        super(itemView);
        parent = itemView;
        /*media_container = itemView.findViewById(R.id.video_layout);*/
        thumbnail = itemView.findViewById(R.id.cover);
        progressBar = itemView.findViewById(R.id.progressBar);
    }

    public void onBind(VideoInfo videoInfo, RequestManager requestManager) {
        parent.setTag(this);
        requestManager
                .load(videoInfo.getCoverUrl())
                .into(thumbnail);
    }
}
