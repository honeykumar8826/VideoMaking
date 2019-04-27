package com.videoMaking.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.videoMaking.R;
import com.videoMaking.modal.ShareReuseModal;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ShareReuseAdapter extends RecyclerView.Adapter<ShareReuseAdapter.ShareReuseViewHolder> {
    private final Context context;
    private List<ShareReuseModal> shareReuseModalList;

    public ShareReuseAdapter(Context context, List<ShareReuseModal> shareReuseModalList) {
        this.context = context;
        this.shareReuseModalList = shareReuseModalList;
    }

    @NonNull
    @Override
    public ShareReuseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.share_reuse_list_layout, viewGroup, false);
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = windowManager.getDefaultDisplay().getWidth();
        int height = windowManager.getDefaultDisplay().getHeight();
        view.setLayoutParams(new RecyclerView.LayoutParams(width, RecyclerView.LayoutParams.MATCH_PARENT));
        view.setLayoutParams(new RecyclerView.LayoutParams(height, RecyclerView.LayoutParams.WRAP_CONTENT));
        return new ShareReuseAdapter.ShareReuseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShareReuseViewHolder shareReuseViewHolder, int i) {
        final ShareReuseModal shareReuseModal = shareReuseModalList.get(i);
        final String imgUrl = shareReuseModal.getImgUrl();
        shareReuseViewHolder.userName.setText(shareReuseModal.getUserName());
        shareReuseViewHolder.name.setText(shareReuseModal.getName());


        Glide.with(context)
                .asBitmap()
                .load(imgUrl)
                .placeholder(R.drawable.placeholder)
                .apply(RequestOptions.circleCropTransform())
                .into(shareReuseViewHolder.userProfile);
    }

    @Override
    public int getItemCount() {
        return shareReuseModalList.size();
    }

    class ShareReuseViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.profile_img)
        ImageView userProfile;
        @BindView(R.id.tv_userName)
        TextView userName;
        @BindView(R.id.tv_name)
        TextView name;

        public ShareReuseViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}

